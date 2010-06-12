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

package net.xeoh.plugins.remote.impl.ermi;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginConfiguration;
import net.xeoh.plugins.base.annotations.Capabilities;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.Init;
import net.xeoh.plugins.base.annotations.events.Shutdown;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;
import net.xeoh.plugins.base.annotations.meta.Author;
import net.xeoh.plugins.base.util.PluginConfigurationUtil;
import net.xeoh.plugins.remote.ExportResult;
import net.xeoh.plugins.remote.PublishMethod;
import net.xeoh.plugins.remote.RemoteAPI;
import net.xeoh.plugins.remote.RemoteAPIERMI;
import net.xeoh.plugins.remote.util.internal.PluginExport;
import net.xeoh.plugins.remote.util.vanilla.ExportResultImpl;
import net.xeoh.plugins.remotediscovery.RemoteDiscovery;
import net.xeoh.plugins.remotediscovery.util.RemoteAPIDiscoveryUtil;

import org.freshvanilla.rmi.Proxies;
import org.freshvanilla.rmi.VanillaRmiServer;

/**
 * Essence RMI Implementation. Nice framework ...
 *
 * @author Ralf Biedert
 *
 */
@Author(name = "Ralf Biedert")
@PluginImplementation
public class RemoteAPIImpl implements RemoteAPIERMI {
    /** */
    @InjectPlugin
    public PluginConfiguration configuration;

    /** */
    @InjectPlugin
    public PluginConfiguration facade;

    private String exportServer = "127.0.0.1";

    /** Where this server can be found */
    private final String protocol = "ermi://";

    /** Port to start using */
    //private final int START_PORT = 22719;
    /** Needed for shutdown. */
    final List<VanillaRmiServer<Plugin>> allServer = new ArrayList<VanillaRmiServer<Plugin>>();

    /** Log events */
    final Logger logger = Logger.getLogger(this.getClass().getName());

    /** */
    @InjectPlugin
    public RemoteDiscovery discovery;

    /** */
    private RemoteAPIDiscoveryUtil remoteAPIDiscoveryUtil;

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remote.RemoteAPI#exportPlugin(net.xeoh.plugins.base.Plugin)
     */
    public ExportResult exportPlugin(final Plugin plugin) {

        // Get some (very) random start port.
        final int startInt = getFreePort(); //this.START_PORT + r.nextInt(10000);

        for (int d = 0; d < 10; d++) {
            final URI exportPlugin = exportPlugin(plugin, startInt + d);
            if (exportPlugin != null) return new ExportResultImpl(exportPlugin);
        }

        return null;
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remote.RemoteAPIERMI#exportPlugin(net.xeoh.plugins.base.Plugin, int)
     */
    public URI exportPlugin(final Plugin plugin, final int port) {

        // Try to set the servername to something sensible
        String servername = this.configuration.getConfiguration(RemoteAPI.class, "export.server");
        if (servername == null) {
            servername = this.exportServer;
        }

        // Sanity checks
        if (plugin == null) {
            this.logger.warning("Cannot export plugin, as it is null");
            return null;
        }

        final String name = PluginExport.getExportName(plugin);

        try {
            final VanillaRmiServer<Plugin> newServer = Proxies.newServer(name, port, plugin);
            synchronized (this.allServer) {
                this.allServer.add(newServer);
            }
        } catch (final IOException e) {
            this.logger.warning("Unable to export the plugin, Proxes.newServer excepted ...");
            e.printStackTrace();
            return null;
        }

        // Return a proper URL
        URI createURI = createURI(this.protocol + servername + ":" + port + "/" + name);

        // Announce the plugin
        this.discovery.announcePlugin(plugin, getPublishMethod(), createURI);

        return createURI;
    }

    /**
     * Returns the capabilities of this plugin.
     * 
     * @return .
     */
    @Capabilities
    public String[] getCapabilites() {
        return new String[] { "ermi", "ERMI" };
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remote.RemoteAPI#getPublishMethod()
     */
    public PublishMethod getPublishMethod() {
        return PublishMethod.ERMI;
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remote.RemoteAPI#getRemoteProxy(java.net.URI, java.lang.Class)
     */
    @SuppressWarnings( { "unchecked", "boxing" })
    public <R extends Plugin> R getRemoteProxy(final URI url, final Class<R> remote) {
        // In case this is a remote url, let the discoverer work.
        if (this.remoteAPIDiscoveryUtil.isDiscoveryURI(url)) { return this.remoteAPIDiscoveryUtil.getRemoteProxy(url, remote); }

        if (url == null) {
            this.logger.warning("URL was null. Cannot get a proxy for that, returning null.");
            return null;
        }

        this.logger.info("Trying to retrieve remote proxy for " + url);

        final String address = url.getHost() + ":" + url.getPort();
        final String name = url.getPath().substring(1);

        final PluginConfigurationUtil pcu = new PluginConfigurationUtil(this.configuration);

        // Obtain timeout when creating proxy
        final int timeout = pcu.getInt(RemoteAPI.class, "proxy.timeout", 500);

        // Tricky part, obtaining the proxy works fast, but we only know everything works fine after the first call,
        // so lets try that ...
        final R newClient = Proxies.newClient(name, address, remote);

        // Execute collection asynchronously
        final ExecutorCompletionService<String> ecs = new ExecutorCompletionService(Executors.newCachedThreadPool());
        ecs.submit(new Callable<String>() {

            public String call() throws Exception {
                return newClient.toString();
            }
        });

        // Wait at most half a second (TODO: Make this configurable)
        try {
            final Future<String> poll = ecs.poll(timeout, TimeUnit.MILLISECONDS);
            if (poll == null) return null;

            poll.get(timeout, TimeUnit.MILLISECONDS);

            return newClient;
        } catch (final InterruptedException e) {
            this.logger.fine("Error while waiting for a getRemoteProxy() result");
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        } catch (final TimeoutException e) {
            e.printStackTrace();
        }

        return null;
    }

    /** */
    @Init
    public void init() {
        // Try to obtain a proper address
        try {
            this.exportServer = InetAddress.getLocalHost().getHostAddress();
        } catch (final UnknownHostException e) {
            //
        }

        this.remoteAPIDiscoveryUtil = new RemoteAPIDiscoveryUtil(this.discovery, this);
    }

    /** */
    @Shutdown
    public void shutdown() {
        synchronized (this.allServer) {
            for (final VanillaRmiServer<Plugin> server : this.allServer) {
                server.close();
            }
        }
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remote.RemoteAPI#unexportPlugin(net.xeoh.plugins.base.Plugin)
     */
    public void unexportPlugin(final Plugin plugin) {
        // Implement this
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