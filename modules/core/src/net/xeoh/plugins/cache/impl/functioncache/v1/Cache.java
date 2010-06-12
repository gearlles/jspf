/*
 * Cache.java
 * 
 * Copyright (c) 2010, Ralf Biedert All rights reserved.
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
package net.xeoh.plugins.cache.impl.functioncache.v1;

import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author rb
 *
 */
public class Cache {

    /** */
    private final ArrayList<SoftReference<CacheEntry>> entries = new ArrayList<SoftReference<CacheEntry>>(200);

    /**
     * 
     */
    public Cache() {
        //
    }

    /**
     * @param method
     * @param _args
     * @return .
     */
    public synchronized CacheEntry getEntry(Method method, Object[] _args) {
        final Collection<SoftReference<CacheEntry>> toRemove = new ArrayList<SoftReference<CacheEntry>>();

        final Object[] args = (_args == null) ? new Object[0] : _args;

        CacheEntry rval = null;

        // Try to retrieve the entry  
        for (SoftReference<CacheEntry> entry : this.entries) {

            // First check if that element is still in there ...
            final CacheEntry e = entry.get();
            if (e == null) {
                toRemove.add(entry);
                continue;
            }

            // Then check if the method name is the same
            Object[] a = e.getArgs();
            if (!e.getMethod().equals(method)) continue;
            if (a.length != args.length) continue;

            boolean skip = false;

            // And the args
            for (int i = 0; i < args.length; i++) {
                if (!a[i].equals(args[i])) {
                    skip = true;
                    break;
                }
            }

            if (skip) continue;

            // Must be our element
            rval = e;
        }

        // Clean up unused
        this.entries.removeAll(toRemove);

        return rval;
    }

    /**
     * @param method
     * @param args
     * @param rval
     */
    public synchronized void addEntry(Method method, Object[] args, Object rval) {
        this.entries.add(new SoftReference<CacheEntry>(new CacheEntry(method, args, rval)));
    }
}
