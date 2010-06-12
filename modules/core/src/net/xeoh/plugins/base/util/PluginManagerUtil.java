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

import java.util.ArrayList;
import java.util.Collection;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.options.getplugin.OptionPluginSelector;
import net.xeoh.plugins.base.options.getplugin.PluginSelector;

/**
 * Helper functions for a PluginManager.
 *
 * @author Ralf Biedert
 */
public class PluginManagerUtil {

    private final PluginManager pluginManager;

    /**
     * @param pm
     */
    public PluginManagerUtil(PluginManager pm) {
        this.pluginManager = pm;
    }

    /**
     * @param <P>
     * @param plugin
     * @return .
     */
    public <P extends Plugin> Collection<P> getPlugins(final Class<P> plugin) {
        return getPlugins(plugin, new PluginSelector<P>() {

            public boolean selectPlugin(final P p) {
                return true;
            }
        });
    }

    /**
     * @param <P>
     * @param plugin
     * @param selector
     * @return .
     */
    public <P extends Plugin> Collection<P> getPlugins(final Class<P> plugin,
                                                       final PluginSelector<P> selector) {
        final Collection<P> allPlugins = new ArrayList<P>();

        this.pluginManager.getPlugin(plugin, new OptionPluginSelector<P>(new PluginSelector<P>() {

            public boolean selectPlugin(final P p) {
                if (selector.selectPlugin(p)) {
                    allPlugins.add(p);
                }
                return false;
            }
        }));

        return allPlugins;
    }

}
