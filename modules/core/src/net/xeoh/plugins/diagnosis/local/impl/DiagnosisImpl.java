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

import static net.jcores.CoreKeeper.$;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.xeoh.plugins.base.PluginConfiguration;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.Shutdown;
import net.xeoh.plugins.base.util.PluginConfigurationUtil;
import net.xeoh.plugins.diagnosis.local.Diagnosis;
import net.xeoh.plugins.diagnosis.local.DiagnosisChannel;
import net.xeoh.plugins.diagnosis.local.DiagnosisChannelID;
import net.xeoh.plugins.diagnosis.local.DiagnosisMonitor;
import net.xeoh.plugins.diagnosis.local.DiagnosisStatus;
import net.xeoh.plugins.diagnosis.local.impl.serialization.java.Entry;
import net.xeoh.plugins.diagnosis.local.impl.serialization.java.EntryCallback;
import net.xeoh.plugins.diagnosis.local.impl.serialization.java.LogFileReader;
import net.xeoh.plugins.diagnosis.local.impl.serialization.java.LogFileWriter;
import net.xeoh.plugins.diagnosis.local.options.ChannelOption;
import net.xeoh.plugins.diagnosis.local.options.status.OptionInfo;

@PluginImplementation
public class DiagnosisImpl implements Diagnosis {
    /** Stores information on a key */
    class KeyEntry {
        /** Locks access to this item */
        final Lock entryLock = new ReentrantLock();

        /** All listeners subscribed to this item */
        final Collection<DiagnosisMonitor<?>> allListeners = new ArrayList<DiagnosisMonitor<?>>();

        /** The current channel holder */
        DiagnosisStatus<?> lastItem = null;
    }

    /** Manages all information regarding a key */
    final Map<Class<? extends DiagnosisChannelID<?>>, KeyEntry> items = new ConcurrentHashMap<Class<? extends DiagnosisChannelID<?>>, KeyEntry>();

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

    /** If recording should be enabled */
    boolean recordingEnabled = false;

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
            if (this.serializer == null && this.recordingEnabled) {
                this.serializer = createWriter();
            }
        }

        final DiagnosisChannel<?> impl = new DiagnosisChannelImpl(this, channel);
        return (DiagnosisChannel<T>) impl;
    }

    
    /**
     * Try to create a write for our logging data.
     * 
     * @return
     */
    LogFileWriter createWriter() {
        // First try to return a writer with the given name
        try {
            return new LogFileWriter(this.recordingFile, this.compressOutput);
        } catch (Exception e) {}

        // First try to return a writer with some time stamp attached (in case the old one was not overwritable)
        try {
            return new LogFileWriter(this.recordingFile + "." + System.currentTimeMillis(), this.compressOutput);
        } catch (Exception e) {}

        // Now we are out of luck
        return null;
    }

    /**
     * Stores the given entry to our record file
     * 
     * @param status
     * @param entry
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void recordEntry(DiagnosisStatus<?> status, Entry entry) {
        if (this.serializer != null)
            this.serializer.record(entry);

        final KeyEntry keyEntry = getKeyEntry(status.getChannel());


        
        // Now process entry
        try {
            keyEntry.entryLock.lock();
            keyEntry.lastItem = status;

            
            // Check if we should publish silently.
            for (DiagnosisMonitor<?> listener : keyEntry.allListeners) {
                try {
                    ((DiagnosisMonitor<?>) listener).onStatusChange((DiagnosisStatus) status);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } finally {
            keyEntry.entryLock.unlock();
        }
    }

    /** Opens all required streams */
    @SuppressWarnings("boxing")
    // This MUST NOT be tagged with @Init, as it will be executed manually by the PluginManager.
    public void init() {
        final PluginConfigurationUtil util = new PluginConfigurationUtil(this.configuration);

        this.isDisabled = !util.getBoolean(Diagnosis.class, "general.enabled", true);
        this.recordingEnabled = util.getBoolean(Diagnosis.class, "recording.enabled", false);
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

    /*
     * (non-Javadoc)
     * 
     * @see net.xeoh.plugins.diagnosis.local.Diagnosis#registerListener(java.lang.Class,
     * net.xeoh.plugins.diagnosis.local.DiagnosisListener)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Serializable> void registerMonitor(Class<? extends DiagnosisChannelID<T>> channel,
                                                          DiagnosisMonitor<T> listener) {
        if (channel == null || listener == null) return;

        // Get the meta information for the requested id
        final KeyEntry keyEntry = getKeyEntry(channel);

        
        // Now process and add the entry
        try {
            keyEntry.entryLock.lock();
            // If there has been a channel established, use that one
            if (keyEntry.lastItem != null) {
                try {
                    listener.onStatusChange((DiagnosisStatus<T>) keyEntry.lastItem);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            keyEntry.allListeners.add(listener);
        } finally {
            keyEntry.entryLock.unlock();
        }
    }

    /**
     * Returns the key entry of a given ID.
     * 
     * @param id The ID to request
     * @return The key entry.
     */
    private KeyEntry getKeyEntry(Class<? extends DiagnosisChannelID<?>> id) {
        KeyEntry keyEntry = null;
        
        synchronized (this.items) {
            keyEntry = this.items.get(id);
            if (keyEntry == null) {
                keyEntry = new KeyEntry();
                this.items.put(id, keyEntry);
            }
        }

        return keyEntry;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.xeoh.plugins.diagnosis.local.Diagnosis#replay(java.lang.String,
     * net.xeoh.plugins.diagnosis.local.DiagnosisMonitor)
     */
    @Override
    public void replay(final String file, final DiagnosisMonitor<?> listener) {
        final LogFileReader reader = new LogFileReader(file);
        reader.replay(new EntryCallback() {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            public void nextEntry(Entry entry) {
                // Convert the entry to a status events
                final List<OptionInfo> infos = new ArrayList<OptionInfo>();
                for (String value : entry.additionalInfo.keySet()) {
                    infos.add(new OptionInfo(value, (Serializable) entry.additionalInfo.get(value)));
                }

                final DiagnosisStatusImpl<?> event = new DiagnosisStatusImpl(entry.channel, (Serializable) entry.value, entry.date, $(infos).array(OptionInfo.class));
                listener.onStatusChange((DiagnosisStatus) event);
            }
        });
    }
}
