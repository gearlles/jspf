/*
 * RemoteAPIDiscoveryUtil.java
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
package net.xeoh.plugins.remotediscovery.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.remote.RemoteAPI;
import net.xeoh.plugins.remotediscovery.DiscoveredPlugin;
import net.xeoh.plugins.remotediscovery.RemoteDiscovery;
import net.xeoh.plugins.remotediscovery.options.DiscoverOption;
import net.xeoh.plugins.remotediscovery.options.discover.OptionNearest;
import net.xeoh.plugins.remotediscovery.options.discover.OptionOldest;
import net.xeoh.plugins.remotediscovery.options.discover.OptionYoungest;

/**
 * This util is only relevant for RemoteAPI implementors which want to use 
 * the discovery:// URL
 * 
 * @author rb
 *
 */
public class RemoteAPIDiscoveryUtil {
    /** */
    private RemoteDiscovery discovery;

    /** */
    private RemoteAPI remoteAPI;

    /**
     * @param discovery
     * @param remoteAPI
     */
    public RemoteAPIDiscoveryUtil(RemoteDiscovery discovery, RemoteAPI remoteAPI) {
        this.discovery = discovery;
        this.remoteAPI = remoteAPI;
    }

    /**
     * If this function returns false, just proceed normally. If it returns true, control should be handed over to this module.
     * 
     * @param uri
     * @return .
     */
    public boolean isDiscoveryURI(URI uri) {
        if (uri == null) return false;
        if (uri.getScheme() == null) return false;
        return uri.getScheme().toLowerCase().equals("discover");
    }

    /**
     * Requests a remote service based on a discovery uri
     * 
     * @param <R>
     * @param url
     * @param remote
     * @return .
     */
    public <R extends Plugin> R getRemoteProxy(final URI url, final Class<R> remote) {

        final String authority = url.getAuthority().toLowerCase();
        final List<DiscoverOption> options = new ArrayList<DiscoverOption>();

        // Process options
        if (authority.equals("any")) {
            //
        }

        if (authority.equals("nearest")) {
            options.add(new OptionNearest());
        }

        if (authority.equals("youngest")) {
            options.add(new OptionYoungest());
        }

        if (authority.equals("oldest")) {
            options.add(new OptionOldest());
        }

        final Collection<DiscoveredPlugin> discovered = this.discovery.discover(remote, options.toArray(new DiscoverOption[0]));

        // Now find a matching plugin
        for (DiscoveredPlugin discoveredPlugin : discovered) {
            if (discoveredPlugin.getPublishMethod() != this.remoteAPI.getPublishMethod())
                continue;

            URI publishURI = discoveredPlugin.getPublishURI();

            return this.remoteAPI.getRemoteProxy(publishURI, remote);
        }

        // This is bad, we didn't find anything.
        return null;
    }
}
