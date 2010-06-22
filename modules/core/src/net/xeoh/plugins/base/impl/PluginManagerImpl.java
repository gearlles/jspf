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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
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
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.PluginInformation.Information;
import net.xeoh.plugins.base.annotations.PluginImplementation;
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
import net.xeoh.plugins.base.impl.metahandling.PluginMetaHandler;
import net.xeoh.plugins.base.impl.metahandling.PluginWrapper;
import net.xeoh.plugins.base.impl.registry.PluggableMetaInformation;
import net.xeoh.plugins.base.impl.registry.PluginRegistry;
import net.xeoh.plugins.base.impl.registry.PluggableMetaInformation.PluginLoadedInformation;
import net.xeoh.plugins.base.impl.registry.PluggableMetaInformation.PluginStatus;
import net.xeoh.plugins.base.options.AddPluginsFromOption;
import net.xeoh.plugins.base.options.GetPluginOption;
import net.xeoh.plugins.base.options.addpluginsfrom.OptionLoadAsynchronously;
import net.xeoh.plugins.base.options.getplugin.OptionCapabilities;
import net.xeoh.plugins.base.options.getplugin.OptionPluginSelector;
import net.xeoh.plugins.base.options.getplugin.PluginSelector;
import net.xeoh.plugins.base.util.OptionUtils;
import net.xeoh.plugins.base.util.PluginConfigurationUtil;
import net.xeoh.plugins.base.util.PluginManagerUtil;
import net.xeoh.plugins.bus.impl.BusImpl;
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

    /** Used for observing access to methods and so on ... */
    private final PluginSupervisorImpl pluginSupervisor;

    /** */
    private final Lock pluginListLock = new ReentrantLock();

    /** */
    private final Lock addPluginLock = new ReentrantLock();

    /** Backreference to ourself */
    private final PluginManagerUtil pluginManagerUtil;

    /** User properties for plugin configuration */
    PluginInformation information;

    /** Loads plugins from various urls */
    private final Collection<AbstractLoader> pluginLoader = new ArrayList<AbstractLoader>();

    /** Manages content cache of jar files */
    //private final OldJARCache jarCache = new OldJARCache();
    private final JARCache jarCache = new JARCache();

    /** */
    private final PluginRegistry pluginRegistry = new PluginRegistry();

    /** Manages the creation of plugins */
    private final Spawner spawner;

    /** Locate classpath elements */
    private final ClassPathLocator classPathLocator;

    /** Classloader used by plugin manager to locate and load plugin classes */
    private final ClassPathManager classPathManager;

    /** Indicates if a shutdown has already been one */
    private boolean shutdownPerformed = false;

    /** If true, plugin meta supervision is enabled */
    private boolean wrapPluginsInMetaProxy = false;

    /**
     * Construct new properties.
     *
     * @param initialProperties
     */
    protected PluginManagerImpl(final Properties initialProperties) {

        // TODO: Is getClass().getClassLoader() okay? ... at least the applet seems to need it, 
        // but will other apps have problems otherwise?
        this.classPathLocator = new ClassPathLocator(this.jarCache);
        this.classPathManager = new ClassPathManager();

        this.pluginManagerUtil = new PluginManagerUtil(this);
        this.spawner = new Spawner(this);

        this.configuration = new PluginConfigurationImpl(initialProperties);
        this.pluginSupervisor = new PluginSupervisorImpl();

        // Needs to be done as early as possible ...
        setupMetaHandling();

        // Hook fundamental plugin
        hookPlugin(new SpawnResult(this, SpawnType.PLUGIN));
        hookPlugin(new SpawnResult(this.configuration, SpawnType.PLUGIN));
        hookPlugin(new SpawnResult(this.pluginSupervisor, SpawnType.PLUGIN));

        // Load the cache file path
        this.cachePath = this.configuration.getConfiguration(PluginManager.class, "cache.file");

        // Register loader
        this.pluginLoader.add(new InternalClasspathLoader(this));
        this.pluginLoader.add(new FileLoader(this));
        this.pluginLoader.add(new HTTPLoader(this));

        loadAdditionalPlugins();
        applyConfig();
    }

    /**
     * Currently this method only support loading from directories and jars (i.e. no
     * network loading)
     *
     * TODO: Implement network loading.
     *
     * FIXME: Use exactly one classloader to load all files from one URL, not several !
     *
     * @see net.xeoh.plugins.base.PluginManager#addPluginsFrom(URI, AddPluginsFromOption...)
     */
    public void addPluginsFrom(final URI url, final AddPluginsFromOption... options) {
        this.logger.fine("Adding plugins from " + url);

        final OptionUtils<AddPluginsFromOption> ou = new OptionUtils<AddPluginsFromOption>(options);

        // Shall we call this asynchoronously?
        if (ou.contains(OptionLoadAsynchronously.class)) {

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

        return;
    }

    /**
     * @param url
     * @param options
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

    /* (non-Javadoc)
     * @see net.xeoh.plugins.base.PluginManager#getPlugin(java.lang.Class, net.xeoh.plugins.base.option.GetPluginOption[])
     */
    @SuppressWarnings( { "unchecked" })
    @RecognizesOption(option = OptionPluginSelector.class)
    public <P extends Plugin> P getPlugin(final Class<P> requestedPlugin,
                                          GetPluginOption... options) {

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

                    // In case we have caps do special handling and don't return the next best plugin
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
     * This implementation simply parses the  collection and, when the
     * current plugin match the given interface, send it to the selector to check who it
     * can be handled. Notice the first instance is always returned
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
                final PluggableMetaInformation metaInformation = this.pluginRegistry.getMetaInformationFor(plugin);
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

    /* (non-Javadoc)
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
     * Apply things from the config
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
     * Load some additionnal and useful plugins
     */
    private void loadAdditionalPlugins() {
        // Remaining core plugins
        hookPlugin(this.spawner.spawnPlugin(BusImpl.class));
        hookPlugin(this.spawner.spawnPlugin(InformationBrokerImpl.class));

        // We need the information plugin in getPlugin, so we can't get it normally.
        this.information = (PluginInformation) this.spawner.spawnPlugin(PluginInformationImpl.class).pluggable;
        ((PluginInformationImpl) this.information).pluginManager = this;
        hookPlugin(new SpawnResult(this.information, SpawnType.PLUGIN));

        // Set all plugins as active we have so far ...
        final Collection<Plugin> allPlugins = this.pluginRegistry.getAllPlugins();
        for (Plugin plugin : allPlugins) {
            this.pluginRegistry.getMetaInformationFor(plugin).pluginStatus = PluginStatus.ACTIVE;
        }
    }

    /**
     * Adds a plugins to the list of known plugins
     * 
     * @param p
     */
    public void hookPlugin(SpawnResult p) {
        // Sanity check
        if (p.spawnType != SpawnType.PLUGIN)
            throw new IllegalStateException("May only hook plugins!");

        this.pluginListLock.lock();
        try {
            Plugin plugin = (Plugin) p.pluggable;

            // Only do something if the plugins is not this
            if (plugin != this && plugin != this.pluginSupervisor) {
                // Wrap plugin inside meta handler, if enabled
                if (this.wrapPluginsInMetaProxy) {

                    final Class<?>[] internalInterfaces = plugin.getClass().getInterfaces();
                    final Class<?>[] interfaces = new Class[internalInterfaces.length + 1];

                    System.arraycopy(internalInterfaces, 0, interfaces, 0, internalInterfaces.length);

                    interfaces[interfaces.length - 1] = PluginWrapper.class;

                    // FIXME (#14): PluginMangerDisablingPlugins gives error when using TestAnnotations at this line. Probably a problem with
                    plugin = (Plugin) Proxy.newProxyInstance(plugin.getClass().getClassLoader(), interfaces, new PluginMetaHandler(this.pluginSupervisor, plugin));
                }
            }

            // 1. Process plugin loaded information
            processPluginLoadedAnnotationForThisPlugin(p);

            // Finally register it.
            this.pluginRegistry.registerPlugin(plugin, p.metaInformation);

            // Process plugin loaded information
            processPluginLoadedAnnotationForOtherPlugins(p);
        } finally {
            this.pluginListLock.unlock();
        }
    }

    private void processPluginLoadedAnnotationForThisPlugin(SpawnResult spawnResult) {
        // Sanity check
        if (spawnResult.spawnType != SpawnType.PLUGIN)
            throw new IllegalStateException("May only process annotations for plugins!");

        // process all annotations of the given plugin
        for (PluginLoadedInformation pli : spawnResult.metaInformation.pluginLoadedInformation) {
            final Collection<? extends Plugin> plins = this.pluginManagerUtil.getPlugins(pli.baseType);

            for (Plugin plugin : plins) {
                try {
                    pli.method.invoke(spawnResult.pluggable, plugin);
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

    private void processPluginLoadedAnnotationForOtherPlugins(SpawnResult spawnResult) {

        for (Plugin plugin : this.pluginRegistry.getAllPlugins()) {
            final PluggableMetaInformation pmi = this.pluginRegistry.getMetaInformationFor(plugin);

            for (PluginLoadedInformation pli : pmi.pluginLoadedInformation) {
                final Collection<? extends Plugin> plins = this.pluginManagerUtil.getPlugins(pli.baseType);

                // Check if the new plugin is returned upon request
                if (plins.contains(spawnResult.pluggable)) {
                    try {
                        pli.method.invoke(plugin, spawnResult.pluggable);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }

                    pli.calledWith.add((Plugin) spawnResult.pluggable);
                }
            }
        }
    }

    /**
     * Check if meta handling should be enabled
     */
    private void setupMetaHandling() {
        final String cfg = this.configuration.getConfiguration(PluginManager.class, "supervision.enabled");
        if (cfg != null && cfg.equals("true")) {
            this.wrapPluginsInMetaProxy = true;
        }
    }

    /**
     * @return .
     */
    public JARCache getJARCache() {
        return this.jarCache;
    }

    /**
     * @return .
     */
    public ClassPathManager getClassPathManager() {
        return this.classPathManager;
    }

    /**
     * @return .
     */
    public PluginRegistry getPluginRegistry() {
        return this.pluginRegistry;
    }

    /**
     * @return .
     */
    public PluginConfiguration getPluginConfiguration() {
        return this.configuration;
    }

    /**
     * @return .
     */
    public Spawner getSpawner() {
        return this.spawner;
    }

    /**
     * @return .
     */
    public ClassPathLocator getClassPathLocator() {
        return this.classPathLocator;
    }
}
