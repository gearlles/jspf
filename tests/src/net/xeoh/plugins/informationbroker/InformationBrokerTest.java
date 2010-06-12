/*
 * BusTest.java
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
package net.xeoh.plugins.informationbroker;

import java.net.URI;
import java.util.Collection;

import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.informationbroker.standarditems.strings.StringID;
import net.xeoh.plugins.informationbroker.standarditems.strings.StringItem;
import net.xeoh.plugins.informationbroker.standarditems.vanilla.ObjectID;
import net.xeoh.plugins.informationbroker.standarditems.vanilla.ObjectItem;
import net.xeoh.plugins.informationbroker.util.InformationBrokerUtil;
import net.xeoh.plugins.informationbroker.util.ValueListener;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author rb
 *
 */
public class InformationBrokerTest {

    private PluginManager pm;

    boolean called1 = false;

    boolean called2 = false;

    /**
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        this.pm = PluginManagerFactory.createPluginManager();
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
     * @throws InterruptedException
     */
    @Test
    public void testBroker() throws InterruptedException {
        Assert.assertNotNull(this.pm);

        final InformationBroker plugin = this.pm.getPlugin(InformationBroker.class);
        
        plugin.publish(new StringItem("some:id", "somevalue"));
        final StringItem informationItem = plugin.getInformationItem(new StringID("some:id"));

        Assert.assertNotNull(informationItem);
        Assert.assertEquals(informationItem.getContent(), "somevalue");

        plugin.subscribe(new InformationListener() {

            public void informationUpdate(
                                          InformationBroker broker,
                                          Collection<InformationItemIdentifier<?, InformationItem<?>>> ids) {
                InformationBrokerTest.this.called1 = true;
            }
        }, SubscriptionMode.ALL_SET, new StringID("some:id"));

        plugin.subscribe(new InformationListener() {

            public void informationUpdate(
                                          InformationBroker broker,
                                          Collection<InformationItemIdentifier<?, InformationItem<?>>> ids) {
                InformationBrokerTest.this.called2 = true;
            }

        }, SubscriptionMode.ALL_SET, new StringID("some:other:id"), new StringID("some:id"));

        plugin.publish(new ObjectItem("x:x", "abx"));
        
        Object c1 = plugin.getInformationItem(new ObjectID("x:x")).getContent();
        String c2 = plugin.getInformationItem(new ObjectID("x:x")).getContent(String.class);

        System.out.println(c1);
        System.out.println(c2);
        
        InformationBrokerUtil ibu = new InformationBrokerUtil(plugin);
        
        ibu.onValue(new ObjectID("x:x"), new ValueListener<Object>() {
            public void newValue(Object value) {
                System.out.println(value);
            }
        });

        Thread.sleep(1000);

        plugin.publish(new ObjectItem("x:x", "yoyo"));
        
        

        Thread.sleep(100);
        Assert.assertTrue(this.called1);
        Assert.assertFalse(this.called2);
        plugin.publish(new StringItem("some:other:id", "somevalue"));
        Thread.sleep(100);
        Assert.assertTrue(this.called2);
    }
}
