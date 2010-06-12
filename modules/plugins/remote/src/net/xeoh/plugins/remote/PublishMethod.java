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

/**
 * How an implementation publishes something. As this enum is only hardly extensible from
 * outside, we have to list here all possible methdos beforehand, so other might implement
 * the plugins and use a value from in here. Currently, we only have XMLRPC.
 *
 * Note: All Remote plugins also should use the Capabilities to tell their functionality.
 *
 * @author Ralf Biedert
 *
 */
public enum PublishMethod {
    /**
     * Essential RMI
     */
    ERMI,
    /**
     * In case you implemented a method not specified here. Use Capabilities then.
     */
    OTHER,
    /**
     * Use XMLRPC for communication. Might have problems with void or null.
     */
    XMLRPC,
    
    /**
     * DFKI's XMLRPC Delight service
     */
    XMLRPCDELIGHT,
   
    /**
     * Make plugins accessible by JavaScript
     */
    JAVASCRIPT,
    /**
     * Make plugins accessible by JavaScript through Jabsorb (JSON)
     */
    JSON,

    /**
     * Lipe RMI appears to be the first sensible RMI provider that supports callbacks. This version however was hacked a bit to support
     * automatic exports of interfaces.
     */
    LIPE
}
