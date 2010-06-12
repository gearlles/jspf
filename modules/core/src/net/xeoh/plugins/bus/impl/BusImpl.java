/*
 * BusImpl.java
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
package net.xeoh.plugins.bus.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.Init;
import net.xeoh.plugins.base.annotations.meta.Author;
import net.xeoh.plugins.bus.Bus;
import net.xeoh.plugins.bus.Channel;
import net.xeoh.plugins.bus.ChannelListener;
import net.xeoh.plugins.bus.messages.BusMessage;

/**
 * Thou shalt not peek in here.
 *
 * @author Ralf Biedert
 */
/**
 * @author rb
 *
 */
@Author(name = "Ralf Biedert")
@PluginImplementation
public class BusImpl implements Bus {

    /**
     * List of all known listeners
     */
    @SuppressWarnings("unchecked")
    HashMap<Class<? extends Channel>, List<ChannelListener<?>>> knownListeners;

    /**
     * Threads may be evil
     */
    Lock knownListenersLock = new ReentrantLock();

    /* (non-Javadoc)
     * @see net.xeoh.plugins.bus.Bus#addChannelListener(java.lang.Class, net.xeoh.plugins.bus.ChannelListener)
     */
    public <B extends BusMessage, C extends Channel<B>> void addChannelListener(
                                                                                final Class<C> channel,
                                                                                final ChannelListener<B> listener) {

        // Check parameter
        if (channel == null || listener == null) return;

        // Some assertions
        assert this.knownListeners != null : "Lister must have been initialized!";

        // Return a list of all channel listeners
        this.knownListenersLock.lock();

        try {
            List<ChannelListener<?>> list = this.knownListeners.get(channel);

            // Create a new list if we dont have any
            if (list == null) {
                list = new ArrayList<ChannelListener<?>>();
                this.knownListeners.put(channel, list);
            }

            // Finally add the listener
            list.add(listener);
        } finally {
            this.knownListenersLock.unlock();
        }
    }

    /** */
    @SuppressWarnings("unchecked")
    @Init
    public void init() {
        this.knownListeners = new HashMap<Class<? extends Channel>, List<ChannelListener<?>>>();
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.bus.Bus#sendOnChannel(java.lang.Class, net.xeoh.plugins.bus.messages.BusMessage)
     */
    @SuppressWarnings("unchecked")
    public <B extends BusMessage, D extends B, C extends Channel<B>> void sendOnChannel(
                                                                                        final Class<C> channel,
                                                                                        final D message) {
        // Check parameter
        if (channel == null || message == null) return;

        // Obtain proper list
        this.knownListenersLock.lock();

        // TODO: Optimize this, as currently all channels are locked until the message is
        // delivered
        try {
            final List<ChannelListener<?>> _list = this.knownListeners.get(channel);
            if (_list == null) return;
            final List<ChannelListener<?>> list = new ArrayList<ChannelListener<?>>(_list);

            // And propagate message.
            for (final ChannelListener channelListener : list) {
                try {
                    channelListener.incomingMessage(message);
                } catch (final Exception e) {
                    // In case of an error, ensure the other listner will receive their event.
                    e.printStackTrace();
                }
            }

        } finally {
            this.knownListenersLock.unlock();
        }
    }
}
