/*
 * Cache.java
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
package net.xeoh.plugins.sandbox;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.options.getplugin.OptionPluginSelector;
import net.xeoh.plugins.base.options.getplugin.PluginSelector;
import net.xeoh.plugins.base.util.JSPFProperties;
import net.xeoh.plugins.remote.ExportResult;
import net.xeoh.plugins.remote.PublishMethod;
import net.xeoh.plugins.remote.RemoteAPI;
import net.xeoh.plugins.testplugins.testannotations.TestAnnotations;

public class JSONTest {
    public static void main(String[] args) throws URISyntaxException, InterruptedException {
        final JSPFProperties props = new JSPFProperties();

        props.setProperty(PluginManager.class, "cache.enabled", "true");
        props.setProperty(PluginManager.class, "cache.mode", "weak");
        props.setProperty(PluginManager.class, "cache.file", "jspf.cache");

        PluginManager pm = PluginManagerFactory.createPluginManager(props);
        pm.addPluginsFrom(new URI("classpath://*"));

        TestAnnotations plugin = pm.getPlugin(TestAnnotations.class);
        RemoteAPI remote = pm.getPlugin(RemoteAPI.class, new OptionPluginSelector<RemoteAPI>(new PluginSelector<RemoteAPI>() {

            public boolean selectPlugin(final RemoteAPI p) {
                if (p.getPublishMethod().equals(PublishMethod.JSON)) return true;
                return false;
            }

        }));

        ExportResult exportPlugin = remote.exportPlugin(plugin);
        Collection<URI> exportURIs = exportPlugin.getExportURIs();
        for (URI uri : exportURIs) {
            System.out.println(uri);
        }
        
        Thread.sleep(6000000);

        pm.shutdown();
    }
}
