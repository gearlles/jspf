/*
 * PluginMetaHandler.java
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
import java.util.List;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.impl.PluginSupervisorImpl;
import net.xeoh.plugins.base.supervision.SupervisionListener;

/**
 * @author rb
 *
 */
public class PluginMetaHandler extends InterfacedMetaHandler<Plugin> {

    /** */
    private final PluginSupervisorImpl supervisor;

    /**
     * @param supervisor 
     * @param plugin 
     */
    public PluginMetaHandler(PluginSupervisorImpl supervisor, Plugin plugin) {
        super(plugin);

        this.supervisor = supervisor;
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.base.impl.metahandling.InterfacedMetaHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // Skip simple methods
        if (method.getName().equals("hashCode")) return method.invoke(this.object, args);
        if (method.getName().equals("toString")) return method.invoke(this.object, args);
        if (method.getName().equals("getWrappedPlugin")) return this.object;

        // Call to present our supervisors and obtained supervisors
        final SupervisedCallImpl call = new SupervisedCallImpl(this.object, proxy, method, args);
        final List<SupervisionListener> allListenerFor = this.supervisor.getAllListenerFor(this.object);

        // Handle before call ...
        for (SupervisionListener l : allListenerFor) {
            final Object rval = l.beforeCall(call);
            if (call.isIntercepted()) { return rval; }
        }

        // Our return value
        Object returnValue = null;

        // Try to execute the method call
        try {
            returnValue = method.invoke(this.object, args);
        } catch (Exception t) {
            call.setException(t);
            t.printStackTrace();
        }

        // Set the return value
        call.setReturnValue(returnValue);

        // Handle after call ...
        for (SupervisionListener l : allListenerFor) {
            final Object rval = l.afterCall(call);
            if (call.isIntercepted()) { return rval; }
        }

        // In case of null, nothing is to be done here ...
        if (returnValue == null) return null;

        // Also wrap children ... (should be optional) ... fails on InformationBroker test, and any other class
        // which gets casted from an interface.
        //if (false && method.getReturnType().isInterface()) {
        //    returnValue = Proxy.newProxyInstance(returnValue.getClass().getClassLoader(), returnValue.getClass().getInterfaces(), new InterfacedMetaHandler(returnValue));
        //}

        // Return the value or new proxy...
        return returnValue;
    }
}
