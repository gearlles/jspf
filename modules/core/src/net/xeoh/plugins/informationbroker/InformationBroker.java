/*
 * Infobroker.java
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
package net.xeoh.plugins.informationbroker;

import java.util.Map;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.informationbroker.options.PublishOption;
import net.xeoh.plugins.informationbroker.options.SubscribeOption;
import net.xeoh.plugins.informationbroker.standarditems.strings.StringItem;
import net.xeoh.plugins.informationbroker.standarditems.vanilla.VanillaItem;
import net.xeoh.plugins.informationbroker.util.InformationBrokerUtil;

/**
 * Think of the information broker as a large, shared and type-safe hybrid between a {@link Map}. 
 * and a Bus. It enables you to exchange information between a number of plugins by well known 
 * keys. In contrast to a common Bus, the exchanged items are stored and can be retrieved 
 * by their key at any. Like the bus, however, listeners can register for updates to certain 
 * keys to be able to react on changes to them.<br/></br/>
 * 
 * The new InformationBroker interface supersedes the old Bus present in JSPF, as it provides
 * similar functionality.<br/></br/>
 * 
 * For your convenience there are a number of <i>standard items</i>, like the {@link StringItem} and 
 * the {@link VanillaItem}.
 *  
 * @author Ralf Biedert
 */
public interface InformationBroker extends Plugin {

    /**
     * Publishes a new information item. The item will be made available to all listeners 
     * subscribed to the item, or requesting it later. For example, to publish the current user 
     * name you could write:<br/><br/>
     * 
     * <code>
     * publish(new StringItem("user:name", "John Doe"));
     * </code><br/><br/>
     * 
     * @param item The items to publish.
     * @param options  A number of options the <code>publish()</code> method understands.
     */
    public void publish(InformationItem<?> item, PublishOption... options);

    /**
     * Subscribes to a given information item. The listener will be called as soon as the
     * item changes. It will also be called when the item has already been set before. For 
     * example, to subscribe to the latest location of a device (provided as a String) 
     * you could write:<br/><br/>
     * 
     * <code>
     * plugin.subscribe(new StringID("device:location"), new InformationListener&lt;String>() {<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;public void update(InformationItem&lt;String> item) {<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ...<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;}<br/>
     * });
     * </code><br/><br/>
     * 
     * Also see the {@link InformationBrokerUtil}, it contains many convenience functions. 
     * 
     * @param <T> The type of the item to wait for. 
     * @param id The ID to subscribe to
     * @param listener The lister will be called whenever the value of the ID changes; and it will 
     * be called right away if the information item has already been set before.  
     * @param options A number of options the <code>subscribe()</code> method understands.
     */
    public <T> void subscribe(InformationItemIdentifier<T, ?> id,
                              InformationListener<T> listener, SubscribeOption... options);

    /**
     * Unsubscribes the given information listener. It will not be called anymore afterwards.
     * 
     * @param listener The listener to unsubscribe.
     */
    public void unsubscribe(InformationListener<?> listener);
}
