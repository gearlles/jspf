/*
 * TimingSupervisor.java
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
package net.xeoh.plugins.base.util.supervision;

import java.util.HashMap;
import java.util.Map;

import net.xeoh.plugins.base.supervision.SupervisedCall;
import net.xeoh.plugins.base.supervision.SupervisionListener;

/**
 * Sample supervision listener to perform timing ...
 * 
 * @author Ralf Biedert
 */
public class TimingSupervisor implements SupervisionListener {

    /** Start times */
    final Map<SupervisedCall, Long> startTimes = new HashMap<SupervisedCall, Long>();

    /* (non-Javadoc)
     * @see net.xeoh.plugins.base.supervision.SupervisionListener#afterCall(net.xeoh.plugins.base.supervision.SupervisedCall)
     */
    @SuppressWarnings("boxing")
    public Object afterCall(SupervisedCall call) {
        synchronized (this.startTimes) {
            long start = this.startTimes.get(call);
            long stop = System.nanoTime();

            long delta = (stop - start) / 1000;

            System.out.println("Call to " + call.getPlugin().getClass().getSimpleName() + "." + call.getMethod().getName() + "() took " + delta + "µs.");

            // Remove the call
            this.startTimes.remove(call);
        }

        return null;
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.base.supervision.SupervisionListener#beforeCall(net.xeoh.plugins.base.supervision.SupervisedCall)
     */
    @SuppressWarnings("boxing")
    public Object beforeCall(SupervisedCall call) {
        synchronized (this.startTimes) {
            this.startTimes.put(call, System.nanoTime());
        }

        return null;
    }

}
