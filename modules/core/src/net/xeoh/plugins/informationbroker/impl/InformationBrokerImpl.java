/*
 * InformationBrokerImpl.java
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
package net.xeoh.plugins.informationbroker.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.meta.Author;
import net.xeoh.plugins.informationbroker.InformationBroker;
import net.xeoh.plugins.informationbroker.InformationItem;
import net.xeoh.plugins.informationbroker.InformationItemIdentifier;
import net.xeoh.plugins.informationbroker.InformationListener;
import net.xeoh.plugins.informationbroker.SubscriptionMode;

/**
 * @author rb
 */
@Author(name = "Ralf Biedert")
@PluginImplementation
public class InformationBrokerImpl implements InformationBroker {

    private static class Subscription {
        @SuppressWarnings("unchecked")
        public InformationItemIdentifier[] allToWaitFor;
        public InformationListener l;
        public SubscriptionMode mode;

        @SuppressWarnings("unchecked")
        public Subscription(final InformationListener l, final SubscriptionMode mode,
                            final InformationItemIdentifier... allToWaitFor) {

            this.l = l;
            this.mode = mode;
            this.allToWaitFor = allToWaitFor;
        }
    }

    /**
     * Calls listeners for an item.
     * 
     * @param broker
     * @param subscription
     */
    @SuppressWarnings("unchecked")
    final static void callListener(final InformationBroker broker,
                                   final Subscription subscription) {
        // A bit mess in here.
        final Collection<InformationItemIdentifier<?, InformationItem<?>>> cmd = new ArrayList<InformationItemIdentifier<?, InformationItem<?>>>();
        for (final InformationItemIdentifier item : subscription.allToWaitFor) {
            cmd.add(item);
        }
        subscription.l.informationUpdate(broker, cmd);
    }

    /** Manages all information items */
    @SuppressWarnings("unchecked")
    final Map<URI, InformationItem> items = new HashMap<URI, InformationItem>();

    /** Locks access to the items */
    final Lock itemsLock = new ReentrantLock();

    /** All subscription. */
    final Collection<Subscription> subscriptions = new ArrayList<Subscription>();

    /** Locks access to the subs */
    final Lock subscriptionsLock = new ReentrantLock();

    /** */
    final Logger logger = Logger.getLogger(this.getClass().getName());

    /* (non-Javadoc)
     * @see net.xeoh.plugins.informationbroker.InformationBroker#getInformationItem(net.xeoh.plugins.informationbroker.InformationItemIdentifier)
     */
    @SuppressWarnings("unchecked")
    final public <Type, I extends InformationItem<Type>> I getInformationItem(
                                                                              final InformationItemIdentifier<Type, I> item) {
        return (I) _getInformationItem(item);
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.informationbroker.InformationBroker#publish(net.xeoh.plugins.informationbroker.InformationItem)
     */
    @SuppressWarnings("unchecked")
    public void publish(final InformationItem<?>... newItems) {
        //TODO: More safety checks that the elements really fit in there
        for (final InformationItem item : newItems) {
            this.logger.fine("Publishing item " + item.getIdentifier().getID());
            this.itemsLock.lock();
            try {
                this.items.put(item.getIdentifier().getID(), item);
            } finally {
                this.itemsLock.unlock();
            }
        }

        // check subscriptions
        for (final InformationItem item : newItems) {
            checkSubscriptions(item.getIdentifier().getID());
        }
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.informationbroker.InformationBroker#subscribe(net.xeoh.plugins.informationbroker.InformationListener, net.xeoh.plugins.informationbroker.SubscriptionMode, net.xeoh.plugins.informationbroker.InformationItemIdentifier<?>[])
     */
    public void subscribe(InformationListener l, SubscriptionMode mode,
                          InformationItemIdentifier<?, ?>... toWaitFor) {
        this.subscriptionsLock.lock();
        try {
            this.subscriptions.add(new Subscription(l, mode, toWaitFor));
        } finally {
            this.subscriptionsLock.unlock();
        }

        // we need to check subscriptions here too (preconditions might have been already met) 
        checkSubscriptions(null);
    }

    /**
     * Asynchronously checks subscriptions 
     */
    private void checkSubscriptions(final URI justChanged) {
        final ExecutorService es = Executors.newCachedThreadPool();
        es.execute(new Runnable() {
            @SuppressWarnings("unchecked")
            public void run() {
                InformationBrokerImpl.this.subscriptionsLock.lock();
                final Collection<Subscription> copy = new ArrayList<Subscription>(InformationBrokerImpl.this.subscriptions);
                InformationBrokerImpl.this.subscriptionsLock.unlock();

                for (final Subscription subscription : copy) {

                    final InformationItemIdentifier[] allToWaitFor = subscription.allToWaitFor;

                    boolean callSubscriptionListener = false;

                    // Work depending on mode
                    switch (subscription.mode) {
                    case ALL_SET:
                        boolean allFound = true;

                        // Check if every item is found
                        for (final InformationItemIdentifier<?, ? extends InformationItem<?>> informationItemIdentifier : allToWaitFor) {
                            // Try to obtain the item.

                            final Object informationItem = _getInformationItem(informationItemIdentifier);
                            if (informationItem == null) {
                                allFound = false;
                            }
                        }

                        callSubscriptionListener = allFound;
                        break;
                    case SOME_CHANGED:
                        if (justChanged == null) {
                            break;
                        }
                        // Check if some items equals the just changed item.
                        for (final InformationItemIdentifier<?, ?> informationItemIdentifier : allToWaitFor) {
                            if (informationItemIdentifier.getID().equals(justChanged)) {
                                callSubscriptionListener = true;
                            }
                        }
                        break;
                    }

                    if (callSubscriptionListener) {
                        callListener(InformationBrokerImpl.this, subscription);
                    }

                }

                es.shutdown();
            }
        });

    }

    /**
     * We need this hack as otherwise the compiler (called from ant) complains ...
     * @param item
     * @return
     */
    @SuppressWarnings("unchecked")
    final Object _getInformationItem(final InformationItemIdentifier item) {

        this.logger.fine("Requested information item " + item.getID());

        // TODO: More casting safety checks here ...
        this.itemsLock.lock();
        try {
            final Object o = this.items.get(item.getID());
            return o;
        } finally {
            this.itemsLock.unlock();
        }
    }

}
