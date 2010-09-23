/*
 * ClassFinderTest.java
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
package net.xeoh.plugins.sandbox;

import java.net.MalformedURLException;
import java.util.Collection;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.impl.classpath.ClassPathManager;
import net.xeoh.plugins.base.impl.classpath.cache.JARCache;
import net.xeoh.plugins.base.impl.classpath.locator.AbstractClassPathLocation;
import net.xeoh.plugins.base.impl.classpath.locator.ClassPathLocator;

/**
 * @author rb
 *
 */
public class ClassFinderTest {
    /**
     * @param args
     * @throws MalformedURLException
     * @throws ClassNotFoundException 
     */
    public static void main(String[] args) throws MalformedURLException,
                                          ClassNotFoundException {

        final JARCache jc = new JARCache();
        final ClassPathManager cpm = new ClassPathManager();
        final ClassPathLocator cpl = new ClassPathLocator(jc);
        // final Collection<ClassPathLoaction> findBelow = cpl.findBelow(new File("/Users/rb/Documents/Eclipse Workspace Mac OS/Augmented Text Client/dependencies/").toURI());        //
        final Collection<AbstractClassPathLocation> findBelow = cpl.findInCurrentClassPath();
        for (AbstractClassPathLocation fb : findBelow) {
            cpm.registerLocation(fb);

            System.out.println("Searching inside " + fb.getToplevelLocation());
            Collection<String> sc = cpm.findSubclassesFor(fb, Plugin.class);
            for (String string : sc) {
                System.out.println("Found class " + string);
            }
        }

    }
}
