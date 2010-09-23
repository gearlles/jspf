/*
 * ClasspathLocator.java
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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import net.xeoh.plugins.base.impl.classpath.cache.JARCache;

/**
 * Used to find classpaths, JARs and their contents.  
 * 
 * @author Ralf Biedert
 */
public class ClassPathLocator {

    /** Cache to lookup elements */
    private final JARCache cache;

    /**
     * @param cache
     */
    public ClassPathLocator(JARCache cache) {
        this.cache = cache;
    }

    /**
     */
    public ClassPathLocator() {
        this(null);
    }

    /**
     * Given a top level entry, finds a list of class path locations below the given 
     * entry. The top level entry can either be a folder, or it can be a JAR directly.
     * 
     * @param toplevel The top level URI to start from. 
     * @return A list of class path locations.
     */
    public Collection<AbstractClassPathLocation> findBelow(URI toplevel) {

        final Collection<AbstractClassPathLocation> rval = new ArrayList<AbstractClassPathLocation>();
        final File startPoint = new File(toplevel);

        /*
        // First, check if the entry represents a multi-plugin
        if (startPoint.getAbsolutePath().endsWith("\\.plugin")) {
            // Let us, for the beginning, assume it is a directory based multi-plugin (in contrast to 
            // ZIP archives)
            final CoreString filter = $(startPoint).dir().string().filter("\\.jar$");
            filter.print();
            final URI[] array = filter.file().map(new F1<File, URI>() {
                public URI f(File x) {
                    return x.toURI();
                }
            }).array(URI.class);

            // rval.add(AbstractClassPathLocation.newClasspathLocation(this.cache, toplevel.toString(), array));
        }*/

        // Check if this is a directory or a file
        if (startPoint.isDirectory()) {
            final File[] listFiles = startPoint.listFiles();

            boolean hasJARs = false;

            for (File file : listFiles) {
                if (file.getAbsolutePath().endsWith(".jar")) {
                    rval.add(AbstractClassPathLocation.newClasspathLocation(this.cache, file.toURI().toString(), file.toURI()));
                    hasJARs = true;
                }
            }

            // If we have JARs, we already added them
            if (hasJARs) return rval;

            // If we have no JARs, this is probably a classpath
            rval.add(AbstractClassPathLocation.newClasspathLocation(this.cache, toplevel.toString(), toplevel));
        }

        // If this is directly a JAR, add this
        if (startPoint.isFile() && startPoint.getAbsolutePath().endsWith(".jar")) {
            rval.add(AbstractClassPathLocation.newClasspathLocation(this.cache, toplevel.toString(), toplevel));
        }

        return rval;
    }

    /**
     * Finds all locations inside the current classpath.
     * 
     * @return .
     */
    public Collection<AbstractClassPathLocation> findInCurrentClassPath() {
        final Collection<AbstractClassPathLocation> rval = new ArrayList<AbstractClassPathLocation>();

        final String pathSep = System.getProperty("path.separator");
        final String classpath = System.getProperty("java.class.path");
        final String[] split = classpath.split(pathSep);

        for (String string : split) {
            rval.add(AbstractClassPathLocation.newClasspathLocation(this.cache, "#classpath", new File(string).toURI()));
        }

        return rval;
    }
}
