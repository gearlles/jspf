/*
 * PluginManager.java
 *
 * Copyright (c) 2007, Ralf Biedert All rights reserved.
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
package net.xeoh.plugins.base;

import java.net.URI;

import net.xeoh.plugins.base.options.AddPluginsFromOption;
import net.xeoh.plugins.base.options.GetPluginOption;
import net.xeoh.plugins.base.util.uri.ClassURI;

/**
 * Functionality for the plugin manager. This is your (indirect) entry point for the
 * plugin framework. Obtain this class by a corresponding factory-method, for example the
 * one provided in the 'impl' sub-package. This should be the only time in your
 * (application) life to access a file from an impl directory directly. <br>
 * <br>
 * <center>Create this class the first time by a call to
 * <b>PluginManagerFactory.createPluginManager()</b></center> <br>
 * <br>
 *
 * TODO: Add plugin selection mechanisms <br>
 * <br>
 * TODO: Add plugin removal mechanism.<br>
 *
 * @author Ralf Biedert
 */
public interface PluginManager extends Plugin {
    /**
     * Requests the plugin manager to add plugins from a given path. The path can be
     * either a folder-like item where existing .zip and .jar files are trying to be
     * added, as well as existing class files. The path can also be a singular .zip or
     * .jar which is added as well.<br>
     * <br>
     *
     * The manager will search for classes having the PluginImplementation annotation and
     * evaluate this annotation. Thereafter the plugin will be instanciated.<br>
     * <br>
     *   
     * @see ClassURI
     *
     * @param url
     *                The URL to add from. If this is "classpath://*"; the plugin manager will load all plugins within it's 
     *                own classpath
     * 
     * @param options 
     */
    public void addPluginsFrom(URI url, AddPluginsFromOption... options);

    /**
     * Returns the next best plugin for the requested functionality. The plugin will be randomly
     * chosen from all plugins that implement the requested functionality. This function
     * is the same as the one specified below when randomly returning true.
     *
     * @param
     * <P>
     *
     *
     * @param plugin
     *                The interface to request. <b>Must</b> derive from Plugin.
     * @param options 
     *
     * @return A randomly chosen Object that implements <code>plugin</code>.
     */
    public <P extends Plugin> P getPlugin(Class<P> plugin, GetPluginOption... options);

    /**
     * Tells the plugin manager to shut down. This may be useful in cases where you want all created plugins to be destroyed 
     * and shutdown hooks called. Normally this happens during application termination automatically, but sometimes you create a 
     * 2nd instance in the same machine and want the first one to close properly.   
     * 
     * All invocations after this first on this method have no effect.
     */
    public void shutdown();

}
