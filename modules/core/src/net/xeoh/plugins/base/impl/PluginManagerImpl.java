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

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginConfiguration;
import net.xeoh.plugins.base.PluginInformation;
import net.xeoh.plugins.base.PluginInformation.Information;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.PluginLoaded;
import net.xeoh.plugins.base.annotations.meta.Author;
import net.xeoh.plugins.base.annotations.meta.RecognizesOption;
import net.xeoh.plugins.base.annotations.meta.Version;
import net.xeoh.plugins.base.impl.SpawnResult.SpawnType;
import net.xeoh.plugins.base.impl.classpath.ClassPathManager;
import net.xeoh.plugins.base.impl.classpath.cache.JARCache;
import net.xeoh.plugins.base.impl.classpath.locator.ClassPathLocator;
import net.xeoh.plugins.base.impl.loader.AbstractLoader;
import net.xeoh.plugins.base.impl.loader.FileLoader;
import net.xeoh.plugins.base.impl.loader.HTTPLoader;
import net.xeoh.plugins.base.impl.loader.InternalClasspathLoader;
import net.xeoh.plugins.base.impl.registry.PluginMetaInformation;
import net.xeoh.plugins.base.impl.registry.PluginMetaInformation.PluginLoadedInformation;
import net.xeoh.plugins.base.impl.registry.PluginMetaInformation.PluginStatus;
import net.xeoh.plugins.base.impl.registry.PluginRegistry;
import net.xeoh.plugins.base.options.AddPluginsFromOption;
import net.xeoh.plugins.base.options.GetPluginOption;
import net.xeoh.plugins.base.options.addpluginsfrom.OptionLoadAsynchronously;
import net.xeoh.plugins.base.options.addpluginsfrom.OptionReportAfter;
import net.xeoh.plugins.base.options.getplugin.OptionCapabilities;
import net.xeoh.plugins.base.options.getplugin.OptionPluginSelector;
import net.xeoh.plugins.base.options.getplugin.PluginSelector;
import net.xeoh.plugins.base.util.OptionUtils;
import net.xeoh.plugins.base.util.PluginConfigurationUtil;
import net.xeoh.plugins.base.util.PluginManagerUtil;
import net.xeoh.plugins.informationbroker.impl.InformationBrokerImpl;

/**
 * In implementation of the PluginManager interface. <br>
 * <br>
 * TODO: Use logging more extensively. <br>
 * <br>
 * TODO: Make it use only a single classloader.
 * 
 * @author Ralf Biedert
 */
@PluginImplementation
@Version(version = 1 * Version.UNIT_MAJOR)
@Author(name = "Ralf Biedert")
public class PluginManagerImpl implements PluginManager {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    /** Where to save the cache file */
    private final String cachePath;

    /** User properties for plugin configuration */
    private final PluginConfiguration configuration;

    /** Blocks access to the list of plugins in case of multiple threads. */
    private final Lock pluginListLock = new ReentrantLock();

    /** Blocks access to ??? */
    private final Lock addPluginLock = new ReentrantLock();

    /** Backreference to ourself */
    private final PluginManagerUtil pluginManagerUtil;

    /** User properties for plugin configuration */
    PluginInformation information;

    /** Loads plugins from various urls */
    private final Collection<AbstractLoader> pluginLoader = new ArrayList<AbstractLoader>();

    /** Manages content cache of jar files */
    // private final OldJARCache jarCache = new OldJARCache();
    private final JARCache jarCache = new JARCache();

    /** The main container for plugins and plugin information */
    private final PluginRegistry pluginRegistry = new PluginRegistry();

    /** Manages the creation of plugins */
    private final Spawner spawner;

    /** Locate classpath elements */
    private final ClassPathLocator classPathLocator;

    /** Classloader used by plugin manager to locate and load plugin classes */
    private final ClassPathManager classPathManager;

    /** Indicates if a shutdown has already been one */
    private boolean shutdownPerformed = false;

