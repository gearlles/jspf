/*
 * LocalFileProbe.java
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
package net.xeoh.plugins.remotediscovery.impl.v4.probes.local;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.remote.PublishMethod;
import net.xeoh.plugins.remotediscovery.DiscoveredPlugin;
import net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.ExportInfo;
import net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.ExportedPlugin;
import net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.impl.filepreferences.DiscoveryMangerFileImpl;
import net.xeoh.plugins.remotediscovery.impl.v4.DiscoveredPluginImpl;
import net.xeoh.plugins.remotediscovery.impl.v4.probes.AbstractProbe;
import net.xeoh.plugins.remotediscovery.options.DiscoverOption;

/**
 * @author rb
 *
 */
public class LocalProbe extends AbstractProbe {
    /** */
    final Logger logger = Logger.getLogger(this.getClass().getName());

    /** Where we keep our local files */
    final DiscoveryMangerFileImpl dmp = new DiscoveryMangerFileImpl();

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remotediscovery.impl.v4.probes.AbstractProbe#startup()
     */
    @Override
    public void startup() {
        super.startup();
    }

    /**
     * @param pluginManager
     */
    public LocalProbe(PluginManager pluginManager) {
        super(pluginManager);
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remotediscovery.RemoteDiscovery#announcePlugin(net.xeoh.plugins.base.Plugin, net.xeoh.plugins.remote.PublishMethod, java.net.URI)
     */
    @Override
    public void announcePlugin(Plugin plugin, PublishMethod publishMethod, URI uri) {
        this.dmp.anouncePlugin(plugin, publishMethod, uri);
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remotediscovery.RemoteDiscovery#discover(java.lang.Class, net.xeoh.plugins.remotediscovery.options.DiscoverOption[])
     */
    @Override
    public Collection<DiscoveredPlugin> discover(Class<? extends Plugin> plugin,
                                                 DiscoverOption... options) {
        final Collection<DiscoveredPlugin> rval = new ArrayList<DiscoveredPlugin>();

        final ExportInfo exportInfoFor = this.dmp.getExportInfoFor(plugin.getCanonicalName());
        final Collection<ExportedPlugin> allExported = exportInfoFor.allExported;

        for (final ExportedPlugin e : allExported) {
            rval.add(new DiscoveredPluginImpl(PublishMethod.valueOf(e.exportMethod), e.exportURI, 0 , e.timeSinceExport));
        }

        return rval;
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remotediscovery.RemoteDiscovery#revokePlugin(net.xeoh.plugins.base.Plugin, net.xeoh.plugins.remote.PublishMethod, java.net.URI)
     */
    @Override
    public void revokePlugin(Plugin plugin, PublishMethod publishMethod, URI uri) {
        this.dmp.revokePlugin(plugin, publishMethod, uri);
    }

}
