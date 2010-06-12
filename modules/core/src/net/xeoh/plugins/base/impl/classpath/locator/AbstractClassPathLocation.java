/*
 * ClasspathLoaction.java
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
package net.xeoh.plugins.base.impl.classpath.locator;

import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.logging.Logger;

import net.xeoh.plugins.base.impl.classpath.cache.JARCache;
import net.xeoh.plugins.base.impl.classpath.cache.JARCache.JARInformation;

/**
 * Location of a classpath (i.e., either a JAR file or a toplevel directory)
 * 
 * TODO: Constrict two subclasses, JARClassPathLocation and FileClassPathLocation
 * 
 * @author rb
 * 
 */
public abstract class AbstractClassPathLocation {
    /**
     * 
     * Type of this location
     * 
     * @author rb
     * 
     */
    public enum LocationType {
        /**
         * Is a JAR
         */
        JAR,
        /**
         * Is an ordinary dir
         */
        DIRECTORY
    }

    /** */
    final Logger logger = Logger.getLogger(this.getClass().getName());

    /** Location of this item */
    final URI location;

    /** ID of this entry */
    final String realm;

    /** */
    final JARCache cache;

    /** Information for this location entry */
    JARInformation cacheEntry = null;

    /**
     * @param cache
     * @param realm
     * @param location
     */
    AbstractClassPathLocation(JARCache cache, String realm, URI location) {
        this.cache = cache;
        this.realm = realm;
        this.location = location;
    }

    /**
     * Constructs a new classpath location
     * 
     * @param cache
     * @param realm
     * @param location
     * @return .
     */
    public static AbstractClassPathLocation newClasspathLocation(JARCache cache,
                                                                 String realm,
                                                                 URI location) {
        if (location.toString().endsWith(".jar"))
            return new JARClasspathLocation(cache, realm, location);
        return new FileClasspathLocation(cache, realm, location);
    }

    /**
     * @return the location
     */
    public URI getLocation() {
        return this.location;
    }

    /**
     * @return the location
     */
    public String getRealm() {
        return this.realm;
    }

    /**
     * Get the type of this entry
     * 
     * @return .
     */
    public abstract LocationType getType();

    /**
     * Lists the name of all classes inside this classpath element
     * 
     * @return .
     */
    public abstract Collection<String> listToplevelClassNames();

    /**
     * Lists all entries in this location, no matter if class or file (excluding directories)
     * 
     * @return .
     */
    public abstract Collection<String> listAllEntries();

    /**
     * Creates an input stream for the requested item
     * 
     * @param entry
     * 
     * @return .
     */
    public abstract InputStream getInputStream(String entry);

}
