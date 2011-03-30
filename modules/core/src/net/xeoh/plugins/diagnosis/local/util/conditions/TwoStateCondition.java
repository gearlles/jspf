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


/**
 * Reflects an abstract two-state condition, that can either be on, or off. 
 * 
 * @author Ralf Biedert
 */
public abstract class TwoStateCondition extends Condition {
    /** The state of this condition */
    public static enum STATE { ON, OFF }
    
    /** Memorize last state we had */
    STATE lastState = STATE.OFF;
    
    
    /**
     * Announces the state in case it differs from our last announcement.
     * 
     * @param state
     */
    public void announceState(STATE state) {
        if(state == this.lastState) return;
        stateChanged(state);
        this.lastState = state;
    }
    
    /**
     * Called when the state has changed based on some matching input. 
     * 
     * @param state The new state.
     */
    public abstract void stateChanged(STATE state);
    
    /**
     * Returns the current state of this condition.
     * 
     * @return The current state.
     */
    public STATE getState() {
        return this.lastState;
    }
}
