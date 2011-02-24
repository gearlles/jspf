/*
 * Entry.java
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
package net.xeoh.plugins.diagnosis.local.impl.serialization.java;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Reflects an entry on a given channel.
 * 
 * @author Ralf Biedert
 */
public class Entry implements Serializable {
    /** */
    private static final long serialVersionUID = -361673738793578516L;

    /** The actual version we serialize */
    private short version = 1;

    /** Specifies when this entry was observed */
    public long date;

    /** Thread ID for which this entry was observed */
    public long threadID;

    /** Stack trace for this call */
    public String[] stackTrace;

    /** Channel this entry was observed on */
    public String channel;

    /** Value that was observed */
    public Object value;

    /** Additional information */
    public Map<String, Object> additionalInfo = new HashMap<String, Object>();

    /**
     * We do this ourself.
     * 
     * @param stream
     * @throws IOException
     */
    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        stream.writeShort(this.version);
        stream.writeLong(this.date);
        stream.writeLong(this.threadID);
        stream.writeUnshared(this.stackTrace);
        stream.writeUnshared(this.channel);
        stream.writeUnshared(this.value);
        stream.writeUnshared(this.additionalInfo);
    }

    /**
     * We do this ourself.
     * 
     * @param stream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream stream) throws IOException,
                                                             ClassNotFoundException {

        this.version = stream.readShort();

        if (this.version != 1)
            throw new ClassNotFoundException("Version mismatch, cannot handle " + this.version);

        this.date = stream.readLong();
        this.threadID = stream.readLong();
        this.stackTrace = (String[]) stream.readUnshared();
        this.channel = (String) stream.readUnshared();
        this.value = stream.readUnshared();
        try {
            this.additionalInfo = (Map<String, Object>) stream.readUnshared();
        } catch (ClassNotFoundException e) {
            System.err.println("Unknown type in infos (" + e.getMessage() + "). You should run this in the orig app's classpath!");
        }
    }
}
