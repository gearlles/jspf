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

import java.util.Properties;
import java.util.logging.Logger;

import net.xeoh.plugins.base.PluginConfiguration;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.meta.Author;
import net.xeoh.plugins.base.annotations.meta.Version;

/**
 *
 *
 * @author Ralf Biedert
 *
 */
@Author(name = "Ralf Biedert")
@PluginImplementation
@Version(version = Version.UNIT_MAJOR)
public class PluginConfigurationImpl implements PluginConfiguration {
    final Properties configuration;

    final Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * @param initialProperties
     */
    protected PluginConfigurationImpl(final Properties initialProperties) {
        this.configuration = initialProperties;
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.base.PluginConfiguration#getConfiguration(java.lang.Class, java.lang.String)
     */
    public synchronized String getConfiguration(final Class<?> root, final String subkey) {
        return this.configuration.getProperty(getKey(root, subkey));
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.base.PluginConfiguration#setConfiguration(java.lang.Class, java.lang.String, java.lang.String)
     */
    public synchronized void setConfiguration(final Class<?> root, final String subkey,
                                              final String value) {
        this.configuration.setProperty(getKey(root, subkey), value);
    }

    /**
     * @param root
     * @param subkey
     * @return
     */
    private String getKey(final Class<?> root, final String subkey) {
        String prefix = "";
        if (root != null) {
            prefix = root.getName() + ".";
        }

        this.logger.finer("Assembled key '" + prefix + subkey + "'");

        return prefix + subkey;
    }
}
