/*
 * DiagnosisChannel.java
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

import static net.jcores.CoreKeeper.$;
import net.xeoh.plugins.diagnosis.local.DiagnosisChannel;
import net.xeoh.plugins.diagnosis.local.DiagnosisChannelID;
import net.xeoh.plugins.diagnosis.local.impl.serialization.java.Entry;
import net.xeoh.plugins.diagnosis.local.options.StatusOption;

public class DiagnosisChannelImpl implements DiagnosisChannel<Object> {

    /** Main diagnosis */
    private final DiagnosisImpl diagnosis;

    /** Main channel */
    private final Class<? extends DiagnosisChannelID<?>> channel;

    /**
     * Creates a new channel
     * 
     * @param diagnosis
     * 
     * @param channel
     */
    public DiagnosisChannelImpl(DiagnosisImpl diagnosis,
                                Class<? extends DiagnosisChannelID<?>> channel) {
        this.diagnosis = diagnosis;
        this.channel = channel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.xeoh.plugins.diagnosis.local.DiagnosisChannel#status(java.lang.Object)
     */
    @Override
    public void status(Object value, StatusOption... options) {
        final long timestamp = System.currentTimeMillis();
        final long id = Thread.currentThread().getId();

        final Entry entry = new Entry();
        entry.date = timestamp;
        entry.threadID = id;
        entry.channel = this.channel.getCanonicalName();
        entry.value = value;

        // Generate stack trace if requested
        if (this.diagnosis.useStackTraces) {
            final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            entry.stackTrace = $(stackTrace).slice(2, Math.min(this.diagnosis.stackTracesDepth, stackTrace.length - 2)).string().array(String.class);
        }

        this.diagnosis.recordEntry(entry);
    }
}
