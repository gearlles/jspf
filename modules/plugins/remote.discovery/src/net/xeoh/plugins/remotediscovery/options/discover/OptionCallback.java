/*
 * OptionCapabilities.java
 * 
 * Copyright (c) 2009, Ralf Biedert All rights reserved.
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
package net.xeoh.plugins.remotediscovery.options.discover;

import java.util.Collection;

import net.xeoh.plugins.remotediscovery.DiscoveredPlugin;
import net.xeoh.plugins.remotediscovery.options.DiscoverOption;

/**
 * Requests a callback upon the detection of an appropriate plugin. The callback is executed as soon as the first matching 
 * plugin is detected and then removed.  
 * 
 * @author Ralf Biedert.
 */
public class OptionCallback implements DiscoverOption {
    /** */
    private static final long serialVersionUID = 8533647106985234966L;

    /**
     * Callback to implement for listeners. 
     */
    public static interface Callback {
        /**
         * Called with all plugins found since the request. 
         * 
         * @param plugins
         */
        public void pluginsDiscovered(Collection<DiscoveredPlugin> plugins);

        /**
         * Called when the timout occurred before the detection of any plugin.
         */
        public void timeout();
    }

    final int timeout;

    final Callback callback;

    /**
     * The caps to use.
     * @param callback 
     */
    public OptionCallback(Callback callback) {
        this(callback, 0);
    }

    /**
     * @param callback
     * @param timeout
     */
    public OptionCallback(Callback callback, int timeout) {
        this.callback = callback;
        this.timeout = timeout;

    }

    /**
     * @return the timeout
     */
    public int getTimeout() {
        return this.timeout;
    }

    /**
     * @return the callback
     */
    public Callback getCallback() {
        return this.callback;
    }

}
