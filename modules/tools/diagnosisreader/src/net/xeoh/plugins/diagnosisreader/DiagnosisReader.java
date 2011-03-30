/*
 * DiagnosisReader.java
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
package net.xeoh.plugins.diagnosisreader;

import java.io.File;
import java.util.Collection;

import javax.swing.UIManager;

import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.PluginManagerUtil;
import net.xeoh.plugins.base.util.uri.ClassURI;
import net.xeoh.plugins.diagnosisreader.converters.Converter;
import net.xeoh.plugins.diagnosisreader.converters.impl.plain.PlainConverterImpl;
import net.xeoh.plugins.diagnosisreader.ui.MainWindow;

/**
 * @author Ralf Biedert
 */
public class DiagnosisReader {
    public static void main(String[] args) {
        // Set the system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (final Exception e) {}
        
        // We only need the default plugins and a few of our owns
        final PluginManager pluginManager = PluginManagerFactory.createPluginManager();
        final PluginManagerUtil pluginManagerUtil = new PluginManagerUtil(pluginManager);
        
        pluginManager.addPluginsFrom(ClassURI.PLUGIN(PlainConverterImpl.class));
        pluginManager.addPluginsFrom(new File("plugins/").toURI());
        
        final MainWindow mainWindow = new MainWindow(pluginManager);
        mainWindow.setVisible(true);
        
        final Collection<Converter> converters = pluginManagerUtil.getPlugins(Converter.class);
        for (Converter converter : converters) {
            mainWindow.registerHandler(converter);
        }
    }
}
