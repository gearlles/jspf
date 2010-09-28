/*
 * InformationBrokerUtil.java
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
package net.xeoh.plugins.informationbroker.util;

import java.util.concurrent.atomic.AtomicReference;

import net.xeoh.plugins.informationbroker.InformationBroker;
import net.xeoh.plugins.informationbroker.InformationItem;
import net.xeoh.plugins.informationbroker.InformationItemIdentifier;
import net.xeoh.plugins.informationbroker.InformationListener;
import net.xeoh.plugins.informationbroker.options.subscribe.OptionInstantRequest;

/**
 * Helper functions for the {@link InformationBroker} interface. The util uses the embedded 
 * interface to provide more convenience features.   
 *
 * @author Ralf Biedert
 * @see InformationBroker
 */
public class InformationBrokerUtil {
    /** The information broker */
    private final InformationBroker broker;

    /**
     * Creates a new information broker util.
     * 
     * @param broker
     */
    public InformationBrokerUtil(InformationBroker broker) {
        this.broker = broker;
    }

    /**
     * Returns the value for the given id or <code>dflt</code> if neither the key 
     * nor the default was present. For example, to retrieve the current user name 
     * and to get "unknown" if none was present, you could write:<br/><br/>
     * 
     * <code>
     * get(new StringID("user:name"), "unknown");
     * </code><br/><br/>
     * 
     * @param <T> The type of the return value.
     * @param id The ID to request. 
     * @param dflt The default value to return if no item was found.
     * @return Returns the requested item, a default if the item was not present or null 
     * in case neither was found.
     */
    public <T> T get(InformationItemIdentifier<T, ?> id, T... dflt) {
        final AtomicReference<T> object = new AtomicReference<T>();
        this.broker.subscribe(id, new InformationListener<T>() {
            @Override
            public void update(InformationItem<T> item) {
                object.set(item.getContent());
            }
        }, new OptionInstantRequest());

        final T rval = object.get();

        // Now check if we have a sensible return value or not, and return the default 
        // if we must
        if (rval == null && dflt.length > 0) return dflt[0];
        return rval;
    }

    /**
     * Publishes a number of items. This method works exactly as calling 
     * {@link InformationBroker}.<code>publish()</code> several times. 
     * 
     * @param items The items to publish.
     */
    public void publish(InformationItem<?>... items) {
        for (InformationItem<?> informationItem : items) {
            this.broker.publish(informationItem);
        }
    }
}
