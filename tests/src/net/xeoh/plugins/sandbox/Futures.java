/*
 * Futures.java
 * 
 * Copyright (c) 2009, Ralf Biedert All rights reserved.
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
package net.xeoh.plugins.sandbox;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author rb
 *
 */
public class Futures {
    /**
     * @param args
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {

        // Execute collection asynchronously
        ExecutorService newCachedThreadPool = Executors.newCachedThreadPool();
        final ExecutorCompletionService<String> ecs = new ExecutorCompletionService(newCachedThreadPool);
        Future<String> submit = ecs.submit(new Callable<String>() {

            public String call() {
                try {
                    Thread.sleep(1012331872);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return "Yo";
            }
        });

        // Wait at most half a second (TODO: Make this configurable)
        try {

            String string = submit.get(500, TimeUnit.MILLISECONDS);
            if (string == null) {
                System.out.println("OOPs");
                return;
            }

            System.out.println("Okay " + string);
        } catch (final InterruptedException e) {
            System.err.println("Error while waiting for a getRemoteProxy() result");
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        } catch (final TimeoutException e) {
            e.printStackTrace();
        } catch (final SecurityException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Cancelling");
            newCachedThreadPool.shutdownNow();
            submit.cancel(true);
        }

        System.out.println("xx");
    }
}
