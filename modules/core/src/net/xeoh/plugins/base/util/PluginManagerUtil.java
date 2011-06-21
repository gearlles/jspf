/*
 * BusImpl.java
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
package net.xeoh.plugins.base.util;

import static net.jcores.CoreKeeper.$;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.options.AddPluginsFromOption;
import net.xeoh.plugins.base.options.GetPluginOption;
import net.xeoh.plugins.base.options.getplugin.OptionCapabilities;
import net.xeoh.plugins.base.options.getplugin.OptionPluginSelector;
import net.xeoh.plugins.base.options.getplugin.PluginSelector;

/**
 * Helper functions for {@link PluginManager} interface. The util uses the embedded 
 * interface to provide more convenience features.   
 *
 * @author Ralf Biedert
 * @see PluginManager
 * @since 1.0
 */
public class PluginManagerUtil extends VanillaPluginUtil<PluginManager> implements PluginManager {


    /**
     * Creates a new util for the given interface.
     * 
     * @param pm The interface to create the utils for.
     */
    public PluginManagerUtil(PluginManager pm) {
        super(pm);
    }

    /**
     * Returns all plugins implementing the given interface, not just the first, 
     * 'random' match. Use this method if you want to list  the registed plugins (or 
     * select from them on your own). For example, to get all plugins implementing the 
     * <code>Chat</code> interface, write:<br/><br/>
     * 
     * <code>
     * getPlugins(Chat.class);
     * </code>
     * 
     * @param <P> Type of the requested plugin.
     * @param plugin The interface to request.
     * @see OptionPluginSelector 
     * @return A collection of all plugins implementing the given interface.
     */
    public <P extends Plugin> Collection<P> getPlugins(final Class<P> plugin) {
        return getPlugins(plugin, new PluginSelector<P>() {

            public boolean selectPlugin(final P p) {
                return true;
            }
        });
    }

    /**
     * Returns all plugins. Use this method if you want to list all registed plugins.
     * 
     * @see OptionPluginSelector 
     * @return A collection of all plugins implementing the given interface.
     */
    public Collection<Plugin> getPlugins() {
        return getPlugins(Plugin.class);
    }
    
    /**
     * Returns all interfaces implementing the given interface AND satisfying the 
     * given plugin selector. Use this method if you want to list some of the 
     * registed plugins (or select from them on your own). 
     * 
     * @param <P> Type of the requested plugin.
     * @param plugin The interface to request. 
     * @param selector The selector will be called for each available plugin. When 
     * it returns <code>true</code> the plugin will be added to the return value.
     * @see OptionPluginSelector  
     * @return A collection of plugins for which the collector return true.
     */
    public <P extends Plugin> Collection<P> getPlugins(final Class<P> plugin,
                                                       final PluginSelector<P> selector) {
        final Collection<P> allPlugins = new ArrayList<P>();

        this.object.getPlugin(plugin, new OptionPluginSelector<P>(new PluginSelector<P>() {

            public boolean selectPlugin(final P p) {
                if (selector.selectPlugin(p)) {
                    allPlugins.add(p);
                }
                return false;
            }
        }));

        return allPlugins;
    }
    
    /**
     * Returns the next best plugin implementing the requested interface and fulfilling
     * all capabilities specified.
     * 
     * @since 1.0.3
     * @param <P> Type of the requested plugin.
     * @param plugin The interface to request. 
     * @param cap1 The first capability to consider.
     * @param caps The other, optional, capabilities to consider.
     * @see OptionCapabilities  
     * @return A collection of plugins for which the collector return true.
     */
    public <P extends Plugin> P getPlugin(Class<P> plugin, String cap1, String... caps) {
        return this.object.getPlugin(plugin, new OptionCapabilities($(cap1).add(caps).unsafearray()));
    }

    
    /* (non-Javadoc)
     * @see net.xeoh.plugins.base.PluginManager#addPluginsFrom(java.net.URI, net.xeoh.plugins.base.options.AddPluginsFromOption[])
     */
    @Override
    public PluginManagerUtil addPluginsFrom(URI url, AddPluginsFromOption... options) {
        this.object.addPluginsFrom(url, options);
        return this;
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.base.PluginManager#getPlugin(java.lang.Class, net.xeoh.plugins.base.options.GetPluginOption[])
     */
    @Override
    public <P extends Plugin> P getPlugin(Class<P> plugin, GetPluginOption... options) {
        return this.object.getPlugin(plugin, options);
    }
    
    /* (non-Javadoc)
     * @see net.xeoh.plugins.base.PluginManager#shutdown()
     */
    @Override
    public void shutdown() {
        this.object.shutdown();
    }
}
