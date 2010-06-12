/*
 * PluginManagerFactory.java
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

import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.xeoh.plugins.base.PluginManager;

/**
 * Returns a new plugin manager
 * 
 * @author Ralf Biedert
 * 
 */
public class PluginManagerFactory {
    /**
     * Create a new plugin manager. No user configuration is used.
     * 
     * @return newly created implementation.
     */
    public static PluginManager createPluginManager() {
        return createPluginManager(new Properties());
    }

    /**
     * Create a new plugin manager with a supplied user configuration.
     * 
     * @param initialProperties
     *                Initial properties to use.
     * 
     * @return newly created implementation.
     */
    public static PluginManager createPluginManager(final Properties initialProperties) {

        // Setup logging
        if (initialProperties.containsKey("net.xeoh.plugins.base.PluginManager.logging.level")) {
            final String level = initialProperties.getProperty("net.xeoh.plugins.base.PluginManager.logging.level");

            setLogLevel(Level.parse(level));
        }

        return new PluginManagerImpl(initialProperties);
    }

    /**
     * Sets logging to the specified level 
     */
    private static void setLogLevel(Level level) {
        Logger.getLogger("").setLevel(level);
        Logger.getLogger("javax.jmdns").setLevel(Level.OFF);

        Handler[] handlers = Logger.getLogger("").getHandlers();

        for (Handler handler : handlers) {
            handler.setLevel(level);
        }
    }
}
