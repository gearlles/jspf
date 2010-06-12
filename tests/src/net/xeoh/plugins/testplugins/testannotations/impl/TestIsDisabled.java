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
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.Timer;
import net.xeoh.plugins.base.annotations.configuration.IsDisabled;
import net.xeoh.plugins.base.annotations.meta.Author;
import net.xeoh.plugins.base.annotations.meta.Version;
import net.xeoh.plugins.testplugins.testannotations.TestAnnotations;

/**
 * @author rb
 *
 */
@IsDisabled
@Version(version = 1000000)
@Author(name = "Ralf Biedert")
@PluginImplementation
public class TestIsDisabled implements TestAnnotations {

    /**
     * @return .
     */
    @Capabilities
    public String[] getCapabilities() {
        return new String[] { "IsDisabled" };
    }

    /**
     * @return .
     */
    @Timer(period = 50)
    public boolean timerMeLala() {
        System.out.println("THIS MUST NEVER BE PRINTED OR YOUR IsDisabled does not work!");
        return false;
    }

    public String getInitStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getInjectionStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getThreadStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getTimerStatus() {
        // TODO Auto-generated method stub
        return null;
    }

}
