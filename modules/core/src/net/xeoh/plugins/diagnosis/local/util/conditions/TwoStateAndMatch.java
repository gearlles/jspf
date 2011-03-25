/*
 * TwoStateCondition.java
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
package net.xeoh.plugins.diagnosis.local.util.conditions;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.xeoh.plugins.diagnosis.local.DiagnosisChannelID;
import net.xeoh.plugins.diagnosis.local.DiagnosisStatus;
import net.xeoh.plugins.diagnosis.local.util.conditions.matcher.Matcher;


/**
 * Reflects an abstract two-state condition, that can either be on, or off. 
 * 
 * @author Ralf Biedert
 */
public abstract class TwoStateAndMatch extends TwoStateCondition {
    /** Stores the values we need for matching */
    Map<Class<? extends DiagnosisChannelID<?>>, Matcher> onRequirements = new HashMap<Class<? extends DiagnosisChannelID<?>>, Matcher>();

    /** Stores the values we need for matching */
    Map<Class<? extends DiagnosisChannelID<?>>, Object> currentStatus = new HashMap<Class<? extends DiagnosisChannelID<?>>, Object>();
    
    /** */
    public TwoStateAndMatch() {
        setupMatcher();
    }

    /** Override this method to set up your matcher */
    protected void setupMatcher() {
    }

    /**
     * Makes the condition match a number of channel states (linked with AND).
     * 
     * @param <T>
     * @param channel
     * @param matcher
     */
    public <T extends Serializable> void match(Class<? extends DiagnosisChannelID<T>> channel, Matcher matcher) {
        require(channel);
        this.onRequirements.put(channel, matcher);
    }
    
    
    /* (non-Javadoc)
     * @see net.xeoh.plugins.diagnosis.local.DiagnosisMonitor#onStatusChange(net.xeoh.plugins.diagnosis.local.DiagnosisStatus)
     */
    @Override
    public void onStatusChange(DiagnosisStatus<Serializable> status) {
        // First, update our values
        this.currentStatus.put(status.getChannel(), status.getValue());
        
        // Next match status agains on status
        Set<Class<? extends DiagnosisChannelID<?>>> keySet = this.onRequirements.keySet();
        for (Class<? extends DiagnosisChannelID<?>> c : keySet) {
            final Matcher requirement = this.onRequirements.get(c);
            final Object is = this.currentStatus.get(c);
            
            if(!requirement.matches(is)) {
                announceState(STATE.OFF);
                return;
            }
        }
        
        announceState(STATE.ON);
    }
}
