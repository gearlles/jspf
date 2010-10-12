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
package net.xeoh.plugins.remote.impl.json;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
import net.xeoh.plugins.remote.RemoteAPIJSON;
import net.xeoh.plugins.remote.util.internal.PluginExport;
import net.xeoh.plugins.remote.util.vanilla.ExportResultImpl;
import net.xeoh.plugins.remotediscovery.RemoteDiscovery;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.JavaClass;
import org.jabsorb.JSONRPCBridge;
import org.jabsorb.JSONRPCServlet;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

/**
 * Exports plugins to be accessible by JavaScript.  
 * 
 * FIXME: This class leaks memory. The Bridge should be cleared from time to time ...
 * 
 * @author Thomas Lottermann
 */
@Author(name = "Thomas Lottermann")
@PluginImplementation
public class RemoteAPIImpl implements RemoteAPIJSON {

    /** Log events */
    final Logger logger = Logger.getLogger(this.getClass().getName());

    /** */
    @InjectPlugin
    public PluginConfiguration configuration;

    /** Base URL to return (without trailing slash) */
    private String location;

    /** Jetty reference */
    Server server = null;

    /** JSON Bidge */
    JSONRPCBridge bridge = null;

    /** Classes already registered to the bridge */
    Collection<Class<?>> registered = new ArrayList<Class<?>>();

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

        String originalName = PluginExport.getExportName(plugin);
        String className = PluginExport.getHashedName(plugin);
        this.bridge.registerObject(className, plugin);

        // Set to true for powerful exports and memory leaks, or false if you need neither feature.
        if (true) {
            registerCallableRecursively(this.bridge, this.registered, plugin.getClass(), 3);
        }

        // Return browser-compatible URI + className
        URI uri = createURI(this.location + "/" + className);
        this.logger.info("Exported " + originalName + " at " + uri);

        // Announce the plugin
        this.discovery.announcePlugin(plugin, getPublishMethod(), uri);

        return new ExportResultImpl(uri);
    }

    /**
     * Returns all related classes
     * 
     * @param start
     * @return
     */
    private static Collection<Class<?>> getAllRelatedClasses(Class<?> start) {

        List<Class<?>> rval = new ArrayList<Class<?>>();
        JavaClass lookupClass;

        // In case the class fails, return empty.
        try {
            lookupClass = Repository.lookupClass(start);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return rval;
        }

        ConstantPool constantPool = lookupClass.getConstantPool();

        int length = constantPool.getLength();
        for (int i = 0; i < length; i++) {
            Constant constant = constantPool.getConstant(i);
            if (constant instanceof ConstantClass) {
                ConstantClass cc = (ConstantClass) constant;
                ConstantUtf8 constant2 = (ConstantUtf8) constantPool.getConstant(cc.getNameIndex());

                // In case a subclass fails, skip, but print warning.
                try {
                    String toLoad = constant2.getBytes().replace('/', '.');
                    if (toLoad.contains("[")) continue;
                    Class<?> forName = Class.forName(toLoad);
                    rval.add(forName);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

        }

        return rval;
    }

    /**
     * Recursively registers all return types to be accessible, down to the java.lang.Class.
     * By calling this function you ensure a) that all returned objects are accessible 
     * from JavaScript, no matter how nested they are and b) memory leaks. 
     * 
     * @param brdge
     * @param reg
     * @param start
     */
    private static void registerCallableRecursively(JSONRPCBridge brdge,
                                                    Collection<Class<?>> reg,
                                                    Class<?> start, int levelOfRecursion) {

        if (levelOfRecursion == 0) return;

        try {

            Collection<Class<?>> allRelatedClasses = getAllRelatedClasses(start);
            //= start.getDeclaredClasses();
            // Method[] methods = start.getMethods();
            //for (Method method : methods) {
            for (Class<?> returnType : allRelatedClasses) {
                //Class<?> returnType = method.getReturnType();

                if (reg.contains(returnType)) continue;

                // I think these classes are already serialized by JSON, so don't make them accessible. 
                if (returnType.equals(String.class)) continue;
                if (returnType.equals(Void.class)) continue;
                if (returnType.equals(Float.class)) continue;
                if (returnType.equals(Double.class)) continue;
                if (returnType.equals(Integer.class)) continue;
                if (returnType.equals(ArrayList.class)) continue;
                if (returnType.equals(Array.class)) continue;

                reg.add(returnType);
                brdge.registerCallableReference(returnType);

                registerCallableRecursively(brdge, reg, returnType, levelOfRecursion - 1);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remote.RemoteAPI#getPublishMethod()
     */
    public PublishMethod getPublishMethod() {
        return PublishMethod.JSON;
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remote.RemoteAPI#getRemoteProxy(java.net.URL, java.lang.Class)
     */
    public <R extends Plugin> R getRemoteProxy(final URI url, final Class<R> remote) {
        this.logger.warning("JavaScript Plugin was requested to return remote proxy for an JSON instance. This does not work. Sorry.");
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
        String _servername = this.configuration.getConfiguration(RemoteAPI.class, "export.server");
        String servername = _servername == null ? "127.0.0.1" : _servername;

        String _port = this.configuration.getConfiguration(RemoteAPIImpl.class, "export.port");
        int port = getFreePort();
        if (_port != null) {
            port = Integer.parseInt(_port);
        }

        // Set the location and server
        this.location = "http://" + servername + ":" + port;
        this.server = new Server(port);
        this.bridge = new JSONRPCBridge(); //JSONRPCBridge.getGlobalBridge();

        // Create context and our specially hacked jsonrpc servlet.
        final Context context = new Context(this.server, "/", Context.SESSIONS);
        final JSONRPCServlet servlet = new JSONRPCServlet() {
            /**   */
            private static final long serialVersionUID = 7129007024968608285L;

            @Override
            public void service(HttpServletRequest arg0, HttpServletResponse arg1)
                                                                                  throws IOException {

                // Register our global bridge, so jabsorb does not complain about 
                // sessionless globals
                final HttpSession session = arg0.getSession();
                session.setAttribute("JSONRPCBridge", RemoteAPIImpl.this.bridge);

                super.service(arg0, arg1);
            }
        };

        final ServletHolder servletholder = new ServletHolder(servlet);

        servletholder.setInitParameter("gzip_threshold", "200");
        context.addServlet(servletholder, "/*");

        try {
            this.server.start();
        } catch (final Exception e) {
            e.printStackTrace();
        }
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
        return new String[] { "json", "JSON" };
    }

}
