/*
 * DiagnosisUtil.java
 * 
 * Copyright (c) 2011, Ralf Biedert All rights reserved.
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
package net.xeoh.plugins.diagnosis.local.util;

import java.io.Serializable;

import net.xeoh.plugins.base.util.VanillaPluginUtil;
import net.xeoh.plugins.diagnosis.local.Diagnosis;
import net.xeoh.plugins.diagnosis.local.DiagnosisChannel;
import net.xeoh.plugins.diagnosis.local.DiagnosisChannelID;
import net.xeoh.plugins.diagnosis.local.DiagnosisMonitor;
import net.xeoh.plugins.diagnosis.local.options.ChannelOption;
import net.xeoh.plugins.diagnosis.local.util.conditions.Condition;

/**
 * @author Ralf Biedert
 */
public class DiagnosisUtil extends VanillaPluginUtil<Diagnosis> implements Diagnosis {
    /**
     * @param diagnosis
     */
    public DiagnosisUtil(Diagnosis diagnosis) {
        super(diagnosis);
    }
    
    /**
     * Registers a monitor listening to many channels.
     *  
     * @param listener
     * @param all
     */
    @SuppressWarnings("unchecked")
    public void registerMonitors(final DiagnosisMonitor<?> listener,
                             final Class<?>... all) {
        if (listener == null || all == null || all.length == 0) return;

        // Stores all items we received so far
        for (final Class<?> c : all) {
            final Class<? extends DiagnosisChannelID<Serializable>> cc = (Class<? extends DiagnosisChannelID<Serializable>>) c;
            this.object.registerMonitor(cc, (DiagnosisMonitor<Serializable>) listener);
        }
    }
    
    /**
     * Registers a condition to the enclode diagnosis.
     * 
     * @param condition
     */
    public void registerCondition(Condition condition) {
        if(condition == null) return;

        registerMonitors(condition, condition.getRequiredChannels());
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.diagnosis.local.Diagnosis#channel(java.lang.Class, net.xeoh.plugins.diagnosis.local.options.ChannelOption[])
     */
    @Override
    public <T extends Serializable> DiagnosisChannel<T> channel(Class<? extends DiagnosisChannelID<T>> channel,
                                                                ChannelOption... options) {
        return this.object.channel(channel, options);
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.diagnosis.local.Diagnosis#registerMonitor(java.lang.Class, net.xeoh.plugins.diagnosis.local.DiagnosisMonitor)
     */
    @Override
    public <T extends Serializable> void registerMonitor(Class<? extends DiagnosisChannelID<T>> channel,
                                                         DiagnosisMonitor<T> listener) {
        this.object.registerMonitor(channel, listener);
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.diagnosis.local.Diagnosis#replay(java.lang.String, net.xeoh.plugins.diagnosis.local.DiagnosisMonitor)
     */
    @Override
    public void replay(String file, DiagnosisMonitor<?> listener) {
        this.object.replay(file, listener);
    }
}
