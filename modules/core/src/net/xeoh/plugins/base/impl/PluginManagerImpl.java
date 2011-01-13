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
package net.xeoh.plugins.base.impl;

import static net.jcores.CoreKeeper.$;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Logger;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginConfiguration;
import net.xeoh.plugins.base.PluginInformation;
import net.xeoh.plugins.base.PluginInformation.Information;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.meta.Author;
import net.xeoh.plugins.base.annotations.meta.RecognizesOption;
import net.xeoh.plugins.base.annotations.meta.Version;
import net.xeoh.plugins.base.impl.classpath.ClassPathManager;
import net.xeoh.plugins.base.impl.registry.PluginMetaInformation;
import net.xeoh.plugins.base.impl.registry.PluginMetaInformation.PluginStatus;
import net.xeoh.plugins.base.impl.registry.PluginRegistry;
import net.xeoh.plugins.base.impl.spawning.SpawnResult;
import net.xeoh.plugins.base.impl.spawning.Spawner;
import net.xeoh.plugins.base.options.AddPluginsFromOption;
import net.xeoh.plugins.base.options.GetPluginOption;
import net.xeoh.plugins.base.options.addpluginsfrom.OptionReportAfter;
import net.xeoh.plugins.base.options.getplugin.OptionCapabilities;
import net.xeoh.plugins.base.options.getplugin.OptionPluginSelector;
import net.xeoh.plugins.base.options.getplugin.PluginSelector;
import net.xeoh.plugins.base.util.OptionUtils;
import net.xeoh.plugins.base.util.PluginConfigurationUtil;
import net.xeoh.plugins.informationbroker.impl.InformationBrokerImpl;

/**
 * In implementation of the PluginManager interface. <br>
 * 
 * @author Ralf Biedert
 */
@PluginImplementation
@Version(version = 1 * Version.UNIT_MAJOR)
@Author(name = "Ralf Biedert")
public class PluginManagerImpl implements PluginManager {
    /** For debugging. */
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    /** User properties for plugin configuration */
    private final PluginConfiguration configuration;

    /** The main container for plugins and plugin information */
    private final PluginRegistry pluginRegistry = new PluginRegistry();

    /** Classloader used by plugin manager to locate and load plugin classes */
    private final ClassPathManager classPathManager;

    /** Manages the creation of plugins */
    private final Spawner spawner;

    /** Indicates if a shutdown has already been one */
    private boolean shutdownPerformed = false;

    /** User properties for plugin configuration */
    PluginInformation information;

