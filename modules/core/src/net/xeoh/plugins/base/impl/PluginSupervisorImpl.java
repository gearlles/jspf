/*
 * PluginSupervisorImpl.java
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
package net.xeoh.plugins.base.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginSupervisor;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.meta.Author;
import net.xeoh.plugins.base.annotations.meta.Version;
import net.xeoh.plugins.base.supervision.SupervisionListener;

/**
 * @author Ralf Biedert
 */
@PluginImplementation
@Version(version = Version.UNIT_MAJOR)
@Author(name = "Ralf Biedert")
public class PluginSupervisorImpl implements PluginSupervisor {

    /** Keeps listner used for every single plugin */
    private final List<SupervisionListener> generalListener = new ArrayList<SupervisionListener>();

    /** Keeps listner for special plugins */
    private final Map<Plugin, List<SupervisionListener>> pluginListener = new HashMap<Plugin, List<SupervisionListener>>();

    /** */
    private final Lock generalLock = new ReentrantLock();

    /** */
    private final Lock pluginLock = new ReentrantLock();

    /* (non-Javadoc)
     * @see net.xeoh.plugins.base.PluginSupervisor#addGeneralSupervisor(net.xeoh.plugins.base.supervision.SupervisionListener)
     */
    public void addGeneralSupervisor(SupervisionListener supervisor) {
        this.generalLock.lock();
        try {
            this.generalListener.add(supervisor);
        } finally {
            this.generalLock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.base.PluginSupervisor#addSupervisorFor(net.xeoh.plugins.base.Plugin, net.xeoh.plugins.base.supervision.SupervisionListener)
     */
    public void addSupervisorFor(Plugin plugin, SupervisionListener supervisor) {
        this.pluginLock.lock();
        try {
            // Create list if neccessary.
            if (!this.pluginListener.containsKey(plugin)) {
                this.pluginListener.put(plugin, new ArrayList<SupervisionListener>());
            }

            // Add the item
            this.pluginListener.get(plugin).add(supervisor);

        } finally {
            this.pluginLock.unlock();
        }
    }

    /**
     * Return all relevant listener for a plugin.
     * 
     * @param p
     * @return .
     */
    public List<SupervisionListener> getAllListenerFor(Plugin p) {
        final List<SupervisionListener> rval = new ArrayList<SupervisionListener>();

        this.pluginLock.lock();
        this.generalLock.lock();

        try {
            // Add general listener
            rval.addAll(this.generalListener);

            // Add specific listner
            if (this.pluginListener.containsKey(p))
                rval.addAll(this.pluginListener.get(p));
        } finally {
            this.pluginLock.unlock();
            this.generalLock.unlock();
        }

        return rval;
    }
}
