/*
 * DiscoverCall.java
 * 
 * Copyright (c) 2010, Ralf Biedert All rights reserved.
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
package net.xeoh.plugins.remotediscovery.impl.v3;

import java.util.Collection;

import net.xeoh.plugins.remotediscovery.DiscoveredPlugin;

class DiscoverCall extends BaseCall {
    /**
     * 
     */
    private final RemoteDiscoveryImpl remoteDiscoveryImpl;

    /**
     * @param remoteDiscoveryImpl
     */
    DiscoverCall(RemoteDiscoveryImpl remoteDiscoveryImpl) {
        this.remoteDiscoveryImpl = remoteDiscoveryImpl;
    }

    public Collection<DiscoveredPlugin> call() {
        try {
            this.remoteDiscoveryImpl.syso("discover 1");
            // TODO: doRemoteDiscover() and e.resolve() above are the most time consuming functions!!!!
            Collection<DiscoveredPlugin> res = this.remoteDiscoveryImpl.doRemoteDiscover(this.getPlugin(), this.getDiscoverOptions());
            this.remoteDiscoveryImpl.syso("discover 2");
            if (res == null) {
                this.remoteDiscoveryImpl.syso("discover 3");
                return null;
            }
            // so we are ready...
            // Filter is there, so that cache contains unfiltered plugins

            this.remoteDiscoveryImpl.syso("discover 4");
            if (!res.isEmpty()) {
                this.remoteDiscoveryImpl.syso("discover 5");
                this.remoteDiscoveryImpl.doFilter(res, getDiscoverOptions());
            }

            if (!res.isEmpty()) this.remoteDiscoveryImpl.syso("---> discovery cache miss.");

            this.remoteDiscoveryImpl.syso("discover 6");
            return res;
        } catch (InterruptedException e) {
            // so cache thread was faster...
            return null;
        }
    }
}