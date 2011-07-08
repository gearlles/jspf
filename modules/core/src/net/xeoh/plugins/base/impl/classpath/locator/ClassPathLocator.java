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

import static net.jcores.jre.CoreKeeper.$;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.jcores.shared.interfaces.functions.F1;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.diagnosis.channels.tracing.SpawnerTracer;
import net.xeoh.plugins.base.impl.PluginManagerImpl;
import net.xeoh.plugins.base.impl.classpath.cache.JARCache;
import net.xeoh.plugins.base.util.PluginConfigurationUtil;
import net.xeoh.plugins.diagnosis.local.Diagnosis;
import net.xeoh.plugins.diagnosis.local.util.DiagnosisChannelUtil;

/**
 * Used to find classpaths, JARs and their contents.
 * 
 * @author Ralf Biedert
 */
public class ClassPathLocator {

    /** Cache to lookup elements */
    private final JARCache cache;

    /** Mainly used to access the config. */
    private PluginManagerImpl pluginManager;


    /**
     * @param pluginManager
     * @param cache
     */
    public ClassPathLocator(PluginManagerImpl pluginManager, JARCache cache) {
        this.pluginManager = pluginManager;
        this.cache = cache;
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

        // First, check if the entry represents a multi-plugin (in that case we don't add
        // anything else)
        if ($(startPoint).filter(".*\\.plugin?$").get(0) != null) {
            rval.add(AbstractClassPathLocation.newClasspathLocation(this.cache, toplevel.toString(), toplevel));
            return rval;
        }

        // Check if this is a directory or a file
        if (startPoint.isDirectory()) {
            final File[] listFiles = startPoint.listFiles();

            boolean hasJARs = false;

            for (File file : listFiles) {
                if (file.getAbsolutePath().endsWith(".jar")) {
                    rval.add(AbstractClassPathLocation.newClasspathLocation(this.cache, file.toURI().toString(), file.toURI()));
                    hasJARs = true;
                }

                if ($(file).filter(".*\\.plugin?$").get(0) != null) {
                    rval.add(AbstractClassPathLocation.newClasspathLocation(this.cache, file.toURI().toString(), file.toURI()));
                    hasJARs = true;
                }
            }

            // If we have JARs, we already added them
            if (hasJARs) return rval;

            // If we have no JARs, this is probably a classpath, in this case warn that
            // the method is not recommended
            if (toplevel.toString().contains("/bin/") || toplevel.toString().contains("class")) {
                System.err.println("Adding plugins in 'raw' classpaths, such as 'bin/' or 'classes/' is not recommended. Please use classpath://* instead (the video is a bit outdated in this respect).");
            }
            rval.add(AbstractClassPathLocation.newClasspathLocation(this.cache, toplevel.toString(), toplevel));
            return rval;
        }

        // If this is directly a JAR, add this
        if (startPoint.isFile() && startPoint.getAbsolutePath().endsWith(".jar")) {
            rval.add(AbstractClassPathLocation.newClasspathLocation(this.cache, toplevel.toString(), toplevel));
            return rval;
        }

        return rval;
    }

    /**
     * Finds all locations inside the current classpath.
     * 
     * @return .
     */
    @SuppressWarnings("boxing")
    public Collection<AbstractClassPathLocation> findInCurrentClassPath() {
        final DiagnosisChannelUtil<String> channel = new DiagnosisChannelUtil<String>(this.pluginManager.getPlugin(Diagnosis.class).channel(SpawnerTracer.class));
        
        channel.status("findinclasspath/start");
        final Collection<AbstractClassPathLocation> rval = new ArrayList<AbstractClassPathLocation>();

        // Get our current classpath (TODO: Better get this using
        // ClassLoader.getSystemClassLoader()?)
        final boolean filter = new PluginConfigurationUtil(this.pluginManager.getPluginConfiguration()).getBoolean(PluginManager.class, "classpath.filter.default.enabled", true);
        final String blacklist[] = new PluginConfigurationUtil(this.pluginManager.getPluginConfiguration()).getString(PluginManager.class, "classpath.filter.default.pattern", "/jre/lib/;/jdk/lib/;/lib/rt.jar").split(";");
        final String pathSep = System.getProperty("path.separator");
        final String classpath = System.getProperty("java.class.path");
        final List<URL> toFilter = new ArrayList<URL>();

        String[] classpaths = classpath.split(pathSep);

        channel.status("findinclasspath/status", "pathseparator", pathSep, "blacklist", $(blacklist).join(";"));
        
        // Optional, our URL classloader ... In case JSPF has been loaded from an 
        // URL class loader as well (Issue #29)
        URLClassLoader ourloader = $(getClass().getClassLoader()).cast(URLClassLoader.class).get(0);
        while (ourloader != null) { // Removed check for system classloader, might need its elements as well
            channel.status("findinclasspath/urlloader");
            classpaths = $(ourloader.getURLs()).file().forEach(new F1<File, String>() {
                @Override
                public String f(File arg0) {
                    channel.status("findinclasspath/urlloader/path", "path", arg0);
                    return arg0.getAbsolutePath();
                }
            }).add(classpaths).unique().array(String.class);
            ourloader = $(ourloader.getParent()).cast(URLClassLoader.class).get(0);
        }
        

        // Check if we should filter, if yes, get topmost classloader so we know
        // what to filter out
        if (filter) {
            channel.status("findinclasspath/filter");

            ClassLoader loader = ClassLoader.getSystemClassLoader();
            while (loader != null && loader.getParent() != null)
                loader = loader.getParent();

            // Get 'blacklist' and add it to our filterlist
            if (loader != null && loader instanceof URLClassLoader) {
                URL[] urls = ((URLClassLoader) loader).getURLs();
                for (URL url : urls) {
                    channel.status("findinclasspath/filter/add", "item", url);
                    toFilter.add(url);
                }
            }
        }

        // Process all possible locations
        for (String string : classpaths) {
            if(string == null) continue;
            
            try {
                final URL url = new File(string).toURI().toURL();
                
                channel.status("findinclasspath/add", "raw", string, "url", url);

                // Check if the url was already contained
                if (toFilter.contains(url) || blacklisted(blacklist, url)) {
                    channel.status("findinclasspath/add/blacklisted", "raw", string, "url", url);
                    continue;
                }

                // And eventually add the location
                rval.add(AbstractClassPathLocation.newClasspathLocation(this.cache, "#classpath", new File(string).toURI()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        return rval;
    }

    /**
     * Checks if the given URL is blacklisted
     * 
     * @param blacklist
     * @param url
     * @return
     */
    private boolean blacklisted(String[] blacklist, URL url) {
        // Default sanity check
        if (blacklist == null || blacklist.length == 0 || blacklist[0].length() == 0)
            return false;

        // Go thorugh blacklist
        for (String string : blacklist) {
            if (url.toString().contains(string)) return true;
        }

        return false;
    }
}
