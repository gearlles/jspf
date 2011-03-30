/*
 * StatusChange.java
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
package net.xeoh.plugins.diagnosis.local;

import java.io.Serializable;

import net.xeoh.plugins.diagnosis.local.options.status.OptionInfo;

/**
 * Reflects a new diagnosis status on a given channel. 
 * 
 * @author Ralf Biedert
 *
 * @param <T>
 */
public interface DiagnosisStatus<T extends Serializable> {
    
    /**
     * Returns the channel for which some change happened.
     *  
     * @return The channel for which something happened. 
     */
    public Class<? extends DiagnosisChannelID<T>> getChannel();
    
    /**
     * Returns the channel as a string. Mostly used for replay.
     *  
     * @return String representation of the channel.
     */
    public String getChannelAsString();
    
    /**
     * Returns the new value.
     * 
     * @return The value.
     */
    public T getValue();
    
    /**
     * Returns the associated info object.
     * 
     * @return the info objects.
     */
    public OptionInfo[] getInfos();
    
    /**
     * Returns the date when this status was generated. 
     * 
     * @return The date.
     */
    public long getDate();
}
