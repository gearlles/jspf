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
package net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.impl.localpreferences;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

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
public class DiscoveryMangerPreferencesImpl extends AbstractDiscoveryManager implements
        DiscoveryManager {

    /** Root for all agents */
    private Preferences agentsRoot;

    /** All of our exports node */
    private Preferences ourExportsNode;

    /** Only valid results if this is true ...*/
    boolean initSuccessful = false;

    /** Our ID */
    final String ourID;

    /** A list of all exported entities we have */
    final Collection<LocalExportEntry> allExported = new ArrayList<LocalExportEntry>();

    /**
     * Creates a new preferences manager 
     */
    public DiscoveryMangerPreferencesImpl() {
        this.ourID = UUID.randomUUID().toString();

        try {
            this.agentsRoot = Preferences.systemNodeForPackage(getClass()).node("agents");
            this.agentsRoot.sync();
            
            init();
        } catch (Exception e) {
            e.printStackTrace();
            this.logger.fine("Error initializing loopback discovery");
        }

        this.initSuccessful = true;
    }

    /**  */
    private void init() {
        final Logger lgger = DiscoveryMangerPreferencesImpl.this.logger;

        final Preferences root = DiscoveryMangerPreferencesImpl.this.agentsRoot;
        final Preferences ournode = this.agentsRoot.node(this.ourID);

        this.ourExportsNode = ournode.node("exports");

        this.logger.fine("Using UID " + this.ourID);

        // Runs regularly to clean up unused names 
        final Thread cleanup = new Thread(new Runnable() {

            @Override
            public void run() {

                while (true) {
                    // First, update the timestamp of OUR NODE ...
                    ournode.putLong("last.pass", System.currentTimeMillis());

                    // ... and eventually clear ALL NODES which have been up for too long
                    try {
                        final String[] childrenNames = root.childrenNames();

                        for (String string : childrenNames) {
                            final Preferences node = root.node(string);

                            long time = node.getLong("last.pass", 0);
                            if (time < System.currentTimeMillis() - 2000) {
                                lgger.fine("Removing old node " + node.name());
                                node.removeNode();
                            }
                        }
                    } catch (BackingStoreException e1) {
                        lgger.fine("Error getting children ...");
                        e1.printStackTrace();
                    }

                    // And sleep some moment before we try again
                    try {
                        root.sync();
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        lgger.fine("Sleep Interrupted.");
                    } catch (BackingStoreException e) {
                        lgger.fine("Errow syncing node.");
                        e.printStackTrace();
                    }
                }
            }
        });
        cleanup.setDaemon(true);
        cleanup.start();
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.impl.AbstractDiscoveryManager#anouncePlugin(net.xeoh.plugins.base.Plugin, net.xeoh.plugins.remote.PublishMethod, java.net.URI)
     */
    @Override
    public synchronized void anouncePlugin(Plugin plugin, PublishMethod method, URI url) {
        final LocalExportEntry localExportEntry = new LocalExportEntry();
        localExportEntry.plugin = plugin;
        localExportEntry.method = method;
        localExportEntry.uri = url;
        localExportEntry.timeOfExport = System.currentTimeMillis();

        localExportEntry.nodeID = UUID.randomUUID().toString();

        final Preferences ournode = this.ourExportsNode.node(localExportEntry.nodeID);
        ournode.put("export.uri", url.toString());
        ournode.put("export.method", method.name());
        ournode.putLong("export.time", localExportEntry.timeOfExport);

        final Collection<Class<? extends Plugin>> allPluginClasses = getAllPluginClasses(plugin);

        final StringBuilder sb = new StringBuilder();

        for (Class<?> c : allPluginClasses) {
            sb.append(c.getCanonicalName());
            sb.append(";");
        }

        ournode.put("export.plugins", sb.toString());

        // Store the new plugin
        try {
            this.agentsRoot.sync();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }

        this.allExported.add(localExportEntry);
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
                if (e.plugin == plugin && e.method == publishMethod && e.uri.equals(uri)) {
                    toRemove.add(e);
                    final Preferences ournode = this.ourExportsNode.node(e.nodeID);

                    try {
                        ournode.removeNode();
                        this.agentsRoot.sync();
                    } catch (BackingStoreException e1) {
                        e1.printStackTrace();
                    }
                }
            }

        }

        this.allExported.removeAll(toRemove);
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.DiscoveryManager#getExportInfoFor(java.lang.String)
     */
    @Override
    public synchronized ExportInfo getExportInfoFor(String pluginInteraceName) {
        this.logger.fine("Was queried for plugin name " + pluginInteraceName);

        
        final ExportInfo exportInfo = new ExportInfo();
        exportInfo.isExported = false;
        exportInfo.allExported = new ArrayList<ExportedPlugin>();

        // We have to iterate over all agents ... and all their nodes

        try {
            // Sync!
            this.agentsRoot.sync();
            
            // Get all child nodes ...
            final String[] allAgents = this.agentsRoot.childrenNames();

            for (String entry : allAgents) {
                this.logger.fine("Found agent " + entry);
                
                final Preferences currentNode = this.agentsRoot.node(entry + "/exports");
                final String[] allExports = currentNode.childrenNames();

                for (String string : allExports) {
                    this.logger.finer("Found export node " + string);
                    
                    final Preferences exportNode = currentNode.node(string);
                    final String[] interfaces = exportNode.get("export.plugins", "").split(";");

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
                    exportedPlugin.exportMethod = exportNode.get("export.method", "");
                    exportedPlugin.exportURI = URI.create(exportNode.get("export.uri", ""));
                    exportedPlugin.timeSinceExport = Long.parseLong(exportNode.get("export.time", ""));
                    exportedPlugin.port = exportedPlugin.exportURI.getPort();

                    exportInfo.allExported.add(exportedPlugin);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return exportInfo;
    }

    /**
     * Debugs the structure 
     */
    public void debug() {
        try {
            this.agentsRoot.sync();
        } catch (BackingStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        debug(this.agentsRoot);
    }

    /**
     * @param start
     */
    private void debug(Preferences start) {
        System.out.println(start.absolutePath());
        try {
            String[] keys = start.keys();
            for (String string : keys) {
                System.out.println("  " + string + " -> " + start.get(string, ""));
            }

            String[] childrenNames = start.childrenNames();
            for (String string : childrenNames) {
                debug(start.node(string));
            }
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
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

        DiscoveryMangerPreferencesImpl dmp = new DiscoveryMangerPreferencesImpl();
        dmp.anouncePlugin(pm, PublishMethod.JAVASCRIPT, new URI("http://xxx.com"));
        dmp.debug();

        ExportInfo exportInfoFor = dmp.getExportInfoFor("net.xeoh.plugins.base.Plugin");
        Collection<ExportedPlugin> all = exportInfoFor.allExported;
        for (ExportedPlugin e : all) {
            System.out.println(e.exportURI);
        }

        Thread.sleep(1000);
    }
}
