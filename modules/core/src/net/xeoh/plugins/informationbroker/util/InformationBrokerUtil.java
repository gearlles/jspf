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

import java.util.Collection;

import net.xeoh.plugins.informationbroker.InformationBroker;
import net.xeoh.plugins.informationbroker.InformationItem;
import net.xeoh.plugins.informationbroker.InformationItemIdentifier;
import net.xeoh.plugins.informationbroker.InformationListener;
import net.xeoh.plugins.informationbroker.SubscriptionMode;

/**
 * @author Ralf Biedert
 */
public class InformationBrokerUtil {
    /** The information broker */
    private final InformationBroker broker;

    /**
     * @param broker
     */
    public InformationBrokerUtil(InformationBroker broker) {
        this.broker = broker;
    }

    /**
     * @param <Type>
     * @param <I>
     * @param item
     * @param dflt 
     * @return .
     */
    public <Type, I extends InformationItem<Type>> Type getItem(
                                                                InformationItemIdentifier<Type, I> item,
                                                                Type... dflt) {
        I informationItem = this.broker.getInformationItem(item);

        // Return the value if it is there
        if (informationItem != null) return informationItem.getContent();

        // Return the default
        if (dflt.length > 0) return dflt[0];

        // Return nothing ...
        return null;
    }

    /**
     * @param <Type>
     * @param <I>
     * @param item
     * @param valueListener
     */
    public <Type, I extends InformationItem<Type>> void onValue(
                                                                final InformationItemIdentifier<Type, I> item,
                                                                final ValueListener<Type> valueListener) {
        this.broker.subscribe(new InformationListener() {

            public void informationUpdate(
                                          InformationBroker brker,
                                          Collection<InformationItemIdentifier<?, InformationItem<?>>> ids) {
                valueListener.newValue(brker.getInformationItem(item).getContent());
            }
        }, SubscriptionMode.ALL_SET, item);
    }
}
