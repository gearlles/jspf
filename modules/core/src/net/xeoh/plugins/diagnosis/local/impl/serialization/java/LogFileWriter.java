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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.GZIPOutputStream;

public class LogFileWriter {

    /** Raw file to write into */
    FileOutputStream fileOutputStream;

    /** Object stream to use when writing */
    ObjectOutputStream objectOutputStream;

    /** Zip stream to compress */
    GZIPOutputStream zipStream;

    /** Entries to write into the file */
    LinkedBlockingQueue<Entry> eventQueue = new LinkedBlockingQueue<Entry>();

    /**
     * Creates a new serializer
     * 
     * @param file The file to write into.
     * @param compressOutput
     */
    public LogFileWriter(String file, boolean compressOutput) {
        try {
            this.fileOutputStream = new FileOutputStream(file);
            if (compressOutput) {
                this.zipStream = new GZIPOutputStream(this.fileOutputStream);
                this.objectOutputStream = new ObjectOutputStream(this.zipStream);
            } else
                this.objectOutputStream = new ObjectOutputStream(this.fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create recording thread
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int flushCount = 0;
                while (true) {
                    try {
                        final Entry take = LogFileWriter.this.eventQueue.take();
                        LogFileWriter.this.objectOutputStream.writeUnshared(take);

                        if (flushCount++ > 50) {
                            LogFileWriter.this.objectOutputStream.reset();
                            LogFileWriter.this.objectOutputStream.flush();
                            if (LogFileWriter.this.zipStream != null) {
                                LogFileWriter.this.zipStream.flush();
                            }
                            LogFileWriter.this.fileOutputStream.flush();
                            flushCount = 0;
                        }
                    }
                    catch (InterruptedException e) {}
                    catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
             }
        });
        thread.setName("LogFileWriter.serializer");
        thread.setDaemon(true);
        thread.start();

        // Registers a shutdown hook to flush the queue
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                terminate();
            }
        }));
    }

    /** Flushes and closes the recording streams */
    void terminate() {
        try {
            this.objectOutputStream.flush();
            if (this.zipStream != null)
                this.zipStream.flush();
            this.fileOutputStream.flush();
            // this.objectOutputStream.close();
            // this.fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Records the given entry.
     * 
     * @param entry Entry to record.
     */
    public void record(Entry entry) {
        this.eventQueue.add(entry);
    }
}
