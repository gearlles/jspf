/*
 * PluginConfiguration.java
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
package net.xeoh.plugins.base;

/**
 * Allows access to configuration items of plugins. The plugin-dependant
 * configuration files can be specified using the PluginImplementation
 * annotation.<br/><br/>
 * 
 * There are three ways of adding configuration: 1) By calling <code>setPreferences()</code>, 
 * 2) by providing a <code>JSPFPreferences</code> object to the PluginManagerFactory and 3) by 
 * using the <code>@ConfigurationFile</code> annotation.<br/><br/>  
 * 
 * A sample query might look like this: <code>getPreferences(GeoService.class, "remote.url")</code> 
 *
 * @author Ralf Biedert
 */
public interface PluginConfiguration extends Plugin {
    /**
     * Gets a configuration key. Root may be added for convenience and will
     * prefix the subkey with its FQN.
     *
     * @param root May also be null.
     * @param subkey If used in conjunction with root it should not be prefixed
     * with a dot (".")
     *
     * @return The corresponding value or null if nothing was found
     */
    public String getConfiguration(Class<?> root, String subkey);

    /**
     * Set the key for a value. Root may be added for convenience and will
     * prefix the subkey with its FQN. Usually the configuration is added 
     * by providing <code>JSPFPreferences</code> object to the 
     * <code>PluginManagerFactory</code>. 
     *
     * @param root May also be null.
     * @param subkey If used in conjunction with root it should not be prefixed 
     * with a dot (".")
     * @param value The value to set.
     */
    public void setConfiguration(Class<?> root, String subkey, String value);

}
