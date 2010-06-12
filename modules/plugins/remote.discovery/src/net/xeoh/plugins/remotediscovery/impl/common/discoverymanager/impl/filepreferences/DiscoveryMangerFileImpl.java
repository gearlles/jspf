/*
* DiscoveryMangerPreferences.java
* 
* Copyright (c) 2010, Ralf Biedert All rights reserved.
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
package net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.impl.filepreferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.JSPFProperties;
import net.xeoh.plugins.remote.PublishMethod;
import net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.DiscoveryManager;
import net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.ExportInfo;
import net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.ExportedPlugin;
import net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.impl.AbstractDiscoveryManager;
import net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.impl.ExportEntry;

/**
 * @author rb
 *
 */
public class DiscoveryMangerFileImpl extends AbstractDiscoveryManager implements
        DiscoveryManager {

    /** Discovery Root */
    final File root = new File(System.getProperty("java.io.tmpdir") + "/jspf.discovery/");

    final String ourID = "" + new Random().nextInt(1000000000);

    /** A list of all exported entities we have */
    final Collection<LocalExportEntry> allExported = new ArrayList<LocalExportEntry>();

    /**
     * Creates a new preferences manager 
     */
    public DiscoveryMangerFileImpl() {
        init();
    }

    /**  */
    private void init() {
        final Logger lgger = DiscoveryMangerFileImpl.this.logger;

        this.logger.fine("Using UID " + this.ourID);
        this.root.mkdirs();

        // Runs regularly to clean up unused names 
        final Thread update = new Thread(new Runnable() {

            @Override
            public void run() {

                while (true) {
                    try {
                        final long time = System.currentTimeMillis();

                        // Store our array
                        store();
                        
                        // Clean up
                        final File r = DiscoveryMangerFileImpl.this.root;
                        final File[] list = r.listFiles();
                        for (File l : list) {
                            final String abs = l.getAbsolutePath().replaceAll("\\\\", "/");
                            final String f = abs.substring(abs.lastIndexOf("/"));
                            final String[] split = f.split("\\.");

                            long tme = Long.parseLong(split[1]);
                            if (time - tme > 3000) {
                                l.delete();
                            }
                        }

                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        lgger.fine("Sleep Interrupted.");
                    } 
                }
            }
        });
        update.setDaemon(true);
        update.start();
    }

    void store() {
        try {
            final long time = System.currentTimeMillis();
            final String prefix = DiscoveryMangerFileImpl.this.root.getAbsolutePath() + "/" + DiscoveryMangerFileImpl.this.ourID + "." + time;
            final File file = new File(prefix + ".creation");
            final FileOutputStream fos = new FileOutputStream(file);
            final ObjectOutputStream oos = new ObjectOutputStream(fos);

            synchronized (DiscoveryMangerFileImpl.this.allExported) {
                oos.writeObject(DiscoveryMangerFileImpl.this.allExported);
            }

            fos.flush();
            fos.close();

            // Finally rename the file
            file.renameTo(new File(prefix + ".ser"));
            file.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.impl.AbstractDiscoveryManager#anouncePlugin(net.xeoh.plugins.base.Plugin, net.xeoh.plugins.remote.PublishMethod, java.net.URI)
     */
    @Override
    public synchronized void anouncePlugin(Plugin plugin, PublishMethod method, URI url) {
        final LocalExportEntry localExportEntry = new LocalExportEntry();

        final Collection<Class<? extends Plugin>> allPluginClasses = getAllPluginClasses(plugin);
        final StringBuilder sb = new StringBuilder();

        for (Class<?> c : allPluginClasses) {
            sb.append(c.getCanonicalName());
            sb.append(";");
        }

        localExportEntry.uid = this.ourID;
        localExportEntry.values.put("export.uri", url.toString());
        localExportEntry.values.put("export.method", method.name());
        localExportEntry.values.put("export.time", "" + System.currentTimeMillis());
        localExportEntry.values.put("export.plugins", sb.toString());

        synchronized (this.allExported) {
            this.allExported.add(localExportEntry);
        }
        
        store();
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.impl.AbstractDiscoveryManager#revokePlugin(net.xeoh.plugins.base.Plugin, net.xeoh.plugins.remote.PublishMethod, java.net.URI)
     */
    @Override
    public synchronized void revokePlugin(Plugin plugin, PublishMethod publishMethod,
                                          URI uri) {
        final Collection<ExportEntry> toRemove = new ArrayList<ExportEntry>();

        synchronized (this.allExported) {
            for (LocalExportEntry e : this.allExported) {
                // TODO: Implement ME!
                e.hashCode();
            }

            this.allExported.removeAll(toRemove);
        }

    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.DiscoveryManager#getExportInfoFor(java.lang.String)
     */
    @SuppressWarnings( { "boxing", "unchecked" })
    @Override
    public synchronized ExportInfo getExportInfoFor(String pluginInteraceName) {
        this.logger.fine("Was queried for plugin name " + pluginInteraceName);

        final ExportInfo exportInfo = new ExportInfo();
        exportInfo.isExported = false;
        exportInfo.allExported = new ArrayList<ExportedPlugin>();

        try {
            final File[] listFiles = this.root.listFiles();
            final Map<Integer, Long> best = new HashMap<Integer, Long>();

            // Get latest entries for each file
            for (File l : listFiles) {
                final String abs = l.getAbsolutePath().replaceAll("\\\\", "/");
                final String f = abs.substring(abs.lastIndexOf("/") + 1);
                final String[] split = f.split("\\.");

                // Only final files
                if (!f.contains("ser")) continue;

                long tme = Long.parseLong(split[1]);
                int id = Integer.parseInt(split[0]);

                if (best.containsKey(id)) {
                    long last = best.get(id);
                    if (last < tme) best.put(id, tme);
                } else
                    best.put(id, tme);
            }

            // Load all files and do
            final Set<Integer> keySet = best.keySet();
            for (Integer integer : keySet) {
                int id = integer;
                long time = best.get(id);

                try {
                    final File file = new File(this.root.getAbsolutePath() + "/" + id + "." + time + ".ser");
                    final FileInputStream fis = new FileInputStream(file);
                    final ObjectInputStream ois = new ObjectInputStream(fis);
                    final Collection<LocalExportEntry> exported = (Collection<LocalExportEntry>) ois.readObject();

                    for (LocalExportEntry localExportEntry : exported) {
                        this.logger.finer("Found export node " + localExportEntry.uid);

                        final Map<String, String> exportNode = localExportEntry.values;
                        final String[] interfaces = exportNode.get("export.plugins").split(";");

                        boolean found = false;

                        // Check if one of the interface names matches
                        for (String iface : interfaces) {
                            this.logger.finest("Exported plugin interface " + iface);

                            if (!iface.equals(pluginInteraceName)) continue;

                            found = true;
                        }

                        if (!found) continue;

                        // In here we have an exported plugin 
                        final ExportedPlugin exportedPlugin = new ExportedPlugin();
                        exportedPlugin.exportMethod = exportNode.get("export.method");
                        exportedPlugin.exportURI = URI.create(exportNode.get("export.uri"));
                        exportedPlugin.timeSinceExport = Long.parseLong(exportNode.get("export.time"));
                        exportedPlugin.port = exportedPlugin.exportURI.getPort();

                        exportInfo.allExported.add(exportedPlugin);
                    }
                } catch (Exception e) {
                    // e.printStackTrace();
                }
            }

           
        } catch (Exception e) {
            e.printStackTrace();
        }

        return exportInfo;
    }

    /**
     * @param args
     * @throws InterruptedException
     * @throws URISyntaxException 
     */
    public static void main(String[] args) throws InterruptedException,
                                          URISyntaxException {
        final JSPFProperties props = new JSPFProperties();

        props.setProperty(PluginManager.class, "cache.enabled", "true");
        props.setProperty(PluginManager.class, "cache.file", "myjspf.cache");

        final PluginManager pm = PluginManagerFactory.createPluginManager(props);

        DiscoveryMangerFileImpl dmp = new DiscoveryMangerFileImpl();
        dmp.anouncePlugin(pm, PublishMethod.JAVASCRIPT, new URI("http://xxx.com"));

        Thread.sleep(2000);

        ExportInfo exportInfoFor = dmp.getExportInfoFor("net.xeoh.plugins.base.Plugin");
        Collection<ExportedPlugin> all = exportInfoFor.allExported;
        for (ExportedPlugin e : all) {
            System.out.println(e.exportURI);
        }

        Thread.sleep(4000);
    }
}
