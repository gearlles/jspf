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

package net.xeoh.plugins.remote.impl.lipermi;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.logging.Logger;

import net.sf.lipermi.exception.LipeRMIException;
import net.sf.lipermi.handler.CallHandler;
import net.sf.lipermi.net.Client;
import net.sf.lipermi.net.Server;
import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginConfiguration;
import net.xeoh.plugins.base.annotations.Capabilities;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.Init;
import net.xeoh.plugins.base.annotations.events.Shutdown;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;
import net.xeoh.plugins.base.util.PluginConfigurationUtil;
import net.xeoh.plugins.base.util.PluginUtil;
import net.xeoh.plugins.remote.ExportResult;
import net.xeoh.plugins.remote.PublishMethod;
import net.xeoh.plugins.remote.RemoteAPI;
import net.xeoh.plugins.remote.RemoteAPILipe;
import net.xeoh.plugins.remote.util.internal.PluginExport;
import net.xeoh.plugins.remote.util.vanilla.ExportResultImpl;
import net.xeoh.plugins.remotediscovery.RemoteDiscovery;
import net.xeoh.plugins.remotediscovery.util.RemoteAPIDiscoveryUtil;

/**
 * Essence RMI Implementation. Nice framework ...
 *
 * @author Ralf Biedert
 *
 */
@PluginImplementation
public class RemoteAPIImpl implements RemoteAPILipe {
    /** */
    @InjectPlugin
    public PluginConfiguration configuration;

    /** */
    @InjectPlugin
    public PluginConfiguration facade;

    /** */
    private String exportServer = "127.0.0.1";

    /** Where this server can be found */
    private final String protocol = "lipe://";

    /** Port to start using */
    //private final int START_PORT = 22719;

    /** Log events */
    final Logger logger = Logger.getLogger(this.getClass().getName());

    /** */
    @InjectPlugin
    public RemoteDiscovery discovery;

    /** */
    private RemoteAPIDiscoveryUtil remoteAPIDiscoveryUtil;

    /** */
    private Server lipeServer;

    /** */
    private CallHandler callHandler;

    /** */
    int port;

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remote.RemoteAPI#exportPlugin(net.xeoh.plugins.base.Plugin)
     */
    public ExportResult exportPlugin(final Plugin plugin) {

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

        final String name =  PluginExport.getExportName(plugin);
        final Class<? extends Plugin> exporter = new PluginUtil(plugin).getPrimaryInterfaces().iterator().next();

        /*
        // FIXME: Might need improvement.
        final Class<?>[] interfaces = plugin.getClass().getInterfaces();

        Class<?> exporter = null;

        // All interfaces this class implements
        for (final Class<?> class1 : interfaces) {
            if (Plugin.class.isAssignableFrom(class1)) {
                exporter = class1;
            }
        }

*/
        
        this.logger.fine("Using exporter " + exporter);

        try {
            this.callHandler.registerGlobal(exporter, plugin);
        } catch (LipeRMIException e) {
            e.printStackTrace();
        }

        // Return a proper URL
        URI createURI = createURI(this.protocol + servername + ":" + this.port + "/" + name);

        // Announce the plugin
        this.discovery.announcePlugin(plugin, getPublishMethod(), createURI);

        return new ExportResultImpl(createURI);
    }

    /**
     * Returns the capabilities of this plugin.
     * 
     * @return .
     */
    @Capabilities
    public String[] getCapabilites() {
        return new String[] { "lipe", "LIPE" };
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remote.RemoteAPI#getPublishMethod()
     */
    public PublishMethod getPublishMethod() {
        return PublishMethod.LIPE;
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remote.RemoteAPI#getRemoteProxy(java.net.URI, java.lang.Class)
     */
    @SuppressWarnings( { "unchecked" })
    public <R extends Plugin> R getRemoteProxy(final URI url, final Class<R> remote) {
        // In case this is a remote url, let the discoverer work.
        if (this.remoteAPIDiscoveryUtil.isDiscoveryURI(url)) { return this.remoteAPIDiscoveryUtil.getRemoteProxy(url, remote); }

        if (url == null) {
            this.logger.warning("URL was null. Cannot get a proxy for that, returning null.");
            return null;
        }

        this.logger.fine("Trying to retrieve remote proxy for " + url);

        final CallHandler handler = new CallHandler();

        try {
            final Client client = new Client(url.getHost(), url.getPort(), handler);
            final R proxy = (R) client.getGlobal(remote);

            return proxy;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    /** */
    @SuppressWarnings("boxing")
    @Init
    public void init() {
        // Try to obtain a proper address
        try {
            this.exportServer = InetAddress.getLocalHost().getHostAddress();
        } catch (final UnknownHostException e) {
            //
        }

        this.remoteAPIDiscoveryUtil = new RemoteAPIDiscoveryUtil(this.discovery, this);

        this.port = new PluginConfigurationUtil(this.configuration).getInt(getClass(), "export.port", getFreePort());

        this.callHandler = new CallHandler();
        this.lipeServer = new Server();
        try {
            this.lipeServer.bind(this.port, this.callHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** */
    @Shutdown
    public void shutdown() {
        this.lipeServer.close();
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