    /**
     * Construct new properties.
     * 
     * @param initialProperties
     */
    protected PluginManagerImpl(final Properties initialProperties) {

        // TODO: Is getClass().getClassLoader() okay? ... at least the applet seems to
        // need it,
        // but will other apps have problems otherwise?
        this.classPathLocator = new ClassPathLocator(this.jarCache);
        this.classPathManager = new ClassPathManager();

        this.pluginManagerUtil = new PluginManagerUtil(this);
        this.spawner = new Spawner(this);

        this.configuration = new PluginConfigurationImpl(initialProperties);

        // Hook fundamental plugins
        hookPlugin(new SpawnResult(this, SpawnType.PLUGIN));
        hookPlugin(new SpawnResult(this.configuration, SpawnType.PLUGIN));

        // Load the cache file path
        this.cachePath = this.configuration.getConfiguration(PluginManager.class, "cache.file");

        // Register loader
        this.pluginLoader.add(new InternalClasspathLoader(this));
        this.pluginLoader.add(new FileLoader(this));
        this.pluginLoader.add(new HTTPLoader(this));

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

        // Shall we call this asynchronously? TODO: Is this needed?
        if ($(options).get(OptionLoadAsynchronously.class, null) != null) {

            Thread t = new Thread(new Runnable() {
                public void run() {
                    // ... and run the task
                    doAddPluginsFrom(url, options);
                }
            });
            t.setDaemon(true);
            t.start();

            return;
        }

        // Just handle the call normally.
        doAddPluginsFrom(url, options);

        // Check if we should print a report?
        if ($(options).get(OptionReportAfter.class, null) != null)
            this.pluginRegistry.report();

        return;
    }

    /**
     * Actually adds plugins from a given URL.
     * 
     * @param url The url to load plugins from.
     * @param options Additional options.
     */
    void doAddPluginsFrom(final URI url, AddPluginsFromOption... options) {
        this.addPluginLock.lock();
        try {
            // Load local cache
            this.jarCache.loadCache(this.cachePath);

            // Handle URI
            for (AbstractLoader loader : this.pluginLoader) {
                if (!loader.handlesURI(url)) continue;
                loader.loadFrom(url);
                return;
            }

        } finally {
            this.jarCache.saveCache(this.cachePath);
            this.addPluginLock.unlock();
        }

        this.logger.severe("Unable to add elements, as method is unimplemented for that target : " + url);
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

        // This is the plugin to return
        P plugin = getPlugin(requestedPlugin, pluginSelector);

        return plugin;
    }

