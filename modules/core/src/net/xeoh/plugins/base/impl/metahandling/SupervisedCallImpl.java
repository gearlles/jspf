/*
 * SupervisedCallImpl.java
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
package net.xeoh.plugins.base.impl.metahandling;

import java.lang.reflect.Method;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.supervision.SupervisedCall;

/**
 * @author Ralf Biedert
 *
 */
public class SupervisedCallImpl implements SupervisedCall {

    /** */
    private final Plugin plugin;

    /** */
    @SuppressWarnings("unused")
    private final Object proxy;

    /** */
    private final Method method;

    /** */
    private final Object[] args;

    /** Speficies if the call was requested to be intercepted */
    private boolean intercepted = false;

    /** */
    private Exception exception;

    /** */
    private Object returnValue;

    /**
     * @param plugin 
     * @param proxy
     * @param method
     * @param args
     */
    public SupervisedCallImpl(Plugin plugin, Object proxy, Method method, Object[] args) {
        this.plugin = plugin;
        this.proxy = proxy;
        this.method = method;
        this.args = args;
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.base.supervision.SupervisedCall#getArguments()
     */
    public Object[] getArguments() {
        return this.args;
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.base.supervision.SupervisedCall#getMethod()
     */
    public Method getMethod() {
        return this.method;
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.base.supervision.SupervisedCall#getPlugin()
     */
    public Plugin getPlugin() {
        return this.plugin;
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.base.supervision.SupervisedCall#getReturnValue()
     */
    public Object getReturnValue() {
        return this.returnValue;
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.base.supervision.SupervisedCall#getThrownException()
     */
    public Exception getThrownException() {
        return this.exception;
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.base.supervision.SupervisedCall#interceptCall()
     */
    public void interceptCall() {
        this.intercepted = true;
    }

    /**
     * @return .
     */
    public boolean isIntercepted() {
        return this.intercepted;
    }

    /**
     * @param t
     */
    public void setException(Exception t) {
        this.exception = t;
    }

    /**
     * @param returnValue
     */
    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }
}
