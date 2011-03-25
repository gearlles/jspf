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

import net.xeoh.plugins.diagnosis.local.Diagnosis;
import net.xeoh.plugins.diagnosis.local.DiagnosisChannelID;
import net.xeoh.plugins.diagnosis.local.DiagnosisMonitor;
import net.xeoh.plugins.diagnosis.local.util.conditions.Condition;

/**
 * @author Ralf Biedert
 */
public class DiagnosisUtil {
    /** */
    private Diagnosis diagnosis;

    /**
     * @param diagnosis
     */
    public DiagnosisUtil(Diagnosis diagnosis) {
        this.diagnosis = diagnosis;
    }
    
    /**
     * Registers a monitor listening to many channels
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
            this.diagnosis.registerMonitor(cc, (DiagnosisMonitor<Serializable>) listener);
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
}
