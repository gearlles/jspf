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

import java.util.logging.Logger;

import net.xeoh.plugins.base.PluginConfiguration;

/**
 * Helper functions for PluginConfiguration interface. The util uses the embedded
 * interface to provide more convenience features.
 * 
 * @author Ralf Biedert
 * @see PluginConfiguration
 */
public class PluginConfigurationUtil extends VanillaPluginUtil<PluginConfiguration> implements PluginConfiguration {

    /**
     * Creates a new util for the given interface.
     * 
     * @param pc The interface to create the utils for.
     */
    public PluginConfigurationUtil(PluginConfiguration pc) {
        super(pc);
    }

    final Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Returns the requested key as an integer.
     * 
     * @param root Root class to request.
     * @param subkey Subkey to return.
     * @param defautvalue Default value to return if nothing was found.
     * @return The requested key, or the default value if the key was not found, or 0 if neither was present.
     */
    @SuppressWarnings("boxing")
    public int getInt(final Class<?> root, final String subkey,
                      final Integer... defautvalue) {
        
        final String configuration = this.object.getConfiguration(root, subkey);
        if (configuration == null) {
            if (defautvalue.length >= 1) return defautvalue[0];
            this.logger.warning("Returning default 0, but nothing was specified. Your application might behave strangely. Subkey was " + subkey);
            return 0;
        }

        return Integer.parseInt(configuration);
    }

    /**
     * Returns the reqeusted key or a default string.
     * 
     * @param root Root class to request.
     * @param subkey Subkey to return.
     * @param defautvalue Default value to return if nothing was found.
     * @return The requested key, or the default value if the key was not found, or "" if neither was present.
     */
    public String getString(final Class<?> root, final String subkey,
                            final String... defautvalue) {
        final String configuration = this.object.getConfiguration(root, subkey);
        if (configuration == null) {
            if (defautvalue.length >= 1) return defautvalue[0];
            this.logger.warning("Returning default '', but nothing was specified. Your application might behave strangely. Subkey was " + subkey);
            return "";
        }

        return configuration;
    }

    /**
     * Returns the reqeusted key or a default float.
     * 
     * @param root Root class to request.
     * @param subkey Subkey to return.
     * @param defautvalue Default value to return if nothing was found.
     * @return The requested key, or the default value if the key was not found, or 0.0 if neither was present.
     */
    @SuppressWarnings("boxing")
    public float getFloat(final Class<?> root, final String subkey,
                          final Float... defautvalue) {
        final String configuration = this.object.getConfiguration(root, subkey);
        if (configuration == null) {
            if (defautvalue.length >= 1) return defautvalue[0];
            this.logger.warning("Returning default '', but nothing was specified. Your application might behave strangely. Subkey was " + subkey);
            return 0;
        }

        return Float.parseFloat(configuration);
    }

    /**
     * Returns the reqeusted key or a default boolean.
     * 
     * @param root Root class to request.
     * @param subkey Subkey to return.
     * @param defautvalue Default value to return if nothing was found.
     * @return The requested key, or the default value if the key was not found, or <code>false</code> if neither was
     * present.
     */
    @SuppressWarnings("boxing")
    public boolean getBoolean(final Class<?> root, final String subkey,
                              final Boolean... defautvalue) {
        final String configuration = this.object.getConfiguration(root, subkey);
        if (configuration == null) {
            if (defautvalue.length >= 1) return defautvalue[0];
            this.logger.warning("Returning default '', but nothing was specified. Your application might behave strangely. Subkey was " + subkey);
            return false;
        }

        return Boolean.parseBoolean(configuration);
    }

    /**
     * Returns the reqeusted key or a default double.
     * 
     * @param root Root class to request.
     * @param subkey Subkey to return.
     * @param defautvalue Default value to return if nothing was found.
     * @return The requested key, or the default value if the key was not found, or 0.0 if neither was present.
     */
    @SuppressWarnings("boxing")
    public double getDouble(final Class<?> root, final String subkey,
                            final Double... defautvalue) {
        final String configuration = this.object.getConfiguration(root, subkey);
        if (configuration == null) {
            if (defautvalue.length >= 1) return defautvalue[0];
            this.logger.warning("Returning default '', but nothing was specified. Your application might behave strangely. Subkey was " + subkey);
            return 0;
        }

        return Double.parseDouble(configuration);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.xeoh.plugins.base.PluginConfiguration#getConfiguration(java.lang.Class, java.lang.String)
     */
    @Override
    public String getConfiguration(Class<?> root, String subkey) {
        return this.object.getConfiguration(root, subkey);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.xeoh.plugins.base.PluginConfiguration#setConfiguration(java.lang.Class, java.lang.String,
     * java.lang.String)
     */
    @Override
    public void setConfiguration(Class<?> root, String subkey, String value) {
        this.object.setConfiguration(root, subkey, value);
    }
}
