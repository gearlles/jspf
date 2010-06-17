/*
 * RemoteTest.java
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
package net.xeoh.plugins.remote;

import java.net.URI;

import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.options.getplugin.OptionPluginSelector;
import net.xeoh.plugins.base.options.getplugin.PluginSelector;
import net.xeoh.plugins.base.util.JSPFProperties;
import net.xeoh.plugins.remote.impl.lipermi.RemoteAPIImpl;
import net.xeoh.plugins.testplugins.testannotations.TestAnnotations;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author rb
 *
 */
public class RemoteTest {

    private PluginManager pm;

    /**
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        final JSPFProperties props = new JSPFProperties();

        props.setProperty(PluginManager.class, "cache.enabled", "true");
        props.setProperty(PluginManager.class, "cache.mode", "weak");
        props.setProperty(PluginManager.class, "cache.file", "jspf.cache");
        props.setProperty(PluginManager.class, "supervision.enabled", "true");
        props.setProperty(RemoteAPIImpl.class, "export.port", "12345");

        this.pm = PluginManagerFactory.createPluginManager(props);
        this.pm.addPluginsFrom(new URI("classpath://*"));
    }

    /**
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        this.pm.shutdown();
    }

    /**
     * 
     */
    @Test
    public void testERMI() {
        Assert.assertNotNull(this.pm);

        final TestAnnotations plugin = this.pm.getPlugin(TestAnnotations.class);
        final RemoteAPI remote = this.pm.getPlugin(RemoteAPI.class, new OptionPluginSelector<RemoteAPI>(new PluginSelector<RemoteAPI>() {

            public boolean selectPlugin(final RemoteAPI p) {
                if (p.getPublishMethod().equals(PublishMethod.LIPE)) return true;
                return false;
            }

        }));

        Assert.assertNotNull(plugin);
        Assert.assertNotNull(remote);

        final ExportResult exportPluginURL = remote.exportPlugin(plugin);
        System.out.println(exportPluginURL);

        Assert.assertNotNull(exportPluginURL);

        final TestAnnotations remoteProxy = remote.getRemoteProxy(exportPluginURL.getExportURIs().iterator().next(), TestAnnotations.class);
        // System.out.println(remoteProxy);
        Assert.assertNotNull(remoteProxy);
        Assert.assertEquals(remoteProxy.getInitStatus(), "INIT OK");

    }

    /**
     * 
     */
    @Test
    public void testLIPE() {
        Assert.assertNotNull(this.pm);

        final TestAnnotations plugin = this.pm.getPlugin(TestAnnotations.class);
        final RemoteAPI remote = this.pm.getPlugin(RemoteAPI.class, new OptionPluginSelector<RemoteAPI>(new PluginSelector<RemoteAPI>() {

            public boolean selectPlugin(final RemoteAPI p) {
                if (p.getPublishMethod().equals(PublishMethod.LIPE)) return true;
                return false;
            }

        }));

        Assert.assertNotNull(plugin);
        Assert.assertNotNull(remote);

        final ExportResult exportPluginURL = remote.exportPlugin(plugin);
        System.out.println(exportPluginURL.getExportURIs().iterator().next());

        Assert.assertNotNull(exportPluginURL);

        final TestAnnotations remoteProxy = remote.getRemoteProxy(exportPluginURL.getExportURIs().iterator().next(), TestAnnotations.class);
        // System.out.println(remoteProxy);
        Assert.assertNotNull(remoteProxy);
        Assert.assertEquals(remoteProxy.getInitStatus(), "INIT OK");

    }

    /**
     * 
     */
    @Test
    public void testXMLRPC() {
        Assert.assertNotNull(this.pm);

        final TestAnnotations plugin = this.pm.getPlugin(TestAnnotations.class);

        this.pm.getPlugin(RemoteAPI.class);

        final RemoteAPI remote = this.pm.getPlugin(RemoteAPI.class, new OptionPluginSelector<RemoteAPI>(new PluginSelector<RemoteAPI>() {

            public boolean selectPlugin(final RemoteAPI p) {
                if (p.getPublishMethod().equals(PublishMethod.XMLRPC)) return true;
                return false;
            }

        }));

        Assert.assertNotNull(plugin);
        Assert.assertNotNull(remote);

        final ExportResult exportPluginURL = remote.exportPlugin(plugin);

        Assert.assertNotNull(exportPluginURL);

        final TestAnnotations remoteProxy = remote.getRemoteProxy(exportPluginURL.getExportURIs().iterator().next(), TestAnnotations.class);
        // System.out.println(remoteProxy);
        Assert.assertNotNull(remoteProxy);

        // TODO: Why does this line block sometimes?
        Assert.assertEquals(remoteProxy.getInitStatus(), "INIT OK");

    }

    /**
     * 
     */
    @Test
    public void testXMLRPCDelight() {
        Assert.assertNotNull(this.pm);

        final TestAnnotations plugin = this.pm.getPlugin(TestAnnotations.class);

        this.pm.getPlugin(RemoteAPI.class);

        final RemoteAPI remote = this.pm.getPlugin(RemoteAPI.class, new OptionPluginSelector<RemoteAPI>(new PluginSelector<RemoteAPI>() {

            public boolean selectPlugin(final RemoteAPI p) {
                if (p.getPublishMethod().equals(PublishMethod.XMLRPCDELIGHT))
                    return true;
                return false;
            }

        }));

        Assert.assertNotNull(plugin);
        Assert.assertNotNull(remote);

        final ExportResult exportPluginURL = remote.exportPlugin(plugin);

        Assert.assertNotNull(exportPluginURL);

        final TestAnnotations remoteProxy = remote.getRemoteProxy(exportPluginURL.getExportURIs().iterator().next(), TestAnnotations.class);
        // System.out.println(remoteProxy);
        Assert.assertNotNull(remoteProxy);

        // TODO: Why does this line block sometimes?
        Assert.assertEquals(remoteProxy.getInitStatus(), "INIT OK");

    }
}
