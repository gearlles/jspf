/*
 * RemoteDiscovery.java
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
package net.xeoh.plugins.remotediscovery;

import java.net.URI;
import java.util.Collection;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.remote.PublishMethod;
import net.xeoh.plugins.remotediscovery.options.DiscoverOption;

/**
 * Discovers remote services.
 * 
 * @author Ralf Biedert
 */
public interface RemoteDiscovery extends Plugin {

    /**
     * Announces the given plugin on the local network.
     * 
     * @param plugin
     * @param publishMethod 
     * @param uri
     */
    public void announcePlugin(Plugin plugin, PublishMethod publishMethod, URI uri);

    /**
     * Unpublishes the plugin on the network. 
     * 
     * @param plugin
     * @param publishMethod
     * @param uri
     */
    public void revokePlugin(Plugin plugin, PublishMethod publishMethod, URI uri);

    /**
     * Discovers a set of plugins that is compatible with the requested interface. 
     * 
     * @param plugin 
     * @param options 
     * 
     * @return .
     */
    public Collection<DiscoveredPlugin> discover(Class<? extends Plugin> plugin,
                                                 DiscoverOption... options);

}
