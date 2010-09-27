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
import net.xeoh.plugins.base.util.PluginManagerUtil;
import net.xeoh.plugins.base.util.uri.ClassURI;

/**
 * This is your entry point to and the heart of JSPF. The plugin manager keeps track of all 
 * registed plugins and gives you methods to add and query them. You cannot instantiate the
 * PluginManager directly, instead you<br/><br/>
 * 
 * <center>create this class the first time by a call to <b>PluginManagerFactory.createPluginManager()</b></center>
 * 
 * <br/><br/>
 * Afterwards you probably want to add some of your own plugins. During the lifetime of your 
 * application there should only be one PluginManager. The PluginManager does not have to be 
 * passed to the inside of your plugins, instead, they can request it by the <code>@InjectPlugin</code>
 * annotation (i.e, create a field '<code>public PluginManager manager</code>' and add the 
 * annotation).<br/><br/>
 * 
 * Also have a look at <code>PluginManagerUtils</code>.<br/>
 * 
 * @see PluginManagerUtil
 *
 * @author Ralf Biedert
 */
public interface PluginManager extends Plugin {
    /**
     * Requests the plugin manager to add plugins from a given path. The path can be
     * either a folder-like item where existing .zip and .jar files are trying to be
     * added, as well as existing class files. The path can also be a singular .zip or
     * .jar which is added as well.<br><br>
     *
     * The manager will search for classes having the PluginImplementation annotation and
     * evaluate this annotation. Thereafter the plugin will be instanciated.<br><br>
     * 
     * Currently supported are classpath-folders (containing no .JAR files), plugin folders 
     * (containing .JAR files or multiplugins), single plugins and HTTP locations. Example
     * calls look like this:<br/><br/>
     * 
     * <ul>
     * <li><code>addPluginsFrom(new URI("classpath://*"))</code> (add all plugins within the current classpath).</li>
     * <li><code>addPluginsFrom(new File("plugins/").toURI())</code> (all plugins from the given folder, scanning for JARs and <a href="http://code.google.com/p/jspf/wiki/FAQ">multi-plugins</a>).</li>
     * <li><code>addPluginsFrom(new File("plugin.jar").toURI())</code> (the given plugin directly, no scanning is being done).</li>
     * <li><code>addPluginsFrom(new URI("http://sample.com/plugin.jar"))</code> (downloads and adds the given plugin, use with caution).</li>
     * <li><code>addPluginsFrom(new ClassURI(ServiceImpl.class).toURI())</code> (adds the specific plugin implementation already present in the classpath; very uncomfortable, very fast).</li>
     * </ul>
     *   
     * @see ClassURI
     *
     * @param url The URL to add from. If this is "classpath://*"; the plugin manager will 
     * load all plugins within it's own classpath. 
     * 
     * @param options A set of options supported. Please see the individual options for more
     * details.
     */
    public void addPluginsFrom(URI url, AddPluginsFromOption... options);

    /**
     * Returns the next best plugin for the requested interface. The way the plugin is being 
     * selected is undefined, you should assume that a random plugin implementing the requested 
     * interface is chosen. <br><br>
     * 
     * This method is more powerful than it looks like on first sight, especially in conjunction
     * with the right <code>GetPluginOptions</code>. 
     * 
     * @param <P> Type of the plugin / return value. 
     *
     * @param plugin The interface to request. The given class <b>must</b> derive from Plugin. You <b>MUST NOT</b> pass 
     * implementation classes. Only interface classes are accepted (i.e. <code>getPlugin(Service.class)</code>
     * is fine, while <code>getPlugin(ServiceImpl.class)</code> isn't. 
     * @param options A set of options for the request.
     *
     * @return A randomly chosen Object that implements <code>plugin</code>.
     */
    public <P extends Plugin> P getPlugin(Class<P> plugin, GetPluginOption... options);

    /**
     * Tells the plugin manager to shut down. This may be useful in cases where you want all 
     * created plugins to be destroyed and shutdown hooks called. Normally this happens during 
     * application termination automatically, but sometimes you create a 2nd instance in the same 
     * machine and want the first one to close properly.   
     * 
     * All invocations after the first one have no effect.
     */
    public void shutdown();

}
