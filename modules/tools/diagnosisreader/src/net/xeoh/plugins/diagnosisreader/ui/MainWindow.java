/*
 * MainWindow.java
 *
 * Copyright (c) 2011, Ralf Biedert All rights reserved.
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
package net.xeoh.plugins.diagnosisreader.ui;

import static net.jcores.CoreKeeper.$;

import java.io.File;
import java.util.Collection;

import net.jcores.cores.CoreFile;
import net.jcores.cores.CoreObject;
import net.jcores.interfaces.functions.F1;
import net.jcores.options.OptionDropTypeFiles;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.util.PluginManagerUtil;
import net.xeoh.plugins.diagnosisreader.converters.Converter;

/**
 * @author Ralf Biedert
 */
public class MainWindow extends MainWindowTemplate {

    /** */
    private static final long serialVersionUID = 3556149463262771404L;

    /** The plugin manager we use */
    private PluginManager pluginManager;

    /**
     * @param pluginManager
     */
    public MainWindow(PluginManager pluginManager) {
        this.pluginManager = pluginManager;

        // Make the drop panel accept files
        $(this.dropPanel).onDrop(new F1<CoreObject<Object>, Void>() {
            @Override
            public Void f(CoreObject<Object> arg0) {
                process(arg0.as(CoreFile.class));
                return null;
            }
        }, new OptionDropTypeFiles());
    }

    /**
     * Registers a converter.
     *
     * @param c The converter to register.
     */
    public void registerHandler(Converter c) {
        this.converter.addItem(c.getInfo().getName());
    }

    /**
     * Processes the given files
     *
     * @param files
     */
    void process(CoreFile files) {
        final String selected = (String) this.converter.getSelectedItem();
        final PluginManagerUtil managerUtil = new PluginManagerUtil(this.pluginManager);
        final Collection<Converter> plugins = managerUtil.getPlugins(Converter.class);

        // Now convert all files
        for (final Converter c : plugins) {
            if(c.getInfo().getName().equals(selected)) {
                files.map(new F1<File, Void>() {

                    @Override
                    public Void f(File arg0) {
                        c.convert(arg0);
                        return null;
                    }
                });
            }
        }
    }
}