    /**
     * This implementation simply processes the collection and, when the
     * current plugin match the given interface, send it to the selector to check if it
     * is what the selector is looking for.
     * 
     * @see net.xeoh.plugins.base.PluginManager#getPlugin(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    protected <P extends Plugin> P getPlugin(final Class<P> requestedPlugin,
                                             final PluginSelector<P> selector) {
        try {
            this.pluginListLock.lock();

            for (final Plugin plugin : this.pluginRegistry.getAllPlugins()) {
                // Check the meta information for this plugin. We only want active classes
                final PluginMetaInformation metaInformation = this.pluginRegistry.getMetaInformationFor(plugin);
                if (metaInformation.pluginStatus != PluginStatus.ACTIVE) continue;

                // Check if the plugin can be assigned to the requested class
                if (requestedPlugin.isAssignableFrom(plugin.getClass())) {
                    if (selector.selectPlugin((P) plugin)) return (P) plugin;
                }
            }

            return null;
        } finally {
            this.pluginListLock.unlock();
        }
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

        // TODO: This looks ugly (the whole code below)
        boolean lockA = false;
        boolean lockB = false;

        try {
            // It is more important to shutdown, than to get the lock properly. (Really?)
            lockA = this.pluginListLock.tryLock(500, TimeUnit.MILLISECONDS);
            lockB = this.addPluginLock.tryLock(500, TimeUnit.MILLISECONDS);

            // Only execute this method a single time.
            if (this.shutdownPerformed) return;

            // Destroy plugins in a random order
            for (final Plugin p : this.pluginRegistry.getAllPlugins()) {
                this.spawner.destroyPluggable(p, this.pluginRegistry.getMetaInformationFor(p));
            }

            // Curtains down, lights out.
            this.pluginRegistry.clear();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.shutdownPerformed = true;
            if (lockA) this.pluginListLock.unlock();
            if (lockB) this.addPluginLock.unlock();

        }
    }

    /**
     * Apply things from the config.
     */
    @SuppressWarnings("boxing")
    private void applyConfig() {
        final PluginConfigurationUtil pcu = new PluginConfigurationUtil(this.configuration);

        this.jarCache.setEnabled(pcu.getBoolean(PluginManager.class, "cache.enabled", false));

        // Check if we should enable weak mode
        final String mode = pcu.getString(PluginManager.class, "cache.mode", "strong");
        if (mode.equals("weak")) {
            this.jarCache.setWeakMode(true);
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
        hookPlugin(new SpawnResult(this.information, SpawnType.PLUGIN));

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
        // Sanity check
        if (p.spawnType != SpawnType.PLUGIN)
            throw new IllegalStateException("May only hook plugins!");

        this.pluginListLock.lock();
        try {
            Plugin plugin = (Plugin) p.plugin;

            // 1. Process plugin @PluginLoaded annotation for this plugins. TODO: Why was
            // this process split? Can't we just do everything in one method before or
            // after the plugins was registered?
            processPluginLoadedAnnotationForThisPlugin(p);

            // Finally register it.
            this.pluginRegistry.registerPlugin(plugin, p.metaInformation);

            // Process plugin loaded information
            processPluginLoadedAnnotationForOtherPlugins(p);
        } finally {
            this.pluginListLock.unlock();
        }
    }

    /**
     * Processes the {@link PluginLoaded} annotation inside this for all other plugins we
     * know.
     * 
     * @param spawnResult The SpawnResult containing the newly created plugin.
     */
    private void processPluginLoadedAnnotationForThisPlugin(SpawnResult spawnResult) {
        // Sanity check
        if (spawnResult.spawnType != SpawnType.PLUGIN)
            throw new IllegalStateException("May only process annotations for plugins!");

        // process all annotations of the given plugin
        for (PluginLoadedInformation pli : spawnResult.metaInformation.pluginLoadedInformation) {
            final Collection<? extends Plugin> plins = this.pluginManagerUtil.getPlugins(pli.baseType);

            for (Plugin plugin : plins) {
                try {
                    pli.method.invoke(spawnResult.plugin, plugin);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

            pli.calledWith.addAll(plins);
        }
    }

    /**
     * Processes the {@link PluginLoaded} annotation for other plugins for this plugin.
     * 
     * @param spawnResult The SpawnResult for the newly created plugin.
     */
    private void processPluginLoadedAnnotationForOtherPlugins(SpawnResult spawnResult) {

        for (Plugin plugin : this.pluginRegistry.getAllPlugins()) {
            final PluginMetaInformation pmi = this.pluginRegistry.getMetaInformationFor(plugin);

            for (PluginLoadedInformation pli : pmi.pluginLoadedInformation) {
                final Collection<? extends Plugin> plins = this.pluginManagerUtil.getPlugins(pli.baseType);

                // Check if the new plugin is returned upon request
                if (plins.contains(spawnResult.plugin)) {
                    try {
                        pli.method.invoke(plugin, spawnResult.plugin);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }

                    pli.calledWith.add((Plugin) spawnResult.plugin);
                }
            }
        }
    }

    /**
     * Returns the JAR cache, containing plugins infos for JARs.
     * 
     * @return The JARCache.
     */
    public JARCache getJARCache() {
        return this.jarCache;
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

    /**
     * Returns the locator for URIs.
     * 
     * @return The locator.
     */
    public ClassPathLocator getClassPathLocator() {
        return this.classPathLocator;
    }
}
