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

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.options.getplugin.OptionPluginSelector;
import net.xeoh.plugins.base.options.getplugin.PluginSelector;
import net.xeoh.plugins.testplugins.testannotations.TestAnnotations;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author rb
 *
 */
public class PluginManagerTest {

    private PluginManager pm;

    /**
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {

        this.pm = PluginManagerFactory.createPluginManager();
        this.pm.addPluginsFrom(new URI("classpath://*"));
        //this.pm.addPluginsFrom(new File("dist/").toURI());
    }

    /**
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        this.pm.shutdown();
    }

    /**
     * @throws MalformedURLException
     */
    @Test
    public void testAddPluginsFrom() throws MalformedURLException {
        final PluginManager pm2 = PluginManagerFactory.createPluginManager();

        Assert.assertNull(pm2.getPlugin(TestAnnotations.class));
        //pm2.addPluginsFrom(new File("tests/plugins/test.coredefinition.jar").toURI());
        //pm2.addPluginsFrom(new File("tests/plugins/test.annotation.jar").toURI());
        //Assert.assertNotNull(pm2.getPlugin(TestAnnotations.class));
    }

    /**
     * 
     */
    //@Test
    public void testGetPluginClassOfP() {
        Assert.assertNotNull(this.pm);

        final TestAnnotations plugin = this.pm.getPlugin(TestAnnotations.class);

        Assert.assertNotNull(plugin);

        Assert.assertEquals(plugin.getInitStatus(), "INIT OK");
        Assert.assertEquals(plugin.getInjectionStatus(), "INJECTION OK");

        try {
            Thread.sleep(100);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(plugin.getThreadStatus(), "THREAD OK");
        Assert.assertEquals(plugin.getTimerStatus(), "TIMER OK");
    }

    /**
     * 
     */
    @Test
    public void testAllInterfaces() {
        final TestAnnotations plugin = this.pm.getPlugin(TestAnnotations.class);

        Collection<Class<? extends Plugin>> allPluginClasses = getAllPluginClasses(plugin);
        for (Class<? extends Plugin> class1 : allPluginClasses) {
            System.out.println(class1.getCanonicalName());
        }

    }

    @SuppressWarnings("unchecked")
    private static Collection<Class<? extends Plugin>> getAllPluginClasses(Plugin plugin) {
        Collection<Class<? extends Plugin>> rval = new ArrayList<Class<? extends Plugin>>();

        Class<? extends Plugin> c = plugin.getClass();
        Class<?>[] i = c.getInterfaces();

        for (Class<?> cc : i) {
            if (!Plugin.class.isAssignableFrom(cc)) continue;

            Collection<Class<?>> allSuperInterfaces = getAllSuperInterfaces(cc);
            for (Class<?> class1 : allSuperInterfaces) {
                if (!rval.contains(class1)) rval.add((Class<? extends Plugin>) class1);
            }

        }

        return rval;
    }

    private static Collection<Class<?>> getAllSuperInterfaces(Class<?> c) {
        Collection<Class<?>> rval = new ArrayList<Class<?>>();

        rval.add(c);

        Class<?>[] interfaces = c.getInterfaces();
        for (Class<?> class1 : interfaces) {
            Collection<Class<?>> allSuperInterfaces = getAllSuperInterfaces(class1);
            for (Class<?> class2 : allSuperInterfaces) {
                if (rval.contains(class2)) continue;
                rval.add(class2);
            }
        }
        return rval;
    }

    /**
     * 
     */
    //@Test
    public void testGetPluginClassOfPPluginSelectorOfP() {

        Assert.assertNotNull(this.pm);

        final TestAnnotations plugin = this.pm.getPlugin(TestAnnotations.class, new OptionPluginSelector<TestAnnotations>(new PluginSelector<TestAnnotations>() {

            public boolean selectPlugin(final TestAnnotations plugiN) {
                return false;
            }

        }));

        Assert.assertNull(plugin);
    }

}
