/*
 * PluginTest.java
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
package net.xeoh.plugins.core;

import java.net.URI;

import junit.framework.Assert;
import net.xeoh.plugins.base.PluginInformation;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.JSPFProperties;
import net.xeoh.plugins.testplugins.testannotations.TestAnnotations;
import net.xeoh.plugins.testplugins.testannotations.impl.TestAnnotationsImpl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author rb
 *
 */
public class PluginManagerDisablingPlugins {

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
        
        // Enable and disable plugins like this:
        props.setProperty(TestAnnotationsImpl.class, "plugin.disabled", "false");
        
        
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
    public void testGetPluginClassOfP() {
        final PluginInformation pi = this.pm.getPlugin(PluginInformation.class);
        final TestAnnotations ta = this.pm.getPlugin(TestAnnotations.class);

        System.out.println(ta);
        Assert.assertNull(ta);
    }

}
