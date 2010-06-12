/*
 * AbstractDiscoveryManager.java
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
package net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.util.PluginUtil;
import net.xeoh.plugins.remote.PublishMethod;

/**
 * @author rb
 *
 */
public abstract class AbstractDiscoveryManager {

    protected final Logger logger = Logger.getLogger(this.getClass().getName());

    /** Stores the time of startup of this plugin */
    private final long timeOfStartup = System.currentTimeMillis();

    /**
     * @param plugin
     * @return .
     */
    public static Collection<Class<? extends Plugin>> getAllPluginClasses(Plugin plugin) {
        return new PluginUtil(plugin).getAllPluginInterfaces();
    }

    /**
     * @param c
     * @return .
     */
    public static Collection<Class<?>> getAllSuperInterfaces(Class<?> c) {
        Collection<Class<?>> rval = new ArrayList<Class<?>>();

        rval.add(c);

        Class<?>[] interfaces = c.getInterfaces();
        for (Class<?> class1 : interfaces) {
            Collection<Class<?>> allSuperInterfaces = getAllSuperInterfaces(class1);
            for (Class<?> class2 : allSuperInterfaces) {
                rval.add(class2);
            }
        }
        return rval;
    }

    /**
     * @return .
     */
    public int getVersion() {
        return 200;
    }

    /**
     * @param value
     * @return .
     */
    public int ping(int value) {
        this.logger.fine("Was pinged with " + value);
        return value;
    }

    /**
     * @return .
     */
    public long getTimeSinceStartup() {
        return System.currentTimeMillis() - this.timeOfStartup;
    }

    /**
     * @param plugin
     * @param method
     * @param url
     */
    public abstract void anouncePlugin(Plugin plugin, PublishMethod method, URI url);

    /**
     * 
     * @param plugin
     * @param publishMethod
     * @param uri
     */
    public abstract void revokePlugin(Plugin plugin, PublishMethod publishMethod, URI uri);

}
