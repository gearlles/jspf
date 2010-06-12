/*
 * Main.java
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
package multipleplugins;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collection;

import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.PluginManagerUtil;
import quickstart.outputservice.OutputService;

/**
 * @author rb
 *
 */
public class Main {

    /**
     * @param args
     * @throws MalformedURLException
     */
    public static void main(final String[] args) throws MalformedURLException {

        /*
         * The is the only time you have to access a class directly. Just returns the
         * Plugin manager.
         */
        final PluginManager pmf = PluginManagerFactory.createPluginManager();

        /*
         * Add plugins from somewhere. Be sure to put *the right path* in here. This
         * method may be called multiple times. If you plan to deliver your application
         * replace bin/ with for example myplugins.jar
         */
        pmf.addPluginsFrom(new File("target/test-classes/").toURI());

        /*
         * Obtain multiple plugins
         */
        PluginManagerUtil pmu = new PluginManagerUtil(pmf);
        final Collection<OutputService> plugins = pmu.getPlugins(OutputService.class);

        /*
         * Do something
         */
        for (final OutputService s : plugins) {
            s.doSomething();
        }
    }

}
