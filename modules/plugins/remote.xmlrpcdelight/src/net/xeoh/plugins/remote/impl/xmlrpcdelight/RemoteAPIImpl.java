/*
 * RemoteAPIImpl.java
 * 
 * Copyright (c) 2008, Ralf Biedert All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. Redistributions in
 * binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 * 
 * Neither the name of the author nor the names of its contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.xeoh.plugins.remote.impl.xmlrpcdelight;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginConfiguration;
import net.xeoh.plugins.base.annotations.Capabilities;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.Init;
import net.xeoh.plugins.base.annotations.events.Shutdown;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;
import net.xeoh.plugins.base.annotations.meta.Author;
import net.xeoh.plugins.base.util.OptionUtils;
import net.xeoh.plugins.remote.ExportResult;
import net.xeoh.plugins.remote.PublishMethod;
import net.xeoh.plugins.remote.impl.RemoteAPIXMLRPCDelight;
import net.xeoh.plugins.remote.options.ExportVanillaObjectOption;
import net.xeoh.plugins.remote.options.exportvanillaobject.OptionExportName;
import net.xeoh.plugins.remote.util.internal.PluginExport;
import net.xeoh.plugins.remote.util.vanilla.ExportResultImpl;
import net.xeoh.plugins.remotediscovery.RemoteDiscovery;
import net.xeoh.plugins.remotediscovery.util.RemoteAPIDiscoveryUtil;

import org.apache.xmlrpc.WebServer;
import org.apache.xmlrpc.XmlRpcHandler;

import de.dfki.util.xmlrpc.XmlRpc;
import de.dfki.util.xmlrpc.common.XmlRpcConnection;
import de.dfki.util.xmlrpc.server.XmlRpcHandlerFactory;

/**
 * RemoteApi implementation for DFKI's XMLRPC Delight
 * 
 * @author Ralf Biedert, Andreas Lauer, Christian Reuschling
 * 
 */
