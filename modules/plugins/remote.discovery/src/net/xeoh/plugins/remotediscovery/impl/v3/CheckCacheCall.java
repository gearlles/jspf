package net.xeoh.plugins.remotediscovery.impl.v3;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.remote.PublishMethod;
import net.xeoh.plugins.remotediscovery.DiscoveredPlugin;
import net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.DiscoveryManager;
import net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.ExportInfo;

import org.freshvanilla.utils.SimpleResource;

/**
 * 
 * NOTE: This cache saves only one host for a plugin and not the whole bunch....
 * 
 * @author massini
 *
 */
final class CheckCacheCall extends BaseCall {
    /**
     * 
     */
    private final RemoteDiscoveryImpl remoteDiscoveryImpl;

    /**
     * @param remoteDiscoveryImpl
     */
    CheckCacheCall(RemoteDiscoveryImpl remoteDiscoveryImpl) {
        this.remoteDiscoveryImpl = remoteDiscoveryImpl;
    }

    /**
     * Contains plugins which are exported by a specified DiscoveryManager
     *  
     * @author massini
     *
     */
    class Entry implements Serializable {
        private static final long serialVersionUID = -7595621607661329454L;
        RemoteManagerEndpoint manager;
        String plugin;
        DiscoveredPlugin src;

        public Entry(final String plugin, final RemoteManagerEndpoint manager,
                     final DiscoveredPlugin src) {
            this.plugin = plugin;
            this.manager = manager;
            this.src = src;
        }

        /*
        @Override
        public String toString(){
        	StringBuffer buf=new StringBuffer();
        	buf.append("\t"+this.plugin+"\n");
        	buf.append("\tmgr: "+this.manager+"\n");
        	buf.append("\tsrc: "+this.src.getPublishURI()+" "+this.src.getPublishMethod()+"\n");
        	buf.append("\t"+this.src.getCapabilities());
        	return buf.toString();
        }
        //*/

        @SuppressWarnings("boxing")
        private synchronized void readObject(ObjectInputStream in)
                                                                  throws IOException,
                                                                  ClassNotFoundException {
            this.plugin = (String) in.readObject();
            this.manager = (RemoteManagerEndpoint) in.readObject();

            // read DiscoveredPlugin
            URI uri = (URI) in.readObject();
            int dist = (Integer) in.readObject();
            PublishMethod method = (PublishMethod) in.readObject();
            int numCaps = (Integer) in.readObject();
            List<String> caps = new ArrayList<String>();
            for (int i = 0; i < numCaps; ++i)
                caps.add((String) in.readObject());

            // TODO: put some method to aquire update of timeSinceStartUp
            this.src = new DiscoveredPluginImpl(caps, method, uri, dist, 0);
        }

        @SuppressWarnings( { "boxing", "cast" })
        private synchronized void writeObject(ObjectOutputStream out)
                                                                     throws IOException {
            // TODO: !!! WICHTIG!! was ist mit serialVersionUID muss sie auch gespeichert werden?
            out.writeObject(this.plugin);
            out.writeObject(this.manager);
            // write discoveredPlugin
            out.writeObject(this.src.getPublishURI());
            out.writeObject((Integer) this.src.getDistance());
            out.writeObject(this.src.getPublishMethod());
            out.writeObject(this.src.getCapabilities().size());
            for (String s : this.src.getCapabilities())
                out.writeObject(s);
        }

        // TODO: WICHTIG:::: Save/Load DiscoveredPlugin with the right methods
    }

    private static final String CACHE_FILE = "d:\\cachefile.dat";

    private HashMap<Class<? extends Plugin>, HashMap<RemoteManagerEndpoint, Entry>> cache = new HashMap<Class<? extends Plugin>, HashMap<RemoteManagerEndpoint, Entry>>();

    public void addToCache(Class<? extends Plugin> plugin,
                           final RemoteManagerEndpoint manager,
                           final DiscoveredPlugin src) {
        synchronized (this.cache) {
            // 1) get all managers providing this plugin
            HashMap<RemoteManagerEndpoint, Entry> managers = this.cache.get(plugin);
            if (null == managers) { // if we dont have one, so create
                managers = new HashMap<RemoteManagerEndpoint, Entry>();
                managers.put(manager, new Entry(plugin.getCanonicalName(), manager, src));

                this.cache.put(plugin, managers);

                return;
            }

            // 2) get the entry providing this plugin.
            Entry entry = managers.get(manager);
            if (null == entry) { // no entry
                managers.put(manager, new Entry(plugin.getCanonicalName(), manager, src));
                return;
            }

            // 3) here it means we have already an entry... so skip!!!
        }
    }

    public Collection<DiscoveredPlugin> call() {
        this.remoteDiscoveryImpl.syso("cache 1");
        final Collection<Entry> entries = getEntryList(getPlugin());
        this.remoteDiscoveryImpl.syso("cache entries " + entries.size());
        this.remoteDiscoveryImpl.syso("cache 2");
        Collection<DiscoveredPlugin> available = remoteUnavailable(entries);
        this.remoteDiscoveryImpl.syso("cache 3");
        this.remoteDiscoveryImpl.syso("cache available " + available.size());
        this.remoteDiscoveryImpl.syso("cache 4");
        if (!available.isEmpty()) {
            this.remoteDiscoveryImpl.syso("cache 5");
            this.remoteDiscoveryImpl.doFilter(available, getDiscoverOptions());
        }
        this.remoteDiscoveryImpl.syso("cache 6");
        this.remoteDiscoveryImpl.syso("cache: available " + available.size());

        this.remoteDiscoveryImpl.syso("cache 7");
        if (!available.isEmpty()) this.remoteDiscoveryImpl.syso("---> discovery cache hit.");

        this.remoteDiscoveryImpl.syso("cache 8");
        return available;
    }

