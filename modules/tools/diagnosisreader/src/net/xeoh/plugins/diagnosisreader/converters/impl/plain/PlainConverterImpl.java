/*
 * ConverterImpl.java
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
package net.xeoh.plugins.diagnosisreader.converters.impl.plain;

import static net.jcores.CoreKeeper.$;

import java.io.File;
import java.io.Serializable;

import net.jcores.interfaces.functions.F1;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;
import net.xeoh.plugins.diagnosis.local.Diagnosis;
import net.xeoh.plugins.diagnosis.local.DiagnosisMonitor;
import net.xeoh.plugins.diagnosis.local.DiagnosisStatus;
import net.xeoh.plugins.diagnosis.local.options.status.OptionInfo;
import net.xeoh.plugins.diagnosisreader.converters.Converter;
import net.xeoh.plugins.diagnosisreader.converters.ConverterInfo;

/**
 * @author Ralf Biedert
 */
@PluginImplementation
public class PlainConverterImpl implements Converter {
    /** */
    @InjectPlugin
    public Diagnosis diagnosis;

    /*
     * (non-Javadoc)
     * 
     * @see net.xeoh.plugins.diagnosisreader.converters.Converter#getInfo()
     */
    @Override
    public ConverterInfo getInfo() {
        return new ConverterInfo() {

            @Override
            public String getName() {
                return "Plain Text Converter";
            }
        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.xeoh.plugins.diagnosisreader.converters.Converter#convert(java.io.File)
     */
    @Override
    public void convert(File file) {
        final StringBuilder sb = new StringBuilder();
        
        this.diagnosis.replay(file.getAbsolutePath(), new DiagnosisMonitor<Serializable>() {
            @Override
            public void onStatusChange(DiagnosisStatus<Serializable> status) {
                sb.append(status.getDate());
                sb.append(" ");
                sb.append($(status.getChannelAsString()).split("\\.").get(-1));
                sb.append(" ");
                sb.append(status.getValue());
                sb.append(" { ");
                sb.append($(status.getInfos()).map(new F1<OptionInfo, String>() {
                    @Override
                    public String f(OptionInfo arg0) {
                        return arg0.getKey() + ":" + $(arg0.getValue()).get("null");
                    }
                }).string().join(", "));
                sb.append(" }\n");
            }
        });
        
        // Write text to file
        $(file.getAbsolutePath() + ".txt").file().delete().append(sb);
    }
}
