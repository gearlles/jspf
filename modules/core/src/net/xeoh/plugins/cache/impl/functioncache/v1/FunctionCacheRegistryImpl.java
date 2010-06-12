/*
 * FunctionCacheRegistryImpl.java
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.cache.FunctionCacheRegistry;

/**
 * @author rb
 *
 */
@PluginImplementation
public class FunctionCacheRegistryImpl implements FunctionCacheRegistry {

    /* (non-Javadoc)
     * @see net.xeoh.plugins.cache.FunctionCacheRegistry#createCache(net.xeoh.plugins.base.Plugin)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <P extends Plugin> P createCache(final P plugin) {
        return (P) Proxy.newProxyInstance(getClass().getClassLoader(), plugin.getClass().getInterfaces(), new InvocationHandler() {

            // TODO: Add timings and see whether the direct call or the caching works faster
            // TODO: Add warning if the direct call is constantly faster
            // TODO: Call original object from time to time to see if the result changes and warn if it does
            // TODO: Add weak hash of a given size(possible?) and store the results ...
            // TODO: Add cache statistics to see the efficiency of the cache and warn if it isn't

            final Cache cache = new Cache();

            /* (non-Javadoc)
             * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
             */
            @Override
            public Object invoke(Object proxy, Method method, Object[] args)
                                                                            throws Throwable {
                // Check if we have something in the cache ...
                final CacheEntry containsCall = this.cache.getEntry(method, args);
                if (containsCall != null) { return containsCall.getResult(); }

                // If not, put it into the cache
                final Object rval = method.invoke(plugin, args);
                this.cache.addEntry(method, args, rval);

                return rval;
            }
        });
    }

}
