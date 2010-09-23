/*
 * ClassPathManager.java
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
package net.xeoh.plugins.base.impl.classpath;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import net.xeoh.plugins.base.impl.classpath.cache.JARCache.JARInformation;
import net.xeoh.plugins.base.impl.classpath.locator.AbstractClassPathLocation;
import net.xeoh.plugins.base.impl.classpath.locator.JARClasspathLocation;

import org.codehaus.classworlds.ClassRealm;
import org.codehaus.classworlds.ClassWorld;
import org.codehaus.classworlds.DuplicateRealmException;
import org.codehaus.classworlds.NoSuchRealmException;

/**
 * Manages all our classpaths shared by different plugins. 
 * 
 * @author Ralf Biedert
 */
public class ClassPathManager {
    /** */
    final Logger logger = Logger.getLogger(this.getClass().getName());

    /** */
    ClassWorld classWorld;

    /** */
    boolean initializedProperly = false;

    /** */
    public ClassPathManager() {
        // Initialization is a bit ugly ...
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                try {
                    properInit();
                } catch (SecurityException e) {
                    ClassPathManager.this.logger.warning("Proper initialization failed due to security restrictions. Only classpath://xxx URIs will work. Sorry.");
                }
                return null;
            }
        });
    }

    /**
     * Perform a proper initialization
     */
    void properInit() {
        this.classWorld = new ClassWorld();

        try {
            this.classWorld.newRealm("core", getClass().getClassLoader());
        } catch (DuplicateRealmException e) {
            e.printStackTrace();
        }

        this.initializedProperly = true;
    }

    /**
     * Loads a class given its name.
     * 
     * @param location 
     * @param name
     * 
     * @return .
     * 
     * @throws ClassNotFoundException 
     */
    public Class<?> loadClass(AbstractClassPathLocation location, String name)
                                                                              throws ClassNotFoundException {
        // In case no location is supplied ...
        if (location == null) { return getClass().getClassLoader().loadClass(name); }

        try {
            if (this.initializedProperly) {
                final ClassLoader classLoader = this.classWorld.getRealm(location.getRealm()).getClassLoader();
                return classLoader.loadClass(name);
            }
        } catch (ClassNotFoundException e) {
            return getClass().getClassLoader().loadClass(name);
        } catch (NoSuchRealmException e) {
            e.printStackTrace();
        }

        // And again, this time we run this code if we have not been inititalized properly 
        return getClass().getClassLoader().loadClass(name);
    }

    /**
     * Finds all subclasses for the given superclass.
     * 
     * @param location 
     * @param superclass
     * 
     * @return .
     */
    public Collection<String> findSubclassesFor(AbstractClassPathLocation location,
                                                Class<?> superclass) {

        final Collection<String> rval = new ArrayList<String>();
        if (!this.initializedProperly) return rval;

        // Check if we can get the requested information out of the cache
        JARInformation cacheEntry = null;

        // If it is a JAR entry, check if we have cache information
        if (location instanceof JARClasspathLocation) {
            cacheEntry = ((JARClasspathLocation) location).getCacheEntry();

            if (cacheEntry != null) {
                final Collection<String> collection = cacheEntry.subclasses.get(superclass.getCanonicalName());
                if (collection != null) return collection;
            }
        }

        // No? Okay, search the hard way ...
        try {
            final ClassLoader classLoader = this.classWorld.getRealm(location.getRealm()).getClassLoader();
            final Collection<String> listClassNames = location.listToplevelClassNames();

            for (String name : listClassNames) {
                try {
                    final Class<?> c = Class.forName(name, false, classLoader);

                    // No interfaces please
                    if (c.isInterface()) continue;

                    if (superclass.isAssignableFrom(c) && !superclass.getCanonicalName().equals(c.getCanonicalName())) {
                        rval.add(name);
                    }
                } catch (ClassNotFoundException e) {
                    this.logger.fine("ClassNotFoundException. Unable to inspect class " + name + " although it appears to be one.");

                    // Print all causes,  helpful for debugging
                    Throwable cause = e.getCause();
                    while (cause != null) {
                        this.logger.fine("Reason " + cause.getMessage());
                        cause = cause.getCause();
                    }
                } catch (final NoClassDefFoundError e) {
                    this.logger.finer("Ignored class " + name + " due to unresolved dependencies");

                    // Print all causes,  helpful for debugging
                    Throwable cause = e.getCause();
                    while (cause != null) {
                        this.logger.fine("Reason " + cause.getMessage());
                        cause = cause.getCause();
                    }
                } catch (SecurityException e) {
                    this.logger.fine("SecurityException while trying to find subclasses. Cause of trouble: " + name + ". This does not neccessarily mean problems however.");

                    // Print all causes,  helpful for debugging
                    Throwable cause = e.getCause();
                    while (cause != null) {
                        this.logger.fine("Reason " + cause.getMessage());
                        cause = cause.getCause();
                    }
                }
            }
        } catch (NoSuchRealmException e1) {
            e1.printStackTrace();
        }

        // Update the cache information
        if (cacheEntry != null) {
            cacheEntry.subclasses.put(superclass.getCanonicalName(), rval);
        }

        return rval;
    }

    /**
     * Adds a classpath location
     * 
     * @param location
     */
    public void registerLocation(AbstractClassPathLocation location) {
        if (!this.initializedProperly) return;

        try {
            final ClassRealm newRealm = this.classWorld.newRealm(location.getRealm(), getClass().getClassLoader());
            final URI[] classpathLocations = location.getClasspathLocations();
            for (URI uri : classpathLocations) {
                newRealm.addConstituent(uri.toURL());
            }

        } catch (DuplicateRealmException e) {
            // Happens for #classpath realms ...
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a resource as an input stream
     * @param location 
     * 
     * @param name
     * @return .
     */
    public InputStream getResourceAsStream(AbstractClassPathLocation location, String name) {
        // In case no location is supplied ...
        if (location == null) { return getClass().getClassLoader().getResourceAsStream(name); }

        try {
            final ClassLoader classLoader = this.classWorld.getRealm(location.getRealm()).getClassLoader();
            return classLoader.getResourceAsStream(name);
        } catch (NoSuchRealmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
