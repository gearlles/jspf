/*
 * PluginConfigurationImpl.java
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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import net.xeoh.plugins.base.Pluggable;
import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginInformation;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.annotations.Capabilities;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;
import net.xeoh.plugins.base.annotations.meta.Author;
import net.xeoh.plugins.base.annotations.meta.Stateless;
import net.xeoh.plugins.base.annotations.meta.Version;
import net.xeoh.plugins.base.impl.metahandling.PluginWrapper;
import net.xeoh.plugins.base.impl.registry.PluggableMetaInformation;
import net.xeoh.plugins.base.impl.registry.PluginRegistry;

/**
 * TODO: Make plugin threadsafe
 *
 * @author Ralf Biedert
 *
 */
@Author(name = "Ralf Biedert")
@Version(version = 1 * Version.UNIT_MAJOR)
@Stateless
@PluginImplementation
public class PluginInformationImpl implements PluginInformation {
    /** */
    final Logger logger = Logger.getLogger(this.getClass().getName());

    /**  */
    @InjectPlugin
    public PluginManager pluginManager;

    /* (non-Javadoc)
     * @see net.xeoh.plugins.base.PluginInformation#getInformation(net.xeoh.plugins.base.PluginInformation.Information, net.xeoh.plugins.base.Plugin)
     */
    public Collection<String> getInformation(final Information item, final Plugin _plugin) {

        // Needed to query some special information
        final PluginManagerImpl pmi = (PluginManagerImpl) this.pluginManager;

        // To handle annotations, we need to obtain the true class
        final Pluggable pluggable = (_plugin instanceof PluginWrapper) ? ((PluginWrapper) _plugin).getWrappedPlugin() : _plugin;

        // Prepare return values ...
        final Collection<String> rval = new ArrayList<String>();

        switch (item) {

        case CAPABILITIES:
            // Caps are only supported for plugins currently
            final String[] caps = getCaps(pluggable);
            for (final String string : caps) {
                rval.add(string);
            }
            break;

        case AUTHORS:
            Author author = pluggable.getClass().getAnnotation(Author.class);
            if (author == null) break;
            rval.add(author.name());
            break;

        case VERSION:
            Version version = pluggable.getClass().getAnnotation(Version.class);
            if (version == null) break;
            rval.add(Integer.toString(version.version()));
            break;

        case CLASSPATH_ORIGIN:
            if (pluggable instanceof Plugin) {
                final PluginRegistry pluginRegistry = pmi.getPluginRegistry();
                final PluggableMetaInformation metaInformation = pluginRegistry.getMetaInformationFor((Plugin) pluggable);
                if (metaInformation != null && metaInformation.classMeta != null && metaInformation.classMeta.pluginOrigin != null)
                    rval.add(metaInformation.classMeta.pluginOrigin.toString());
            } else {
                this.logger.info("CLASSPATH_ORIGIN cannot be requested for Pluglets at the moment");
            }
            break;

        default:
            this.logger.info("Requested InformationItem is now known!");
            break;
        }

        return rval;
    }

    /**
     * @param plugin
     * @return
     */
    private String[] getCaps(final Pluggable plugin) {
        final Class<? extends Pluggable> spawnClass = plugin.getClass();

        final Method[] methods = spawnClass.getMethods();

        // Search for proper method
        for (final Method method : methods) {

            // Init methods will be marked by the corresponding annotation.
            final Capabilities caps = method.getAnnotation(Capabilities.class);
            if (caps != null) {

                Object result = null;
                try {
                    result = method.invoke(plugin, new Object[0]);
                } catch (final IllegalArgumentException e) {
                    //
                } catch (final IllegalAccessException e) {
                    //
                } catch (final InvocationTargetException e) {
                    //
                }
                if (result != null && result instanceof String[])
                    return (String[]) result;
            }
        }

        return new String[0];
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.base.PluginInformation#getInformation(java.lang.Class, net.xeoh.plugins.base.PluginInformation.Information, net.xeoh.plugins.base.Plugin)
     */
    @Override
    public <T> T getInformation(Class<T> clazz, Information item, Plugin plugin) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *  ...
     */
    @SuppressWarnings( { "unused", "unchecked" })
    public void test() {
        String i1 = getInformation(String.class, Information.AUTHORS, this);
        List<String> i2 = getInformation(List.class, Information.AUTHORS, this);
    }
}
