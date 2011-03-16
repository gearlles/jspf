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
package net.xeoh.plugins.diagnosis;

import java.net.URI;

import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.JSPFProperties;
import net.xeoh.plugins.diagnosis.diagnosis.channels.LoggingChannel1;
import net.xeoh.plugins.diagnosis.local.Diagnosis;
import net.xeoh.plugins.testplugins.testannotations.impl.TestAnnotationsImpl;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author rb
 * 
 */
public class DiagnosisMiniTest {

    private static PluginManager pm;

    /**
     * @throws Exception
     */
    @BeforeClass
    public static void setUp() throws Exception {
        final JSPFProperties props = new JSPFProperties();

        props.setProperty(Diagnosis.class, "recording.enabled", "true");
        props.setProperty(Diagnosis.class, "recording.file", "diagnosis.record");
        props.setProperty(Diagnosis.class, "recording.format", "java/serialization");
        props.setProperty(Diagnosis.class, "analysis.stacktraces.enabled", "true");
        props.setProperty(Diagnosis.class, "analysis.stacktraces.depth", "10000");

        
        // Enable and disable plugins like this:
        props.setProperty(TestAnnotationsImpl.class, "plugin.disabled", "false");
        
        pm = PluginManagerFactory.createPluginManager(props);
        pm.addPluginsFrom(URI.create("xxx:yyy"));
    }

    /**
     * @throws Exception
     */
    @AfterClass
    public static void tearDown() throws Exception {
        pm.shutdown();
    }

    /**
     * 
     */
    @Test
    public void benchmark() {
        Assert.assertNotNull(this.pm);
        final Diagnosis diagnosis = this.pm.getPlugin(Diagnosis.class);
        diagnosis.channel(LoggingChannel1.class).status("XXX");

    }

}
