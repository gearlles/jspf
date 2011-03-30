/*
 * Diagnosis.java
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

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginConfiguration;
import net.xeoh.plugins.base.util.JSPFProperties;
import net.xeoh.plugins.diagnosis.local.options.ChannelOption;

/**
 * Main enty point to the diagnosis. Allows your application to record diagnosis data to various channels, which
 * can then be stored in a diagnosis.record for later analysis. 
 * 
 * 
 * The following configuration sub-keys are usually known for this class (see {@link PluginConfiguration}, keys must be set 
 * <em>before</em> createPluginManager() is being called, i.e., set in the {@link JSPFProperties} object!):<br/><br/>
 * 
 *  <ul>
 *  <li><b>recording.enabled</b> - If we should record to a file or not. If switched off, diagnosis has virtually no overhead. Specify either {true, false}.</li>
 *  <li><b>recording.file</b> - File to which the record should be writte (will be overwritten).</li>
 *  <li><b>recording.format</b> - Format to write. Should be <code>java/serialization</code> for now.</li>
 *  <li><b>analysis.stacktraces.enabled</b> - If true, a stack trace will also be written. Very helpful, rather slow. Specify either {true, false}.</li>
 *  <li><b>analysis.stacktraces.depth</b> - Depth of the stacktrace. Specify either something from 1 to 10000.</li>
 *  </ul><br/>
 *  
 * @author Ralf Biedert
 * @since 1.1
 */
public interface Diagnosis extends Plugin {
    /**
     * Returns a given channel.
     * 
     * @param <T>
     * @param channel
     * @param options
     * @return .
     */
    public <T extends Serializable> DiagnosisChannel<T> channel(Class<? extends DiagnosisChannelID<T>> channel,
                                                                ChannelOption... options);

    /**
     * Registers a listener to the diagnosis.
     * 
     * @param <T> 
     * @param channel 
     * @param listener 
     */
    public  <T extends Serializable> void registerMonitor(Class<? extends DiagnosisChannelID<T>> channel, DiagnosisMonitor<T> listener);
    
    
    /**
     * Adds a replay listener for a given file.
     * 
     * @param file
     * @param listener
     */
    public void replay(String file, DiagnosisMonitor<?> listener); 
}
