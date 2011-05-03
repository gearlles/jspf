/*
 * InjectHandler.java
 * 
 * Copyright (c) 2010, Ralf Biedert All rights reserved.
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
package net.xeoh.plugins.base.impl.spawning.handler;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;
import net.xeoh.plugins.base.impl.registry.PluginClassMetaInformation.Dependency;
import net.xeoh.plugins.base.options.getplugin.OptionCapabilities;

/**
 * Handles injections into plugins. 
 * 
 * @author Ralf Biedert
 */
public class InjectHandler extends AbstractHandler {

    /**
     * @param pluginManager
     */
    public InjectHandler(PluginManager pluginManager) {
        super(pluginManager);
    }

    
    /*
     * (non-Javadoc)
     * 
     * @see
     * net.xeoh.plugins.base.impl.spawning.handler.AbstractHandler#init(net.xeoh.plugins
     * .base.Plugin)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void init(Plugin plugin) throws Exception {

        // All fields we have a look at
        final Field[] fields = plugin.getClass().getFields();
        final Method[] methods = plugin.getClass().getMethods();

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

                this.logger.fine("Injecting plugin by autodetection (" + typeOfField.getName() + ") into " + plugin.getClass().getName());

                field.set(plugin, getEntityForType(typeOfField, capabilities));
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

                this.logger.fine("Injecting plugin by autodetection (" + typeOfMethod.getName() + ") into " + plugin.getClass().getName());

                try {
                    method.invoke(plugin, getEntityForType(typeOfMethod, capabilities));
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.xeoh.plugins.base.impl.spawning.handler.AbstractHandler#deinit(net.xeoh.plugins
     * .base.Plugin)
     */
    @Override
    public void deinit(Plugin plugin) throws Exception {
        // TODO Auto-generated method stub

    }
    
    
    /**
     * Returns the true plugin interface type for something that accepts 
     * an @InjectPlugin annotation. This is either the interface directly,
     * or some Util that accepts a interface as the first parameter.  
     * 
     * @param type
     * @return
     */
    Class<?> getTrueDependencyInterfaceType(Class<?> type) {
        // If it is an interface, return that
        if(type.isInterface()) return type;
        
        
        // In all other cases, return the type of the first parameter of the given class
        try {
            final Constructor<?>[] declaredConstructors = type.getDeclaredConstructors();
            for (Constructor<?> constructor : declaredConstructors) {
                final Class<?>[] parameterTypes = constructor.getParameterTypes();
                if(parameterTypes.length != 1) continue;
                if(parameterTypes[0].isAssignableFrom(type)) return parameterTypes[0];
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Tries to get an entity for the requested type.
     * 
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    Plugin getEntityForType(Class<?> type, String...capabilities) {
        // We need that anyways.
        Plugin plugin = this.pluginManager.getPlugin((Class<Plugin>) getTrueDependencyInterfaceType(type), new OptionCapabilities(capabilities)); 
        
        // First check if the requested type is an anctual interface or not. If it is, we simply treat
        // it as a plugin, if it is not (i.e., a ordinary class), we treat it as a util wrapper.
        if(type.isInterface()) {
            return plugin;
        }
        
        // In that case, we have to inspect the first parameter of the constructor that accepts itself as a 
        // paramter.
        try {
            final Constructor<?>[] constructors = type.getDeclaredConstructors();
            for (Constructor<?> constructor : constructors) {
                final Class<?>[] parameterTypes = constructor.getParameterTypes();
                if(parameterTypes.length != 1) continue;
                if(parameterTypes[0].isAssignableFrom(type)) return (Plugin) constructor.newInstance(plugin);            
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    
    /**
     * Returns the list of all dependencies the plugin has.
     * 
     * @param pluginClass
     * @return .
     */
    @SuppressWarnings("unchecked")
    public Collection<Dependency> getDependencies(Class<? extends Plugin> pluginClass) {
        final Collection<Dependency> rval = new ArrayList<Dependency>();

        // All fields we have a look at
        final Field[] fields = pluginClass.getFields();
        final Method[] methods = pluginClass.getMethods();
        

        // Process every field
        for (final Field field : fields) {
            field.setAccessible(true);
            
            final InjectPlugin ipannotation = field.getAnnotation(InjectPlugin.class);
            if (ipannotation == null) continue;
            if (ipannotation.isOptional()) continue;

            final Dependency d = new Dependency();
            d.capabilites = ipannotation.requiredCapabilities();
            d.pluginClass = (Class<? extends Plugin>) getTrueDependencyInterfaceType(field.getType());
            d.isOptional = ipannotation.isOptional();

            rval.add(d);
        }
        
        // And setter methods as well (aka Scala hack)
        for (Method method : methods) {
            method.setAccessible(true);
            
            final InjectPlugin ipannotation = method.getAnnotation(InjectPlugin.class);
            if (ipannotation == null) continue;
            if (ipannotation.isOptional()) continue;


            final Dependency d = new Dependency();
            d.capabilites = ipannotation.requiredCapabilities();
            d.pluginClass = (Class<? extends Plugin>) getTrueDependencyInterfaceType(method.getParameterTypes()[0]);
            d.isOptional = ipannotation.isOptional();

            rval.add(d);
        }        

        return rval;
    }
}