    /**
     * Construct new properties.
     * 
     * @param initialProperties
     */
    protected PluginManagerImpl(final Properties initialProperties) {
        // Create helper classes and config (needed early)
        this.spawner = new Spawner(this);
        this.classPathManager = new ClassPathManager(this);
        this.configuration = new PluginConfigurationImpl(initialProperties);

        // Hook fundamental plugins
        hookPlugin(new SpawnResult(this));
        hookPlugin(new SpawnResult(this.configuration));

        // Load the rest
        loadAdditionalPlugins();
        applyConfig();
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.xeoh.plugins.base.PluginManager#addPluginsFrom(java.net.URI,
     * net.xeoh.plugins.base.options.AddPluginsFromOption[])
     */
    public void addPluginsFrom(final URI url, final AddPluginsFromOption... options) {
        this.logger.fine("Adding plugins from " + url);
        

        // Add from the given location
        if (!this.classPathManager.addFromLocation(url)) {
            this.logger.severe("Unable to add elements, as method is unimplemented for that target : " + url);
        }

        // Check if we should print a report?
        if ($(options).get(OptionReportAfter.class, null) != null)
            this.pluginRegistry.report();

        return;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.xeoh.plugins.base.PluginManager#getPlugin(java.lang.Class,
     * net.xeoh.plugins.base.option.GetPluginOption[])
     */
    @SuppressWarnings({ "unchecked" })
    @RecognizesOption(option = OptionPluginSelector.class)
    public <P extends Plugin> P getPlugin(final Class<P> requestedPlugin,
                                          GetPluginOption... options) {
        // Sanity check.
        if (!requestedPlugin.isInterface()) {
            System.err.println("YOU MUST NOT call getPlugin() with a concrete class; only interfaces are");
            System.err.println("supported for lookup. This means do not call getPlugin(MyPluginImpl.class),");
            System.err.println("but rather getPlugin(MyPlugin.class)!");

            this.logger.warning("YOU MUST NOT call getPlugin() with a concrete class; only interfaces are supported for lookup.");
            return null;
        }

        // Used to process the options
        final OptionUtils<GetPluginOption> ou = new OptionUtils<GetPluginOption>(options);

        // We use this one to select the plugin
        PluginSelector<P> pluginSelector = null;

        // Check our options. In case we have a plugin selector, only use the selector
        if (ou.contains(OptionPluginSelector.class)) {
            pluginSelector = ou.get(OptionPluginSelector.class).getSelector();
        } else {
            // Capabilites we require
            final String capabilites[] = ou.get(OptionCapabilities.class, new OptionCapabilities()).getCapabilities();

            // Get caps as list
            final Collection<String> caps = Arrays.asList(capabilites);

            // Create our own selector
            pluginSelector = new PluginSelector<P>() {
                public boolean selectPlugin(final Plugin plugin) {

                    // In case we have caps do special handling and don't return the next
                    // best plugin
                    if (caps.size() > 0) {
                        Collection<String> pcaps = PluginManagerImpl.this.information.getInformation(Information.CAPABILITIES, plugin);

                        // Check the plugin has them all
                        if (pcaps.containsAll(caps)) return true;
                        return false;
                    }

                    return true;
                }
            };
        }

        // Check for each plugin if it matches
        for (final Plugin plugin : this.pluginRegistry.getAllPlugins()) {

            // Check the meta information for this plugin. We only want active classes
            final PluginMetaInformation metaInformation = this.pluginRegistry.getMetaInformationFor(plugin);

            // Plugins not active are not considered
            if (metaInformation.pluginStatus != PluginStatus.ACTIVE) continue;

            // Check if the plugin can be assigned to the requested class
            if (requestedPlugin.isAssignableFrom(plugin.getClass())) {
                if (pluginSelector.selectPlugin((P) plugin)) return (P) plugin;
            }
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.xeoh.plugins.base.PluginManager#shutdown()
     */
    public void shutdown() {
        this.logger.fine("Dumping shutdown cause.");
        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (StackTraceElement se : stackTrace) {
                this.logger.finer(se.getClassName() + "." + se.getMethodName() + ":" + se.getLineNumber());
            }
        } catch (Exception e) {
            this.logger.fine("Error generating shutdown strack trace");
        }

        // Only execute this method a single time.
        if (this.shutdownPerformed) return;

        // Destroy plugins in a random order
        for (final Plugin p : this.pluginRegistry.getAllPlugins()) {
            this.spawner.destroyPlugin(p, this.pluginRegistry.getMetaInformationFor(p));
        }

        // Curtains down, lights out.
        this.pluginRegistry.clear();
        this.shutdownPerformed = true;
    }

    /**
     * Apply things from the config.
     */
    @SuppressWarnings("boxing")
    private void applyConfig() {
        final PluginConfigurationUtil pcu = new PluginConfigurationUtil(this.configuration);

        final String cachePath = this.configuration.getConfiguration(PluginManager.class, "cache.file");

        this.classPathManager.getCache().setEnabled(pcu.getBoolean(PluginManager.class, "cache.enabled", false));
        this.classPathManager.getCache().setCachePath(cachePath);

        // Check if we should enable weak mode
        final String mode = pcu.getString(PluginManager.class, "cache.mode", "strong");
        if (mode.equals("weak")) {
            this.classPathManager.getCache().setWeakMode(true);
        }

    }

    /**
     * Load some additional plugins.
     */
    private void loadAdditionalPlugins() {
        // Remaining core plugins
        hookPlugin(this.spawner.spawnPlugin(InformationBrokerImpl.class));

        // We need the information plugin in getPlugin, so we can't get it normally.
        this.information = (PluginInformation) this.spawner.spawnPlugin(PluginInformationImpl.class).plugin;
        ((PluginInformationImpl) this.information).pluginManager = this;
        hookPlugin(new SpawnResult(this.information));

        // Set all plugins as active we have so far ...
        final Collection<Plugin> allPlugins = this.pluginRegistry.getAllPlugins();
        for (Plugin plugin : allPlugins) {
            this.pluginRegistry.getMetaInformationFor(plugin).pluginStatus = PluginStatus.ACTIVE;
        }
    }

    /**
     * Adds a plugins to the list of known plugins and performs late initialization and
     * processing.
     * 
     * @param p The SpawnResult to hook.
     */
    public void hookPlugin(SpawnResult p) {
        // 1. Process plugin @PluginLoaded annotation for this plugins. TODO: Why was
        // this process split? Can't we just do everything in one method before or
        // after the plugins was registered?
        this.spawner.processThisPluginLoadedAnnotation(p.plugin, p.metaInformation);

        // Finally register it.
        this.pluginRegistry.registerPlugin(p.plugin, p.metaInformation);

        // Process plugin loaded information
        this.spawner.processOtherPluginLoadedAnnotation(p.plugin);
    }

    /**
     * Returns the ClassPathManger handling our plugin sources.
     * 
     * @return The PluginManager.
     */
    public ClassPathManager getClassPathManager() {
        return this.classPathManager;
    }

    /**
     * Returns the PluginRegistry, keeping track of loaded plugins.
     * 
     * @return The PluginRegistry.
     */
    public PluginRegistry getPluginRegistry() {
        return this.pluginRegistry;
    }

    /**
     * Returns the PluginConfiguration handling application setup.
     * 
     * @return Returns the plugin configuration.
     */
    public PluginConfiguration getPluginConfiguration() {
        return this.configuration;
    }

    /**
     * Returns the main spawner to instantiate plugins.
     * 
     * @return The Spawner.
     */
    public Spawner getSpawner() {
        return this.spawner;
    }
}
