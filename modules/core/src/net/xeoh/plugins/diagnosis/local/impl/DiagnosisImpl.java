/*
 * DiagnosisImpl.java
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

import net.xeoh.plugins.base.PluginConfiguration;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.Shutdown;
import net.xeoh.plugins.base.util.PluginConfigurationUtil;
import net.xeoh.plugins.diagnosis.local.Diagnosis;
import net.xeoh.plugins.diagnosis.local.DiagnosisChannel;
import net.xeoh.plugins.diagnosis.local.DiagnosisChannelID;
import net.xeoh.plugins.diagnosis.local.impl.serialization.java.Entry;
import net.xeoh.plugins.diagnosis.local.impl.serialization.java.LogFileWriter;
import net.xeoh.plugins.diagnosis.local.options.ChannelOption;

@PluginImplementation
public class DiagnosisImpl implements Diagnosis {

    /** Plugin configuration (will be injected manually by the PluginManager) */
    public PluginConfiguration configuration;

    /** If true, the whole plugin will be disabled */
    boolean isDisabled = false;

    /** If true, if we should dump stack traces */
    boolean useStackTraces = false;

    /** If we should compress our output */
    boolean compressOutput = true;

    /** Depth of stack traces */
    int stackTracesDepth = 1;

    /** The file to which we record to */
    String recordingFile = null;

    /** The actual serializer we use */
    volatile LogFileWriter serializer = null;

    /*
     * (non-Javadoc)
     * 
     * @see net.xeoh.plugins.diagnosis.local.Diagnosis#channel(java.lang.Class,
     * net.xeoh.plugins.diagnosis.local.options.ChannelOption[])
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Serializable> DiagnosisChannel<T> channel(Class<? extends DiagnosisChannelID<T>> channel,
                                                                ChannelOption... options) {
        // In case we are disabled, return a dummy
        if (this.isDisabled) {
            final DiagnosisChannel<?> impl = new DiagnosisChannelDummyImpl(this, channel);
            return (DiagnosisChannel<T>) impl;
        }

        // In case this was the first call, create a serializer
        synchronized (this) {
            try {
                if (this.serializer == null) {
                    this.serializer = new LogFileWriter(this.recordingFile, this.compressOutput);
                }
            } catch (Exception e) {
                // In case something goes wrong, return a dummy
                e.printStackTrace();
                final DiagnosisChannel<?> impl = new DiagnosisChannelDummyImpl(this, channel);
                return (DiagnosisChannel<T>) impl;
            }
        }

        final DiagnosisChannel<?> impl = new DiagnosisChannelImpl(this, channel);
        return (DiagnosisChannel<T>) impl;
    }

    /**
     * Stores the given entry to our record file
     * 
     * @param entry
     */
    public void recordEntry(Entry entry) {
        this.serializer.record(entry);
    }

    /** Opens all required streams */
    @SuppressWarnings("boxing")
    // This MUST NOT be tagged with @Init, as it will be executed manually by the PluginManager.
    public void init() {
        final PluginConfigurationUtil util = new PluginConfigurationUtil(this.configuration);

        this.isDisabled = !util.getBoolean(Diagnosis.class, "recording.enabled", false);
        this.recordingFile = util.getString(Diagnosis.class, "recording.file", "diagnosis.record");
        this.useStackTraces = util.getBoolean(Diagnosis.class, "analysis.stacktraces.enabled", false);
        this.stackTracesDepth = util.getInt(Diagnosis.class, "analysis.stacktraces.depth", 1);

        String mode = util.getString(Diagnosis.class, "recording.format", "java/serialization/gzip");
        if ("java/serialization/gzip".equals(mode)) {
            this.compressOutput = true;
        }

        if ("java/serialization".equals(mode)) {
            this.compressOutput = false;
        }

    }

    /** Close the log file */
    @Shutdown
    public void shutdown() {
        // TODO
        // this.serializer...()
    }
}
