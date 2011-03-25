/*
 * DiagnosisStatusImpl.java
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
package net.xeoh.plugins.diagnosis.local.impl;

import java.io.Serializable;

import net.xeoh.plugins.diagnosis.local.DiagnosisChannelID;
import net.xeoh.plugins.diagnosis.local.DiagnosisStatus;
import net.xeoh.plugins.diagnosis.local.options.status.OptionInfo;

/**
 * @author Ralf Biedert
 *
 * @param <T>
 */
public class DiagnosisStatusImpl<T extends Serializable> implements DiagnosisStatus<T> {

    /** */
    private T value;
    
    /** */
    private Class<? extends DiagnosisChannelID<T>> channel;
    
    /** */
    private long date;
    
    /** */
    private OptionInfo[] infos;

    /**
     * @param channel
     * @param value
     * @param date
     * @param infos
     */
    public DiagnosisStatusImpl(Class<? extends DiagnosisChannelID<T>> channel, T value, long date, OptionInfo[] infos) {
        this.channel = channel;
        this.value = value;
        this.date = date;
        this.infos = infos;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.xeoh.plugins.diagnosis.local.DiagnosisStatus#getChannel()
     */
    @Override
    public Class<? extends DiagnosisChannelID<T>> getChannel() {
        return this.channel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.xeoh.plugins.diagnosis.local.DiagnosisStatus#getValue()
     */
    @Override
    public T getValue() {
        return this.value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.xeoh.plugins.diagnosis.local.DiagnosisStatus#getInfos()
     */
    @Override
    public OptionInfo[] getInfos() {
        return this.infos;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.xeoh.plugins.diagnosis.local.DiagnosisStatus#getDate()
     */
    @Override
    public long getDate() {
        return this.date;
    }

}
