/*
 * RemoteAPIImpl.java
 * 
 * Copyright (c) 2009, Ralf Biedert All rights reserved.
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
package net.xeoh.plugins.remote.impl.javascript;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.logging.Logger;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginConfiguration;
import net.xeoh.plugins.base.annotations.Capabilities;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.Shutdown;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;
import net.xeoh.plugins.base.annotations.meta.Author;
import net.xeoh.plugins.remote.ExportResult;
import net.xeoh.plugins.remote.PublishMethod;
import net.xeoh.plugins.remote.RemoteAPI;
import net.xeoh.plugins.remote.util.internal.PluginExport;
import net.xeoh.plugins.remote.util.vanilla.ExportResultImpl;
import net.xeoh.plugins.remotediscovery.RemoteDiscovery;

import org.directwebremoting.convert.BeanConverter;
import org.directwebremoting.extend.ConverterManager;
import org.directwebremoting.extend.CreatorManager;
import org.directwebremoting.servlet.DwrServlet;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

/**
 * Exports plugins to be accessible by JavaScript.  
 * 
 * @author Thomas Lottermann
 */
@Author(name = "Thomas Lottermann")
@PluginImplementation
public class RemoteAPIImpl implements RemoteAPI {

    /** Log events */
    final Logger logger = Logger.getLogger(this.getClass().getName());

    /** */
    @InjectPlugin
    public PluginConfiguration configuration;

    /** Accepts objects */
    CreatorManager creatorManager;

    /** Converts objects and variables */
    ConverterManager converterManager;

    /** Base URL to return (without trailing slash) */
    private String location;

    /** Jetty reference */
    Server server = null;

    /** */
    @InjectPlugin
    public RemoteDiscovery discovery;

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remote.RemoteAPI#exportPlugin(net.xeoh.plugins.base.Plugin)
     */
    public synchronized ExportResult exportPlugin(final Plugin plugin) {
        if (this.server == null) {
            initServer();
        }

        // Wrap object
        final ObjectCreator creator = new ObjectCreator(plugin);

        // Register object to manager
        this.creatorManager.addCreator(creator.getClassName(), creator);

        //this.converterManager.addConverter("java.util.concurrent.locks.ReentrantLock", new BeanConverter());
        //FIXME: Doesn't work. Maybe get Converters by inspection at export time. 
        this.converterManager.addConverter("*", new BeanConverter());
        this.converterManager.addConverter("com.*", new BeanConverter());
        this.converterManager.addConverter("org.*", new BeanConverter());
        this.converterManager.addConverter("de.*", new BeanConverter());
        this.converterManager.addConverter("uk.*", new BeanConverter());
        this.converterManager.addConverter("java.*", new BeanConverter());
        this.converterManager.addConverter("javax.*", new BeanConverter());
        this.converterManager.addConverter("net.*", new BeanConverter());

        // Return browser-compatible URL
        URI uri = createURI(this.location + "/test/" + PluginExport.getExportName(plugin));
        this.logger.info("Plugin exported as JavaScript to " + uri);

        // Announce the plugin
        this.discovery.announcePlugin(plugin, getPublishMethod(), uri);

        return new ExportResultImpl(uri);
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remote.RemoteAPI#getPublishMethod()
     */
    public PublishMethod getPublishMethod() {
        return PublishMethod.JAVASCRIPT;
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remote.RemoteAPI#getRemoteProxy(java.net.URL, java.lang.Class)
     */
    public <R extends Plugin> R getRemoteProxy(final URI url, final Class<R> remote) {
        this.logger.warning("JavaScript Plugin was requested to return remote proxy for an JavaScript instance. This does not work. Sorry.");
        return null;
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remote.RemoteAPI#unexportPlugin(net.xeoh.plugins.base.Plugin)
     */
    public void unexportPlugin(final Plugin plugin) {
        // TODO Auto-generated method stub
    }

    /**
     * Call this once to bring up the server 
     */
    private void initServer() {

        String _servername = this.configuration.getConfiguration(RemoteAPIImpl.class, "export.server");
        String servername = _servername == null ? "127.0.0.1" : _servername;

        String _port = this.configuration.getConfiguration(RemoteAPIImpl.class, "port");
        int port = getFreePort();
        if (_port != null) {
            port = Integer.parseInt(_port);
        }

        // Set the location and server
        this.location = "http://" + servername + ":" + port;
        this.server = new Server(port);

        final Context context = new Context(this.server, "/", Context.SESSIONS);
        final DwrServlet servlet = new DwrServlet();
        final ServletHolder servletholder = new ServletHolder(servlet);

        servletholder.setInitParameter("debug", "true");
        servletholder.setInitParameter("activeReverseAjaxEnabled", "true");
        servletholder.setInitParameter("initApplicationScopeCreatorsAtStartup", "true");
        servletholder.setInitParameter("jsonRpcEnabled", "true");
        servletholder.setInitParameter("jsonpEnabled", "true");
        servletholder.setInitParameter("preferDataUrlSchema", "false");
        servletholder.setInitParameter("maxWaitAfterWrite", "-1");
        servletholder.setInitParameter("jsonpEnabled", "true");
        servletholder.setInitParameter("allowScriptTagRemoting", "true");
        servletholder.setInitParameter("crossDomainSessionSecurity", "false");
        servletholder.setInitParameter("overridePath", this.location);
        servletholder.setInitParameter("allowGetForSafariButMakeForgeryEasier", "true");

        context.addServlet(servletholder, "/*");

        try {
            this.server.start();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        this.creatorManager = servlet.getContainer().getBean(CreatorManager.class);
        this.converterManager = servlet.getContainer().getBean(ConverterManager.class);
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
     * JSPF Shutdown hook.
     */
    @Shutdown
    public void shutdown() {
        if (this.server == null) return;
        try {
            this.server.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return .
     */
    @Capabilities
    public String[] getCapabilites() {
        return new String[] { "javascript", "JAVASCRIPT" };
    }

}
