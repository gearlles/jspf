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
import java.net.URISyntaxException;

import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.options.addpluginsfrom.OptionLoadAsynchronously;
import net.xeoh.plugins.base.options.getplugin.OptionCapabilities;
import net.xeoh.plugins.remote.RemoteAPI;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author rb
 *
 */
public class PluginManagerLoadAsync {

    private PluginManager pm;

    /**
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        this.pm = PluginManagerFactory.createPluginManager();
    }

    /**
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        System.out.println("1");
        this.pm.shutdown();
        System.out.println("2");
    }

    /**
     * @throws InterruptedException 
     * 
     */
    @Test
    public void testGetPluginClassOfP() throws InterruptedException {

        Assert.assertNotNull("Pluginmanager must be there", this.pm);

        RemoteAPI plugin = this.pm.getPlugin(RemoteAPI.class);
        Assert.assertNull("Plugin must not be there at this point", plugin);

        try {
            this.pm.addPluginsFrom(new URI("classpath://*"), new OptionLoadAsynchronously());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        
        // Thread.sleep(2000);
        
        plugin = this.pm.getPlugin(RemoteAPI.class, new OptionCapabilities("ASOIDASJdasjkdhasdiasoDASOJd"));
        Assert.assertNull("This plugins must not exist", plugin);
        plugin = this.pm.getPlugin(RemoteAPI.class, new OptionCapabilities("XMLRPC"));
        Assert.assertNotNull("Now plugin must be there", plugin);
        
    }   
}
