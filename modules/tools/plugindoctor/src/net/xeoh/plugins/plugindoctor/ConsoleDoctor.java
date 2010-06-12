/*
 * ConsoleDoctor.java
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
package net.xeoh.plugins.plugindoctor;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import net.xeoh.plugins.base.impl.classpath.locator.AbstractClassPathLocation;
import net.xeoh.plugins.base.impl.classpath.locator.ClassPathLocator;
import net.xeoh.plugins.plugindoctor.analysis.AbstractClasspathElement;
import net.xeoh.plugins.plugindoctor.output.JARWriter;

/**
 * @author rb
 *
 */
public class ConsoleDoctor {
    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        final ConsoleDoctor consoleDoctor = new ConsoleDoctor();
        final Collection<URI> projectPath = new ArrayList<URI>();

        projectPath.add(new File("bin/").toURI());
        projectPath.add(new File("../Augmented Text Services/dependencies/analysis/jfreechart-1.0.13.jar").toURI());

        consoleDoctor.extractInterfaces(projectPath, "/tmp");

    }

    /**
     * Scans the path for all available plugins and extracts the plugin interfaces 
     * and their dependencies.
     * 
     * @param projectClasspaths
     * @param target
     */
    public void extractInterfaces(Collection<URI> projectClasspaths, String target) {
        final ClassPathLocator cpl = new ClassPathLocator(null);

        final Collection<AbstractClassPathLocation> allLocations = new ArrayList<AbstractClassPathLocation>();

        // Locate all classpath elements
        for (URI uri : projectClasspaths) {
            allLocations.addAll(cpl.findBelow(uri));
        }

        // Get all elements
        for (AbstractClassPathLocation l : allLocations) {
            final Collection<String> cn = l.listAllEntries();
            for (String c : cn) {

                final AbstractClasspathElement classPathElement = AbstractClasspathElement.newClassPathElement(l, c);
                System.out.println(classPathElement);
            }
        }
        
        new JARWriter(new File(target + "/test.jar"));
    }
}