    @SuppressWarnings( { "boxing", "unchecked" })
    public void loadCache() {
        this.cache.clear();
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(CACHE_FILE));
            this.remoteDiscoveryImpl.syso("load cache");
            try {
                final int cacheSize = (Integer) in.readObject();
                //					syso("size "+cacheSize);
                for (int i = 0; i < cacheSize; ++i) {
                    HashMap<RemoteManagerEndpoint, Entry> rmee = new HashMap<RemoteManagerEndpoint, Entry>();
                    Class<? extends Plugin> plugin = (Class<? extends Plugin>) in.readObject();
                    this.remoteDiscoveryImpl.syso(plugin.getCanonicalName());
                    //						syso("\t"+i+"'"+pluginName+"'");
                    int numEntries = (Integer) in.readObject();
                    //						syso("\t"+numEntries);
                    for (int j = 0; j < numEntries; ++j) {
                        Entry entry = (Entry) in.readObject();
                        //							syso("\t\t"+entry);
                        rmee.put(entry.manager, entry);
                    }
                    this.cache.put(plugin, rmee);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                in.close();
            }
        } catch (FileNotFoundException e) {
            this.remoteDiscoveryImpl.logger.warning("Loading cache file" + CACHE_FILE + " failed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //		public void printCache() {
    //			for(Map.Entry<String, Entry> e: this.cache.entrySet()){
    //				System.out.println(e.getKey()+" "+e.getValue());
    //			}
    //		}

    @SuppressWarnings("boxing")
    public void saveCache() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(CACHE_FILE));
            this.remoteDiscoveryImpl.syso("save cache");
            try {
                //					syso("size"+this.cache.size());
                out.writeObject(this.cache.size());
                for (Map.Entry<Class<? extends Plugin>, HashMap<RemoteManagerEndpoint, Entry>> he : this.cache.entrySet()) {
                    //						syso(he.getKey()+" " +he.getValue().size());
                    this.remoteDiscoveryImpl.syso(he.getKey().getCanonicalName());
                    out.writeObject(he.getKey());
                    out.writeObject(he.getValue().size());
                    for (Entry me : he.getValue().values()) {
                        //							syso("\t"+me);
                        out.writeObject(me);
                    }
                }
                //					out.writeObject(this.cache);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Collection<Entry> getEntryList(Class<? extends Plugin> plugin) {
        synchronized (this.cache) {
            HashMap<RemoteManagerEndpoint, Entry> managers = this.cache.get(plugin);

            if (managers == null) return new ArrayList<Entry>();

            return managers.values();
        }
    }

    private Collection<DiscoveredPlugin> remoteUnavailable(final Collection<Entry> lst) {

        Collection<DiscoveredPlugin> res = new ArrayList<DiscoveredPlugin>();
        this.remoteDiscoveryImpl.syso("cache rem 1");

        for (Entry entry : lst) {
            this.remoteDiscoveryImpl.syso("cache rem 2");
            // try to establish connection to the discovery manager

            this.remoteDiscoveryImpl.syso("cache rem 3");
            final DiscoveryManager mgr = this.remoteDiscoveryImpl.getRemoteProxy(entry.manager.address.getHostAddress(), entry.manager.port);

            this.remoteDiscoveryImpl.syso("cache rem 4");
            if (null == mgr) {
                this.remoteDiscoveryImpl.syso("cache rem 5");
                // so manager not available. -> remote from cache
                // TODO!!!
                continue;
            }
            this.remoteDiscoveryImpl.syso("cache rem 6");

            // so our manager is available......
            try {
                this.remoteDiscoveryImpl.syso("cache rem 7");
                // TODO: here are probably not the complete information as it was before....
                // RemoteDiscoveryImpl.this.logger.info("Error getting ping time of remote manager " + 
                //          endpoint.address + ":" + endpoint.port);
                @SuppressWarnings("unused")
                final AtomicInteger pingTime = this.remoteDiscoveryImpl.getPingTime(mgr, entry.manager.address.getHostAddress(), entry.manager.port);

                this.remoteDiscoveryImpl.syso("cache rem 8");
                // Query the information (needs to be inside a privileged block, otherwise applets might complain.
                final ExportInfo exportInfo = AccessController.doPrivileged(new PrivilegedAction<ExportInfo>() {
                    public ExportInfo run() {
                        return mgr.getExportInfoFor(getPlugin().getCanonicalName());
                    }
                });
                this.remoteDiscoveryImpl.syso("cache rem 9");

                // If the plugin in not exported, do nothing  
                if (!exportInfo.isExported) {
                    this.remoteDiscoveryImpl.syso("cache rem 10");
                    // this plugin is not exported anymore...
                    // TODO: remote from cache list
                    continue;
                }
                this.remoteDiscoveryImpl.syso("cache rem 11");
                // ... otherwise the plugin is available! put in list
                res.add(entry.src);
                this.remoteDiscoveryImpl.syso("cache rem 12");
            } finally {
                this.remoteDiscoveryImpl.syso("cache rem 13");
                // In case the manager is of the type simple resource (which it should be), try to close it.
                if (mgr instanceof SimpleResource) {
                    try {

                        // Try to close our device again 
                        AccessController.doPrivileged(new PrivilegedAction<Object>() {
                            public Object run() {
                                ((SimpleResource) mgr).close();
                                return null;
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.remoteDiscoveryImpl.logger.fine("Error closing remote DiscoveryManager ...");
                    }
                }
                this.remoteDiscoveryImpl.syso("cache rem 14");
            }
        } // for each Entry

        this.remoteDiscoveryImpl.syso("cache rem 15");
        return res;
    }
}