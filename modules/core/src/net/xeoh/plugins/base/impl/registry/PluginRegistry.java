/*
 * PluginRegistry.java
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
package net.xeoh.plugins.base.impl.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.impl.registry.PluggableClassMetaInformation.PluginClassStatus;

/**
 * Manges plugin classes and the plugins
 * 
 * @author Ralf Biedert
 */
public class PluginRegistry {

    /** Stores meta information related to a plugin */
    private final Map<Plugin, PluggableMetaInformation> pluginMetaInformation;

    /** Stores meta information related to a plugin class */
    private final Map<Class<? extends Plugin>, PluggableClassMetaInformation> pluginClassMetaInformation;

    /** 
     * Creates a new registry 
     */
    public PluginRegistry() {
        this.pluginMetaInformation = new HashMap<Plugin, PluggableMetaInformation>();
        this.pluginClassMetaInformation = new HashMap<Class<? extends Plugin>, PluggableClassMetaInformation>();
    }

    /**
     * Returns all plugins, regardless of their status
     * 
     * @return .
     */
    public Collection<Plugin> getAllPlugins() {
        return this.pluginMetaInformation.keySet();
    }

    /**
     * Returns the metainfromation of a plugin
     * 
     * @param plugin
     * @return .
     */
    public PluggableMetaInformation getMetaInformationFor(Plugin plugin) {
        return this.pluginMetaInformation.get(plugin);
    }

    /**
     * Returns the metainfromation of a pluginclass
     * 
     * @param clazz
     * @return .
     */
    public PluggableClassMetaInformation getMetaInformationFor(
                                                               Class<? extends Plugin> clazz) {
        return this.pluginClassMetaInformation.get(clazz);
    }

    /**
     * Remove all plugin references
     */
    public void clear() {
        this.pluginClassMetaInformation.clear();
        this.pluginMetaInformation.clear();
    }

    /**
     * Registers a plugin with the given meta information
     * 
     * @param plugin
     * @param metaInformation
     */
    public void registerPlugin(Plugin plugin, PluggableMetaInformation metaInformation) {
        this.pluginMetaInformation.put(plugin, metaInformation);
    }

    /**
     * @param c
     * @param metaInformation 
     */
    public void registerPluginClass(Class<? extends Plugin> c,
                                    PluggableClassMetaInformation metaInformation) {
        this.pluginClassMetaInformation.put(c, metaInformation);
    }

    /**
     * @param status
     * @return .
     */
    public Collection<Class<? extends Plugin>> getPluginClassesWithStatus(
                                                                          PluginClassStatus status) {
        final List<Class<? extends Plugin>> rval = new ArrayList<Class<? extends Plugin>>();
        final Set<Class<? extends Plugin>> keySet = this.pluginClassMetaInformation.keySet();

        for (Class<? extends Plugin> class1 : keySet) {
            final PluggableClassMetaInformation metaInformation = this.pluginClassMetaInformation.get(class1);

            if (metaInformation.pluginClassStatus == status) rval.add(class1);
        }

        return rval;
    }

}
