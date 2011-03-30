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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.xeoh.plugins.diagnosis.local.DiagnosisChannelID;
import net.xeoh.plugins.diagnosis.local.util.conditions.matcher.Matcher;


/**
 * Reflects an abstract two-state condition, that tries to match against a 
 * number of requirements. 
 * 
 * @author Ralf Biedert
 */
public abstract class TwoStateMatcher extends TwoStateCondition {
    /** Stores the values we need for matching */
    final Map<Class<? extends DiagnosisChannelID<?>>, Matcher> onRequirements = new HashMap<Class<? extends DiagnosisChannelID<?>>, Matcher>();
    
    /** Stores the values we need for matching */
    final Map<Class<? extends DiagnosisChannelID<?>>, Object> currentStatus = new HashMap<Class<? extends DiagnosisChannelID<?>>, Object>();
    
    /** Additional required conditions to match. */
    final List<TwoStateCondition> requiredConditions = new ArrayList<TwoStateCondition>();
    
    
    /** */
    public TwoStateMatcher() {
        setupMatcher();
    }

    /** Override this method to set up your matcher */
    protected void setupMatcher() {
        //
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

    /**
     * Makes the condition match another dependant condition.
     * 
     * @param condition 
     */
    @SuppressWarnings("unchecked")
    public void match(TwoStateCondition condition) {
        // Require all dependant conditions 
        final Class<? extends DiagnosisChannelID<?>>[] requiredChannels = (Class<? extends DiagnosisChannelID<?>>[]) condition.getRequiredChannels();
        for (Class<? extends DiagnosisChannelID<?>> class1 : requiredChannels) {
            require(class1);
        }
        
        // And also store condition
        this.requiredConditions.add(condition);
    }
}
