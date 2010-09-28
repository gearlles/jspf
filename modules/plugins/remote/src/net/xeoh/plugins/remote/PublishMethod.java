/*
 * PublishMethod.java
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
package net.xeoh.plugins.remote;

import net.xeoh.plugins.base.annotations.Capabilities;

/**
 * How an implementation publishes something. As this enum is only hardly extensible from
 * outside, we have to list here all possible methods beforehand, so other might implement
 * the plugins and use a value from in here. Currently, we only have XMLRPC.
 *
 * Note: All Remote plugins also should use the &#064;{@link Capabilities} annotation to tell their 
 * functionality.
 *
 * @author Ralf Biedert
 * @see RemoteAPI
 */
public enum PublishMethod {
    /** <a href="http://code.google.com/p/essence-rmi/">Essence RMI</a> plugin. */
    ERMI,

    /** In case you implemented a method not specified here. Use Capabilities then. */
    OTHER,
    /** Use <a href="http://en.wikipedia.org/wiki/XML-RPC">XMLRPC</a> for communication. Might have problems with void or null. */
    XMLRPC,

    /** DFKI's <a href="http://delight.opendfki.de/">XMLRPC Delight</a> service */
    XMLRPCDELIGHT,

    /** Make plugins accessible by JavaScript (currently defunct) */
    JAVASCRIPT,

    /** Make plugins accessible by JavaScript through <a href="http://jabsorb.org/">Jabsorb (JSON)</a>. Note that browser
     * restrictions might prevent you from using the plugin properly. */
    JSON,

    /**
     * <a href="http://lipermi.sourceforge.net/">Lipe RMI</a> appears to be the first sensible RMI provider that supports 
     * callbacks. The version we use however was hacked a bit to support automatic export of interfaces and contains less 
     * bugs and deadlocks. Preferred way of exporting plugins! 
     */
    LIPE
}
