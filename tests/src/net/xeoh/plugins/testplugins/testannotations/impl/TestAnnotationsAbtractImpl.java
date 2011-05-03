/*
 * TestInitImpl.java
 *
 * Copyright (c) 2008, Ralf Biedert All rights reserved.
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
package net.xeoh.plugins.testplugins.testannotations.impl;

import net.xeoh.plugins.base.annotations.Capabilities;
import net.xeoh.plugins.base.annotations.Thread;
import net.xeoh.plugins.base.annotations.Timer;
import net.xeoh.plugins.base.annotations.events.Init;
import net.xeoh.plugins.base.annotations.events.PluginLoaded;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;
import net.xeoh.plugins.informationbroker.InformationBroker;
import net.xeoh.plugins.informationbroker.util.InformationBrokerUtil;
import net.xeoh.plugins.remote.RemoteAPI;
import net.xeoh.plugins.testplugins.testannotations.TestAnnotations;

/**
 * @author rb
 * 
 */
public class TestAnnotationsAbtractImpl implements TestAnnotations {
    /**
     * 
     */
    @InjectPlugin
    public InformationBrokerUtil bus;

    @InjectPlugin
    public InformationBroker busbus;

    
    String init = "INIT FAILED";

    String thread = "THREAD FAILED";

    String timer = "TIMER FAILED";

    /**
     * @return .
     */
    @Capabilities
    public String[] getCapabilities() {
        return new String[] { "SUNSHINE", "RAIN" };
    }

    public String getInitStatus() {
        return this.init;
    }

    public String getInjectionStatus() {
        System.out.println("GET STATUS CALLED " + this.bus);
        return this.bus != null && this.busbus != null && this.bus.getObject() == this.busbus ? "INJECTION OK" : "INJECTION FAILED";
    }

    public String getThreadStatus() {
        return this.thread;
    }

    public String getTimerStatus() {
        return this.timer;
    }

    /**
     * 
     */
    @Init
    public void initMeLala() {
        this.init = "INIT OK";
        System.out.println("Plugin Initialized");
        // throw new NullPointerException();
    }

    /**
     * 
     */
    @Thread
    public void threadMeLala() {
        this.thread = "THREAD OK";
    }

    /**
     * 
     */
    //@Thread
    public void bigbang() {
        System.out.println("Testing VisualVM Big Bang Theory");
        double d = 3.14;
        while (true) {
            System.getenv("" + System.currentTimeMillis());
            d *= Math.sin(d);
        }
    }

    /**
     * @return .
     */
    @Timer(period = 50)
    public boolean timerMeLala() {
        this.timer = "TIMER OK";
        return true;
    }

    /**
     * @param p
     */
    @SuppressWarnings("boxing")
    @PluginLoaded
    public void newPlugin(RemoteAPI p) {
        System.out.printf("PluginLoaded (%d): %s\n", System.currentTimeMillis(), p);
    }
}
