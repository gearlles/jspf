/*
 * Spawner.java
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
package net.xeoh.plugins.base.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TimerTask;
import java.util.logging.Logger;

import net.xeoh.plugins.base.Pluggable;
import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.Pluglet;
import net.xeoh.plugins.base.annotations.Thread;
import net.xeoh.plugins.base.annotations.Timer;
import net.xeoh.plugins.base.annotations.events.Init;
import net.xeoh.plugins.base.annotations.events.PluginLoaded;
import net.xeoh.plugins.base.annotations.events.Shutdown;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;
import net.xeoh.plugins.base.impl.SpawnResult.SpawnType;
import net.xeoh.plugins.base.impl.registry.PluggableClassMetaInformation.Dependency;
import net.xeoh.plugins.base.impl.registry.PluggableMetaInformation;
import net.xeoh.plugins.base.impl.registry.PluggableMetaInformation.PluginLoadedInformation;
import net.xeoh.plugins.base.impl.registry.PluggableMetaInformation.PluginStatus;
import net.xeoh.plugins.base.options.getplugin.OptionCapabilities;

/**
 * Spawn a given class
 * 
 * @author rb
 */
public class Spawner {

    final Logger logger = Logger.getLogger(this.getClass().getName());

    /** Main plugin manager */
    private final PluginManagerImpl pluginManagerImpl;

    /**
     * @param pmi
     */
    public Spawner(final PluginManagerImpl pmi) {
        this.pluginManagerImpl = pmi;
    }

