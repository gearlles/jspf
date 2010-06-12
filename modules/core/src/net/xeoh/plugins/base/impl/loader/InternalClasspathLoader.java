/*
 * InternalClasspathLoader.java
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
package net.xeoh.plugins.base.impl.loader;

import java.net.URI;
import java.util.Collection;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.impl.PluginManagerImpl;
import net.xeoh.plugins.base.impl.classpath.ClassPathManager;
import net.xeoh.plugins.base.impl.classpath.locator.AbstractClassPathLocation;
import net.xeoh.plugins.base.impl.classpath.locator.ClassPathLocator;

/**
 * @author rb
 *
 */
public class InternalClasspathLoader extends AbstractLoader {

    /**
     * @param pluginManager
     */
    public InternalClasspathLoader(PluginManagerImpl pluginManager) {
        super(pluginManager);
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.base.impl.loader.AbstractLoader#handlesURI(java.net.URI)
     */
    @Override
    public boolean handlesURI(URI uri) {
        if (uri.getScheme().equals("classpath")) return true;

        return false;
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.base.impl.loader.AbstractLoader#loadFrom(java.net.URI)
     */
    @Override
    public void loadFrom(URI url) {

        // Special handler to load files from the local classpath
        if (url.toString().equals("classpath://*")) {
            loadAllClasspathPluginClasses();
            return;
        }

        // Special handler to load files from the local classpath, specified by name.
        // Please note that this is a very bad solution and should only be used in special cases,
        // as when invoking from applets that don't have permission to access the classpath (is this so)
        if (url.toString().startsWith("classpath://")) {
            // Obtain the fq-classname to load
            final String toLoad = url.toString().substring("classpath://".length());
            loadClassFromClasspathByName(toLoad);
            return;
        }
    }

    /** */
    private void loadAllClasspathPluginClasses() {
        // Start the classpath search
        this.logger.finer("Starting classpath search ...");

        final ClassPathManager manager = this.pluginManager.getClassPathManager();
        final ClassPathLocator locator = this.pluginManager.getClassPathLocator();

        final Collection<AbstractClassPathLocation> locations = locator.findInCurrentClassPath();
        for (AbstractClassPathLocation location : locations) {
            manager.registerLocation(location);

            final Collection<String> candidates = manager.findSubclassesFor(location, Plugin.class);

            this.logger.finer("Found " + candidates.size() + " candidates.");

            for (String string : candidates) {
                tryToLoadClassAsPlugin(location, string);
            }
        }

        return;
    }

    private void loadClassFromClasspathByName(final String toLoad) {
        this.logger.fine("Loading " + toLoad + " directly");
        tryToLoadClassAsPlugin(null, toLoad);
    }

}
