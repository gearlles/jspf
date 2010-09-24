/*
 * PluginInformation.java
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
package net.xeoh.plugins.base;

import java.util.Collection;

/**
 * Return different information about plugins, static as well as dynamic.
 *
 * @author Ralf Biedert
 *
 */
public interface PluginInformation extends Plugin {
    /**
     * @author rb
     *
     */
    public static enum Information {
        /** The author of this plugins. A list of one (Aloo) String is returnd. */
        AUTHORS,

        /**
         * Returns the self proclaimed capabilites of this plugin. A list of Strings is
         * returned. What is inside the capabilites is a matter of the individual plugin's
         * interface definition.
         */
        CAPABILITIES,

        /**
         * Version of this plugin. Aloo String, a direct conversion of the plugins
         * corresponding integer value.
         */
        VERSION,

        /** How many times this plugins has been requested. Aloo Integer as String. */
        // REQUEST_COUNT,

        /** Date when the plugin was initialized. Aloo Long as String. */
        // INIT_DATE

        /**
         * Returns a single string containing the URI to the classpath item this 
         * element came from 
         */
        CLASSPATH_ORIGIN,

        /** Returns a unique ID for a given plugin that does not change over versions */
        UNIQUE_ID
    }

    /**
     * Returns some information of a plugin
     * @param item 
     * @param plugin 
     *
     * @return .
     */
    public Collection<String> getInformation(Information item, Plugin plugin);
}
