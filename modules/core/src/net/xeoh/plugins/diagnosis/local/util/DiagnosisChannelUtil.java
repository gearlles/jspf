/*
 * DiagnosisChannelUtil.java
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

import static net.jcores.CoreKeeper.$;

import java.io.Serializable;

import net.jcores.interfaces.functions.Fn;
import net.jcores.utils.VanillaUtil;
import net.xeoh.plugins.diagnosis.local.DiagnosisChannel;
import net.xeoh.plugins.diagnosis.local.options.StatusOption;
import net.xeoh.plugins.diagnosis.local.options.status.OptionInfo;

/**
 * Wraps a {@link DiagnosisChannel} and provides helper functions.
 * 
 * @author Ralf Biedert
 *
 * @param <T> The type of the diagnosis object.
 */
public class DiagnosisChannelUtil<T> extends VanillaUtil<DiagnosisChannel<T>> implements DiagnosisChannel<T> {

    /**
     * Creates a new util with the given channel.
     * 
     * @param object
     */
    public DiagnosisChannelUtil(DiagnosisChannel<T> object) {
        super(object);
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.diagnosis.local.DiagnosisChannel#status(java.lang.Object, net.xeoh.plugins.diagnosis.local.options.StatusOption[])
     */
    @Override
    public void status(T value, StatusOption... options) {
        this.object.status(value, options);
    }
    
    /**
     * Logs the value and creates an {@link OptionInfo} for each 
     * two info parameters (key, value).
     * 
     * @param value The value to log.
     * @param infos The info parameters.
     */
    public void status(T value, Serializable... infos) {
        if(infos == null) {
            this.object.status(value);
            return;
        }

        // Convert the options we have
        final OptionInfo[] options = $(infos).forEach(new Fn<Serializable, OptionInfo>() {
            @Override
            public OptionInfo f(Serializable... arg0) {
                return new OptionInfo(arg0[0].toString(), arg0[1]);
            }
        }, 2).array(OptionInfo.class);
        
        this.object.status(value, options);
    }
}
