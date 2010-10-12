/*
 * RemoteAPIImpl.java
 *
 * Copyright (c) 2008, Ralf Biedert All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. Redistributions in binary form must reproduce the
 * above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * Neither the name of the author nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package net.xeoh.plugins.remote.impl.xmlrpc;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
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
import net.xeoh.plugins.remote.ExportResult;
import net.xeoh.plugins.remote.PublishMethod;
import net.xeoh.plugins.remote.RemoteAPIXMLRPC;
import net.xeoh.plugins.remote.util.internal.PluginExport;
import net.xeoh.plugins.remote.util.vanilla.ExportResultImpl;
import net.xeoh.plugins.remotediscovery.RemoteDiscovery;
import net.xeoh.plugins.remotediscovery.util.RemoteAPIDiscoveryUtil;

import com.flat502.rox.client.XmlRpcClient;
import com.flat502.rox.server.XmlRpcServer;

/**
 * TODO: XMLRPC impl. appears to be crappy: marshalling problems with chars, no null or
 * void support, ... replace this with something different in the future.
 *
 * @author Ralf Biedert
 *
 */
@Author(name = "Ralf Biedert")
@PluginImplementation
public class RemoteAPIImpl implements RemoteAPIXMLRPC {
    /** */
    @InjectPlugin
    public PluginConfiguration configuration;

    /**
     * Where this server can be found
     */
    private String exportUrl = "http://";

    /**
     * Server object to receive requests
     */
    private volatile XmlRpcServer server;

    /**
     * Lock server from concurrent access
     */
    private final Lock serverLock = new ReentrantLock();

    /** */
    @InjectPlugin
    public RemoteDiscovery discovery;

    /**
     * Log events
     */
    final Logger logger = Logger.getLogger(this.getClass().getName());

    /** */
    private RemoteAPIDiscoveryUtil remoteAPIDiscoveryUtil;

    /*
     * (non-Javadoc)
     *
     * @see net.xeoh.plugins.remote.RemoteAPI#exportPlugin(net.xeoh.plugins.base.Plugin)
     */
    public URI exportPlugin(final Plugin plugin, int port) {
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

        // All interfaces this class implements
   
        /*
         *   // FIXME: Might need improvement.
        final Class<?>[] interfaces = plugin.getClass().getInterfaces();

        for (final Class<?> class1 : interfaces) {
            if (Plugin.class.isAssignableFrom(class1)) {
                exportName = class1.getSimpleName();
            }
        }*/

        // The URL we export at
        final String exportURL = this.exportUrl + exportName;

        this.serverLock.lock();
        try {
            this.server.registerProxyingHandler(null, "^" + exportName + "\\.(.*)", plugin);
        } finally {
            this.serverLock.unlock();
        }

        URI createURI = createURI(exportURL);

        // Announce the plugin
        this.discovery.announcePlugin(plugin, getPublishMethod(), createURI);

        return createURI;
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remote.RemoteAPI#exportPlugin(net.xeoh.plugins.base.Plugin)
     */
    public ExportResult exportPlugin(final Plugin plugin) {
        return new ExportResultImpl(exportPlugin(plugin, 0));
    }

    /**
     * @return .
     */
    @Capabilities
    public String[] getCapabilites() {
        return new String[] { "xmlrpc", "XMLRPC" };
    }

    /** */
    @Init
    public void init() {
        this.remoteAPIDiscoveryUtil = new RemoteAPIDiscoveryUtil(this.discovery, this);
    }

    public PublishMethod getPublishMethod() {
        return PublishMethod.XMLRPC;
    }

    @SuppressWarnings("unchecked")
    public <R extends Plugin> R getRemoteProxy(final URI url, final Class<R> remote) {

        // In case this is a remote url, let the discoverer work.
        if (this.remoteAPIDiscoveryUtil.isDiscoveryURI(url)) { return this.remoteAPIDiscoveryUtil.getRemoteProxy(url, remote); }

        try {
            final String prefix = url.getPath().substring(1) + ".";
            final XmlRpcClient client = new XmlRpcClient(url.toURL());
            return (R) client.proxyObject(prefix, remote);
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** */
    @Shutdown
    public void shutdown() {
        try {
            if (this.server != null) {
                this.server.stop();
            }
            this.server = null;
        } catch (final IOException e) {
            //
        }
    }

    public void unexportPlugin(final Plugin plugin) {
        // TODO Auto-generated method stub

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
        } catch (URISyntaxException e) {
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

                // FIXME: Export to public network, not to localhost ...
                this.server = new XmlRpcServer(SERVER_PORT);
                this.server.start();

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

    /**
     * @return
     */
    private static int getFreePort() {

        try {
            final ServerSocket serverSocket = new ServerSocket(0);
            final int port = serverSocket.getLocalPort();
            serverSocket.close();
            return port;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 1025 + new Random().nextInt(50000);
    }

}