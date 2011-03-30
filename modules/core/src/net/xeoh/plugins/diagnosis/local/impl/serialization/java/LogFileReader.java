/*
 * SerializationFile.java
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

import static net.jcores.CoreKeeper.$;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

public class LogFileReader {
    /** */
    private final String file;

    /**
     * Creates a new serializer
     * 
     * @param file The file to write into.
     */
    public LogFileReader(String file) {
        this.file = file;
    }

    /**
     * @param callback
     */
    public void replay(EntryCallback callback) {
        try {
            final ObjectInputStream stream = new ObjectInputStream(new FileInputStream(this.file));
            while (true) {
                try {
                    callback.nextEntry((Entry) stream.readObject());
                } catch(Exception e) {
                    if(e instanceof EOFException) break;
                    if(e instanceof ClassNotFoundException) { System.err.println("Skipping one entry due to a class not found: " + e.getMessage()); continue; }
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // e.printStackTrace();
            System.out.println("End of File");
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        final LogFileReader reader = new LogFileReader("diagnosis.record");
        reader.replay(new EntryCallback() {
            @Override
            public void nextEntry(Entry entry) {
                final long time = entry.date;
                final String name = $(entry.channel).split("\\.").get(-1);
                String opts = "";
                for (String string : entry.additionalInfo.keySet()) {
                    opts += ":" + string + "=" + entry.additionalInfo.get(string);
                }

                if(opts.length() > 0)
                opts = opts.substring(1);

                String output = time + "," + name + "," + entry.value + "," + opts;
                if (output.contains("BrowserPluginTracer") && output.contains("callfunction/start"))
                    System.out.println(output);
            }
        });
    }
}