    /**
     * Destroys a given plugin, halt all timers and threads, calls shutdown methods.
     * 
     * @param plugin
     * @param metaInformation 
     */
    public void destroyPluggable(final Plugin plugin,
                                 final PluggableMetaInformation metaInformation) {

        // Halt all timer tasks
        for (final TimerTask timerTask : metaInformation.timerTasks) {
            timerTask.cancel();
        }

        // Halt all timers
        for (final java.util.Timer timer : metaInformation.timers) {
            timer.cancel();
        }

        // Halt all threads
        for (final java.lang.Thread thread : metaInformation.threads) {
            // TODO: Maybe not the best way to terminate.
            try {
                thread.interrupt();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        // Call shutdown hooks
        callShutdownMethods(plugin);
    }

    /**
     * Spawn a plugin and process its internal annotations.
     *
     * @param c
     *                Class to spawn from.
     * @return .
     */
    @SuppressWarnings("rawtypes")
    public SpawnResult spawnPlugin(final Class c) {
        return spawnPluggable(c, false);
    }

    /**
     * Spawn a plugin and process its internal annotations.
     *
     * @param c
     *                Class to spawn from.
     * @param lazySpawn 
     * @return .
     */
    @SuppressWarnings("rawtypes")
    public SpawnResult spawnPluggable(final Class c, boolean lazySpawn) {

        // Used for time measurements.
        final long startTime = System.nanoTime();
        final java.util.Timer timer = new java.util.Timer();
        final TimerTask lateMessage = new TimerTask() {
            @Override
            public void run() {
                Spawner.this.logger.fine("Class " + c + " takes very long to spawn. You should fix that.");
            }
        };

        // Finally load and register plugin
        try {
            // Schedule late message. (TODO: Make this configurable)
            timer.schedule(lateMessage, 250);

            // Instanciate the plugin
            final Pluggable spawnedPlugin = (Pluggable) c.newInstance();

            // Detect type 
            SpawnType type = null;

            if (Pluglet.class.isAssignableFrom(c)) {
                type = SpawnType.PLUGLET;
            }

            if (Plugin.class.isAssignableFrom(c)) {
                type = SpawnType.PLUGIN;
            }

            // In here spawning of the plugin worked
            final SpawnResult spawnResult = new SpawnResult(spawnedPlugin, type);
            spawnResult.metaInformation.pluginStatus = PluginStatus.SPAWNED;
            spawnResult.metaInformation.spawnTime = System.currentTimeMillis();

            // If we spawn lazily, this is enough for the beginning
            if (lazySpawn) return spawnResult;

            return continueSpawn(spawnResult);
        } catch (final Throwable e) {
            this.logger.warning("Unable to load plugin " + c.getName());
            this.logger.warning(e.toString());
            e.printStackTrace();
            Throwable cause = e.getCause();
            while (cause != null) {
                cause.printStackTrace();
                cause = cause.getCause();
            }
        } finally {
            // Halt the late message
            timer.cancel();

            final long stopTime = System.nanoTime();
            final long delta = (stopTime - startTime) / 1000;
            this.logger.fine("Time to startup plugin " + c + " was " + delta + "µs");
        }
        return null;
    }

    /**
     * Continues to spawn a previously halted spawn operation
     * 
     * @param spawnResult The parameter you passed. 
     * @return .
     */
    public SpawnResult continueSpawn(SpawnResult spawnResult) {

        final Pluggable spawnedPlugin = spawnResult.pluggable;
        final Class<? extends Pluggable> c = spawnedPlugin.getClass();

        // Finally load and register plugin
        try {
            // 1. Inject all variables
            injectVariables(spawnedPlugin);

            // Obtain all methods
            final Method[] methods = getMethods(c);

            // 2. Call all init methods
            final boolean initStatus = callInitMethods(spawnedPlugin, methods);
            if (initStatus == false) {
                spawnResult.metaInformation.pluginStatus = PluginStatus.FAILED;
                return spawnResult;
            }

            // Initialization complete
            spawnResult.metaInformation.pluginStatus = PluginStatus.INITIALIZED;

            // 3. Spawn all threads
            spawnThreads(spawnResult, methods);

            // 4. Spawn timer
            spawnTimer(spawnResult, methods);

            // 5. Obtain PluginLoaded methods
            obtainPluginLoadedMethods(spawnResult, methods);

            // Currently running
            spawnResult.metaInformation.pluginStatus = PluginStatus.ACTIVE;

            return spawnResult;

        } catch (final Throwable e) {
            this.logger.warning("Unable to load plugin " + c.getName());
            this.logger.warning(e.toString());
            e.printStackTrace();
            Throwable cause = e.getCause();
            while (cause != null) {
                cause.printStackTrace();
                cause = cause.getCause();
            }
        }
        return null;
    }

    /**
     * 
     *   @param methods 
     * @returns True if initialization was successful.
     * @throws IllegalAccessException
     * 
     *  
     */
    private boolean callInitMethods(final Pluggable spawnedPlugin, final Method[] methods)
                                                                                          throws IllegalAccessException {
        final Class<? extends Pluggable> spawnClass = spawnedPlugin.getClass();

        this.logger.finer("Doing init for " + spawnedPlugin);

        for (final Method method : methods) {
            this.logger.finest("Processing method " + method);

            // Init methods will be marked by the corresponding annotation.
            final Init annotation = method.getAnnotation(Init.class);
            if (annotation != null) {
                this.logger.finer("Annotation found on method " + method);

                try {
                    final Object invoke = method.invoke(spawnedPlugin, new Object[0]);
                    if (invoke != null && invoke instanceof Boolean) {
                        // Check if any init method returns false.
                        if (((Boolean) invoke).booleanValue() == false) return false;
                    }
                } catch (final IllegalArgumentException e) {
                    this.logger.warning("Error(IAE) invoking requested @Init on plugin " + spawnClass.getName());
                    this.logger.warning(e.toString());
                    e.printStackTrace();
                    return false;
                } catch (final InvocationTargetException e) {
                    this.logger.warning("Error(ITE) invoking requested @Init on plugin " + spawnClass.getName());
                    this.logger.warning(e.toString());
                    e.printStackTrace();
                    return false;
                } catch (final Exception e) {
                    this.logger.warning("Error invoking requested @Init on plugin (unknown exception): " + spawnClass.getName());
                    this.logger.warning(e.toString());
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @param plugin
     */
    private void callShutdownMethods(final Plugin plugin) {
        final Class<? extends Plugin> spawnClass = plugin.getClass();
        final Method[] methods = spawnClass.getMethods();

        this.logger.finer("Doing shutdown for " + plugin);

        for (final Method method : methods) {
            this.logger.finest("Processing method " + method);

            // Init methods will be marked by the corresponding annotation.
            final Shutdown annotation = method.getAnnotation(Shutdown.class);
            if (annotation != null) {
                this.logger.finer("Annotation found on method " + method);

                try {
                    method.invoke(plugin, new Object[0]);
                } catch (final IllegalArgumentException e) {
                    this.logger.warning("Error invoking requested @Shutdown on plugin " + spawnClass.getName());
                    this.logger.warning(e.toString());
                    e.printStackTrace();
                } catch (final InvocationTargetException e) {
                    this.logger.warning("Error invoking requested @Shutdown on plugin " + spawnClass.getName());
                    this.logger.warning(e.toString());
                    e.printStackTrace();
                } catch (final Exception e) {
                    this.logger.warning("Error invoking requested @Shutdown on plugin (unknown exception): " + spawnClass.getName());
                    this.logger.warning(e.toString());
                    e.printStackTrace();
                }
            }
        }
        return;
    }

    /**
     * @param c
     * @return
     */
    private Method[] getMethods(final Class<? extends Pluggable> c) {
        final Method[] methods = c.getMethods();
        return methods;
    }

    /**
     * @param spawnedPlugin
     * @throws IllegalAccessException
     */
    @SuppressWarnings("unchecked")
    private void injectVariables(final Pluggable spawnedPlugin)
                                                               throws IllegalAccessException {

        // All fields we have a look at
        final Field[] fields = spawnedPlugin.getClass().getFields();
        final Method[] methods = spawnedPlugin.getClass().getMethods();

        // Process every field
        for (final Field field : fields) {
            // Try to get inject annotation. New: also turn on extended accessibility, so 
            // elements don't have to be public anymore. 
            field.setAccessible(true);
            final InjectPlugin ipannotation = field.getAnnotation(InjectPlugin.class);

            // If there is one ..
            if (ipannotation != null) {

                // Obtain capabilities 
                final String[] capabilities = ipannotation.requiredCapabilities();

                // Handle the plugin-parameter part
                // In the default case do an auto-detection ...
                final Class<? extends Plugin> typeOfField = (Class<? extends Plugin>) field.getType();

                this.logger.fine("Injecting plugin by autodetection (" + typeOfField.getName() + ") into " + spawnedPlugin.getClass().getName());

                field.set(spawnedPlugin, this.pluginManagerImpl.getPlugin(typeOfField, new OptionCapabilities(capabilities)));
            }
        }

        // And setter methods as well (aka Scala hack)
        for (Method method : methods) {
            // Try to get inject annotation. New: also turn on extended accessibility, so 
            // elements don't have to be public anymore. 
            method.setAccessible(true);
            final InjectPlugin ipannotation = method.getAnnotation(InjectPlugin.class);

            if (ipannotation != null) {

                // Obtain capabilities 
                final String[] capabilities = ipannotation.requiredCapabilities();

                // Handle the plugin-parameter part
                // In the default case do an auto-detection ...
                final Class<? extends Plugin> typeOfMethod = (Class<? extends Plugin>) method.getParameterTypes()[0];

                this.logger.fine("Injecting plugin by autodetection (" + typeOfMethod.getName() + ") into " + spawnedPlugin.getClass().getName());

                try {
                    method.invoke(spawnedPlugin, this.pluginManagerImpl.getPlugin(typeOfMethod, new OptionCapabilities(capabilities)));
                } catch (IllegalArgumentException e) {
                    this.logger.warning("Unable to inject plugin " + typeOfMethod + " into method " + method);
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    this.logger.warning("Unable to inject plugin " + typeOfMethod + " into method " + method);
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @param spawnResult
     * @param methods
     */
    private void spawnThreads(final SpawnResult spawnResult, final Method[] methods) {
        final Class<? extends Pluggable> spawnClass = spawnResult.pluggable.getClass();
        for (final Method method : methods) {
            // Init methods will be marked by the corresponding annotation.  New: 
            // also turn on extended accessibility, so elements don't have to be public anymore.
            method.setAccessible(true);
            final net.xeoh.plugins.base.annotations.Thread annotation = method.getAnnotation(Thread.class);
            if (annotation != null) {

                final java.lang.Thread t = new java.lang.Thread(new Runnable() {

                    public void run() {
                        try {
                            // TODO: Pass kind of ThreadController as argument 1 (or any fitting argument)
                            method.invoke(spawnResult.pluggable, new Object[0]);
                        } catch (final IllegalArgumentException e) {
                            Spawner.this.logger.warning("Error starting requested thread on plugin (1)" + spawnClass.getName());
                            Spawner.this.logger.warning(e.getMessage());
                            e.printStackTrace();
                        } catch (final IllegalAccessException e) {
                            Spawner.this.logger.warning("Error invoking requested thread on plugin (2) " + spawnClass.getName());
                            Spawner.this.logger.warning(e.getMessage());
                            e.printStackTrace();
                        } catch (final InvocationTargetException e) {
                            Spawner.this.logger.warning("Error invoking requested thread on plugin (3) " + spawnClass.getName());
                            Spawner.this.logger.warning(e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });

                t.setDaemon(annotation.isDaemonic());
                t.start();

                // Add timer task to list
                spawnResult.metaInformation.threads.add(t);
            }
        }
    }

    /**
     * @param spawnResult
     * @param methods
     */
    @SuppressWarnings("unchecked")
    private void obtainPluginLoadedMethods(SpawnResult spawnResult, Method[] methods) {
        for (final Method method : methods) {
            //New: also turn on extended accessibility, so elements don't have to be public anymore.
            method.setAccessible(true);
            final PluginLoaded annotation = method.getAnnotation(PluginLoaded.class);
            if (annotation != null) {
                final PluginLoadedInformation pli = new PluginLoadedInformation();
                final Class<?>[] parameterTypes = method.getParameterTypes();

                if (parameterTypes.length != 1) {
                    this.logger.warning("Wrong number of parameters for PluginLoaded annotations");
                    continue;
                }

                pli.method = method;
                pli.baseType = (Class<? extends Plugin>) parameterTypes[0];

                // And add result
                spawnResult.metaInformation.pluginLoadedInformation.add(pli);
            }
        }
    }

    /**
     * @param spawnResult
     * @param methods
     */
    private void spawnTimer(final SpawnResult spawnResult, final Method[] methods) {
        final Class<? extends Pluggable> spawnClass = spawnResult.pluggable.getClass();
        for (final Method method : methods) {
            // Init methods will be marked by the corresponding annotation. New: also 
            // turn on extended accessibility, so elements don't have to be public anymore.
            method.setAccessible(true);
            final net.xeoh.plugins.base.annotations.Timer annotation = method.getAnnotation(Timer.class);
            if (annotation != null) {

                final java.util.Timer t = new java.util.Timer();

                final TimerTask tt = new TimerTask() {

                    @Override
                    public void run() {
                        try {
                            final Object invoke = method.invoke(spawnResult.pluggable, new Object[0]);
                            if (invoke != null && invoke instanceof Boolean) {
                                if (((Boolean) invoke).booleanValue()) {
                                    t.cancel();
                                }
                            }
                        } catch (final IllegalArgumentException e) {
                            Spawner.this.logger.warning("Error starting requested timer on plugin " + spawnClass.getName());
                            Spawner.this.logger.warning(e.toString());
                            e.printStackTrace();
                        } catch (final IllegalAccessException e) {
                            Spawner.this.logger.warning("Error invoking requested timer method on plugin " + spawnClass.getName());
                            Spawner.this.logger.warning(e.toString());
                            e.printStackTrace();
                        } catch (final InvocationTargetException e) {
                            Spawner.this.logger.warning("Error invoking requested timer method on plugin " + spawnClass.getName());
                            Spawner.this.logger.warning(e.toString());
                            e.printStackTrace();
                        }
                    }
                };

                if (annotation.timerType() == Timer.TimerType.RATE_BASED) {
                    t.scheduleAtFixedRate(tt, annotation.startupDelay(), annotation.period());
                }

                if (annotation.timerType() == Timer.TimerType.DELAY_BASED) {
                    t.schedule(tt, annotation.startupDelay(), annotation.period());
                }

                // Add timer task to list
                spawnResult.metaInformation.timerTasks.add(tt);
                spawnResult.metaInformation.timers.add(t);
            }

        }
    }

    /**
     * Returns the list of all dependencies the plugin has .
     * 
     * @param pluginClass
     * @return .
     */
    @SuppressWarnings("unchecked")
    public Collection<Dependency> getDependencies(Class<? extends Plugin> pluginClass) {
        final Collection<Dependency> rval = new ArrayList<Dependency>();

        // All fields we have a look at
        final Field[] fields = pluginClass.getFields();

        // Process every field
        for (final Field field : fields) {
            // Try to get inject annotation. New: also turn on extended accessibility, 
            // so elements don't have to be public anymore.
            field.setAccessible(true);
            final InjectPlugin ipannotation = field.getAnnotation(InjectPlugin.class);

            // If there is one ..
            if (ipannotation == null) continue;

            // Don't recognize optional fields as dependencies.
            if (ipannotation.isOptional()) continue;

            // Obtain capabilities                         

            final Dependency d = new Dependency();
            d.capabilites = ipannotation.requiredCapabilities();
            d.pluginClass = (Class<? extends Plugin>) field.getType();
            d.isOptional = ipannotation.isOptional();

            rval.add(d);
        }

        return rval;
    }
}