@Author(name = "Ralf Biedert, Andreas Lauer, Christian Reuschling")
@PluginImplementation
public class RemoteAPIImpl implements RemoteAPIXMLRPCDelight {
    /**
     * @return
     */
    private static int getFreePort() {

        try {
            final ServerSocket serverSocket = new ServerSocket(0);
            final int port = serverSocket.getLocalPort();
            serverSocket.close();
            return port;
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return 1025 + new Random().nextInt(50000);
    }

    /** */
    @InjectPlugin
    public PluginConfiguration configuration;

    /** */
    @InjectPlugin
    public RemoteDiscovery discovery;

    /**
     * Where this server can be found
     */
    private String exportUrl = "http://";

    /** */
    private RemoteAPIDiscoveryUtil remoteAPIDiscoveryUtil;

    /**
     * Lock server from concurrent access
     */
    private final Lock serverLock = new ReentrantLock();

    /**
     * Log events
     */
    final Logger logger = Logger.getLogger(this.getClass().getName());

    /** Used for unexport */
    HashMap<Plugin, String> handlerToPluginMap = new HashMap<Plugin, String>();

    /**
     * Server object to receive requests
     */
    volatile WebServer server;

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remote.RemoteAPI#exportPlugin(net.xeoh.plugins.base.Plugin)
     */
    public ExportResult exportPlugin(final Plugin plugin) {
        this.serverLock.lock();
        if (this.server == null) {
            initServer();
        }
        this.serverLock.unlock();

        // If this server is still null now, return
        if (this.server == null) return null;

        //
        // Try to find the most appropriate export name
        //


        String exportName =  PluginExport.getExportName(plugin);
        
        /*
        // FIXME: Might need improvement.
        final Class<?>[] interfaces = plugin.getClass().getInterfaces();

        // All interfaces this class implements
        for (final Class<?> class1 : interfaces) {
            if (Plugin.class.isAssignableFrom(class1)) {
                exportName = class1.getSimpleName();
            }
        }
        */

        // The URL we export at
        final String exportURL = this.exportUrl + exportName;

        this.serverLock.lock();

        try {

            final XmlRpcHandler delightHandler = XmlRpcHandlerFactory.createHandlerFor(plugin);

            this.handlerToPluginMap.put(plugin, exportName);

            this.server.addHandler(exportName, delightHandler);

        } finally {
            this.serverLock.unlock();
        }

        final URI createURI = createURI(exportURL);

        // Announce the plugin
        this.discovery.announcePlugin(plugin, getPublishMethod(), createURI);

        return new ExportResultImpl(createURI);
    }

    /**
     * @return .
     */
    @Capabilities
    public String[] getCapabilites() {
        return new String[] { "xmlrpc", "XMLRPC", "xmlrpcdelight", "XMLRPCDELIGHT" };
    }

    public PublishMethod getPublishMethod() {
        return PublishMethod.XMLRPCDELIGHT;
    }

    public <R extends Plugin> R getRemoteProxy(final URI url, final Class<R> remote) {

        // In case this is a remote url, let the discoverer work.
        if (this.remoteAPIDiscoveryUtil.isDiscoveryURI(url))
            return this.remoteAPIDiscoveryUtil.getRemoteProxy(url, remote);

        try {
            String handler = url.getPath();

            handler = handler.substring(handler.lastIndexOf('/') + 1);

            final R client = XmlRpc.createClient(remote, handler, XmlRpcConnection.connect(url.toURL()));

            return client;

        } catch (final IOException e) {
            e.printStackTrace();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** */
    @Init
    public void init() {
        this.remoteAPIDiscoveryUtil = new RemoteAPIDiscoveryUtil(this.discovery, this);
    }

    /** */
    @Shutdown
    public void shutdown() {
        // shutdown in einem Extra-Thread, damit es sich nicht verhakt

        if (this.server != null) {
            final Thread shutDownThread = new Thread(new Runnable() {
                public void run() {
                    RemoteAPIImpl.this.server.shutdown();
                    RemoteAPIImpl.this.server = null;
                    RemoteAPIImpl.this.logger.info("XmlRpc server shut down");
                }
            });

            shutDownThread.start();

        }
    }

    public void unexportPlugin(final Plugin plugin) {
        final String strPluginID = this.handlerToPluginMap.get(plugin);

        this.handlerToPluginMap.remove(plugin);

        this.server.removeHandler(strPluginID);
    }

    /**
     * Internally used to create an URL without 'try'
     * 
     * @param string
     * @return
     */
    URI createURI(final String string) {
        try {
            return new URI(string);
        } catch (final URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Try to setup the server if it's not already there
     */
    void initServer() {

        int NUM_RETRIES = 10;
        int SERVER_PORT = getFreePort();

        try {
            SERVER_PORT = Integer.parseInt(this.configuration.getConfiguration(RemoteAPIImpl.class, "xmlrpc.port"));
        } catch (final NumberFormatException e) {
            // Do nothing, as the most probable reason is the config was not set.
        }

        this.logger.info("Setting up XMLRPC-server on port " + SERVER_PORT);

        boolean succeded = false;

        while (!succeded && NUM_RETRIES-- > 0) {
            try {
                this.exportUrl += InetAddress.getLocalHost().getCanonicalHostName();
                this.exportUrl += ":" + SERVER_PORT + "/";

                this.server = new WebServer(SERVER_PORT);
                // start wird schon im Konstruktor ausgeführt
                // this.server.start();

                this.logger.info("XMLRPC server listening on baseroot " + this.exportUrl);
                succeded = true;
            } catch (final UnknownHostException e) {
                this.logger.warning("Unable to create XMLRPC handler for this host");
                e.printStackTrace();
            } catch (final IOException e) {
                this.logger.warning("Unable to create XMLRPC handler for this host");
                e.printStackTrace();
            }

            SERVER_PORT++;
        }

    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remote.impl.RemoteAPIXMLRPCDelight#exportVanillaObject(java.lang.Object, net.xeoh.plugins.base.Option[])
     */
    public URI exportVanillaObject(Object toExport, ExportVanillaObjectOption... option) {
        this.serverLock.lock();
        if (this.server == null) {
            initServer();
        }
        this.serverLock.unlock();

        // 
        //final String exportName = toExport.getClass().getSimpleName() + "@" + System.nanoTime();

        final OptionUtils<ExportVanillaObjectOption> ou = new OptionUtils<ExportVanillaObjectOption>(option);
        if (ou.contains(OptionExportName.class)) {
            ou.get(OptionExportName.class);
            // TODO: Get export name
        }

        // If this server is still null now, return
        if (this.server == null) return null;

        return null;
    }
}
