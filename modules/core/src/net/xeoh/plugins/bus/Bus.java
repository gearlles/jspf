/*
 * Bus.java
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
package net.xeoh.plugins.bus;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.bus.messages.BusMessage;

/**
 * The bus should be used for optional "low profile" messages, callbacks and so on, like
 * the standard Java listener, however without the hassle of bloating your plugins with
 * add / remove / ... functions. <br>
 * <br>
 * All receivers will obtain the same message, so your message-interface and/or your
 * callees should ensure it won't be altered. <br>
 * <br>
 *
 * TODO: removeChannelListener()
 *
 * @author Ralf Biedert
 */
public interface Bus extends Plugin {
    /**
     * Adds a listener to a given channel. Any object can be used to specify a channel,
     * however, using interface classes is recommended. There is no act of explicitly
     * creating a channel. As soon as as someone sends on a channel you have registered
     * to, you will receive the message. <br>
     * <br>
     * As an implementation might choose to process all listener sequentially they should
     * return quickly.
     * @param <B> 
     * @param <C> 
     *
     * @param channel
     * @param listener
     */
    public <B extends BusMessage, C extends Channel<B>> void addChannelListener(
                                                                                Class<C> channel,
                                                                                ChannelListener<B> listener);

    /**
     * Sends a message on the given channel. No channel has to be created explicitly
     * before using this function.<br>
     * <br>
     *
     * Credits to 'surial' from the #java-channel on freenode.net for solving a generics
     * 'feature' that required you to an anonymous BusMessage to its 'parent' bus message.
     * @param channel 
     * @param message 
     * @param <B> 
     * @param <D> 
     * @param <C> 
     *
     */
    public <B extends BusMessage, D extends B, C extends Channel<B>> void sendOnChannel(
                                                                                        Class<C> channel,
                                                                                        D message);
    // public <B extends BusMessage, C extends Channel<B>> void sendOnChannel(Class<C>
    // channel, B message);
}
