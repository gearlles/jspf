package net.xeoh.plugins.remotediscovery.impl.v2;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.jmdns.impl.tasks.ServiceResolver;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginConfiguration;
import net.xeoh.plugins.base.PluginInformation;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.Thread;
import net.xeoh.plugins.base.annotations.configuration.IsDisabled;
import net.xeoh.plugins.base.annotations.events.Shutdown;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;
import net.xeoh.plugins.base.util.OptionUtils;
import net.xeoh.plugins.base.util.PluginConfigurationUtil;
import net.xeoh.plugins.remote.PublishMethod;
import net.xeoh.plugins.remotediscovery.DiscoveredPlugin;
import net.xeoh.plugins.remotediscovery.RemoteDiscovery;
import net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.DiscoveryManager;
import net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.ExportInfo;
import net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.ExportedPlugin;
import net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.impl.AbstractDiscoveryManager;
import net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.impl.tcpip.DiscoveryManagerTCPIPImpl;
import net.xeoh.plugins.remotediscovery.options.DiscoverOption;
import net.xeoh.plugins.remotediscovery.options.discover.OptionCallback;
import net.xeoh.plugins.remotediscovery.options.discover.OptionNearest;
import net.xeoh.plugins.remotediscovery.options.discover.OptionOldest;
import net.xeoh.plugins.remotediscovery.options.discover.OptionYoungest;

import org.freshvanilla.rmi.Proxies;
import org.freshvanilla.rmi.VanillaRmiServer;
import org.freshvanilla.utils.SimpleResource;

/**
 * @author Thomas Lottermann
 *
 */
@IsDisabled
@PluginImplementation
public class RemoteDiscoveryImpl implements RemoteDiscovery {

    /** */
    final Logger logger = Logger.getLogger(this.getClass().getName());

    private static final String TYPE = "_jspfplugindiscovery._tcp.local.";

    private static final String EXPORT_NAME = "DiscoveryManager";

    private static final String NAME = "JSPF";

    private int startupLock;

    /** How we lock for the first call? */
    private String lockMode;

    /**
     * Constructs daemonic threads
     */
    ThreadFactory threadFactory = new ThreadFactory() {

        public java.lang.Thread newThread(Runnable r) {
            java.lang.Thread rval = new java.lang.Thread(r);
            rval.setDaemon(true);
            return rval;
        }
    };

    /**
     * A request for callbacks.
     * 
     * @author rb
     *
     */
    class CallbackRequest {
        Class<? extends Plugin> req;

        OptionCallback oc;

        DiscoverOption[] moreOptions;

        Timer timer = new Timer();

        public CallbackRequest(Class<? extends Plugin> p, OptionCallback c,
                               DiscoverOption... moreOptions) {
            this.req = p;
            this.oc = c;
            this.moreOptions = moreOptions;
            if (this.oc.getTimeout() > 0) {
                this.timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        CallbackRequest.this.oc.getCallback().timeout();
                        cancelTimeout();
                        RemoteDiscoveryImpl.this.allRequests.remove(CallbackRequest.this);
                    }

                }, this.oc.getTimeout());
            }
        }

        public void cancelTimeout() {
            try {
                this.timer.cancel();
            } catch (Throwable t) {
                // 
            }
        }
    }

    /**
     * 
     * @author rb
     *
     */
    static class RemoteManagerEndpoint {
        /** */
        public InetAddress address;
        /** */
        public int port;

        /**
         * @param adr
         * @param port
         */
        public RemoteManagerEndpoint(InetAddress adr, int port) {
            this.address = adr;
            this.port = port;
        }

        public RemoteManagerEndpoint() {
            // TODO Auto-generated constructor stub
        }
    }

    /** All callbacks for discovered plugins */
    protected final List<CallbackRequest> allRequests = new ArrayList<CallbackRequest>();

    /** */
    protected final Lock serviceInfosLock = new ReentrantLock();

    /** */
    protected final Lock jmdnsLock = new ReentrantLock();

    /** */
    protected final CountDownLatch startupLatch = new CountDownLatch(1);

    /** usually should contain only all DiscoveryManager; */
    protected final Collection<ServiceInfo> serviceInfos = new ArrayList<ServiceInfo>();

    /** Contains a list of remote DiscoveryManagers all over the network and also the local manager. This is not final as we replace 
     * it every iteration. This way the client has the chance to grab a local "copy"  and work on it while a more receent version is 
     * being prepared*/
    protected volatile Collection<RemoteManagerEndpoint> remoteManagersEndpoints = new ArrayList<RemoteManagerEndpoint>();

    /** The local manager */
    protected final AbstractDiscoveryManager localTCPIPManager = new DiscoveryManagerTCPIPImpl();

    /** Server of the exported local manager */
    VanillaRmiServer<DiscoveryManager> localManagerExportServer;

    JmDNS jmdns;

    long timeOfStartup;

    /** */
    AtomicLong discoverThreadCounter = new AtomicLong();

    /** */
    @InjectPlugin
    public PluginInformation pluginInformation;

    /** */
    @InjectPlugin
    public PluginConfiguration pluginConfiguration;

    /** */
    @SuppressWarnings("boxing")
    @Thread(isDaemonic = true)
    public void backgroundInit() {
        // (Ralf:) zomfg, this is one of the worst hacks i've done the last couple of months. Deep within  jmdns we placed a variable to
        // override a check if it is already save to transmit something. jmDNS usually takes 5 seconds to reach that state, but that's
        // too long for us. If you set this variable the check will be skipped and the request should take place much faster.
        // Appears to work(tm).
        ServiceResolver.ANNOUNCE_OVERRIDE = true;

        this.logger.fine("Staring background init");

        final PluginConfigurationUtil pcu = new PluginConfigurationUtil(this.pluginConfiguration);
        this.startupLock = pcu.getInt(RemoteDiscovery.class, "startup.locktime", 1000);
        this.lockMode = pcu.getString(RemoteDiscovery.class, "startup.lockmode", "onepass");

        try {
            this.logger.finer("Locking");

            this.jmdnsLock.lock();
            this.jmdns = JmDNS.create(); // Maybe init with local loopback in case no other network card is present, otherwise returns null 

            this.timeOfStartup = System.currentTimeMillis();

            final int port = getFreePort();
            final ServiceInfo service = ServiceInfo.create(TYPE, NAME + " @" + this.timeOfStartup, port, 0, 0, EXPORT_NAME);
            this.logger.finer("Exporting at port " + port);
            this.localManagerExportServer = Proxies.newServer(EXPORT_NAME, port, (DiscoveryManager) this.localTCPIPManager);
            this.logger.finer("Announcing at port " + port);
            this.jmdns.registerService(service);

            // Set it again, this time for the lock below
            this.timeOfStartup = System.currentTimeMillis();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            this.logger.warning("Error starting discovery.");
        } finally {
            this.logger.finer("Unlocking");
            this.startupLatch.countDown();
            this.jmdnsLock.unlock();
        }
    }

    /** */
    @Shutdown
    public void shutdown() {

        final java.lang.Thread t = new java.lang.Thread(new Runnable() {

            public void run() {
                RemoteDiscoveryImpl.this.jmdnsLock.lock();

                // All of these statements tend to fail because of various reasons. During the shutdown however, 
                // we want to ignore them all ...
                try {
                    try {
                        RemoteDiscoveryImpl.this.localManagerExportServer.close();
                    } catch (Exception e) {
                        //
                    }
                    try {
                        RemoteDiscoveryImpl.this.jmdns.unregisterAllServices();
                    } catch (Exception e) {
                        //
                    }
                    try {
                        RemoteDiscoveryImpl.this.jmdns.close();
                    } catch (Exception e) {
                        //
                    }
                } finally {
                    RemoteDiscoveryImpl.this.jmdnsLock.unlock();
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    /** */
    @net.xeoh.plugins.base.annotations.Timer(period = 260)
    public void discoverThread() {

        this.logger.finer("Starting new discover pass");

        try {
            this.logger.finer("Awaiting latch");
            this.startupLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        this.logger.finer("Latch passed");

        // Increase counter
        this.discoverThreadCounter.incrementAndGet();

        // Create empty data.
        ServiceInfo[] infos = new ServiceInfo[0];

        // Magic: Get all network services of our type.
        try {
            this.logger.finer("Trying to lock jmDNS lock");
            this.jmdnsLock.lock();
            this.logger.finer("Lock obtained. Listing known entries of our type");
            // TODO: jmdsn can be null if no network card is present
            infos = this.jmdns.list(TYPE);
            this.logger.finer("List obtained.");
        } catch (IllegalStateException e) {
            this.logger.warning("Error discovering plugins ...");
        } finally {
            this.jmdnsLock.unlock();
            this.logger.finer("Lock unlocked.");
        }

        // Reset all service infos
        try {
            this.logger.finer("Trying to obtain service info lock.");
            this.serviceInfosLock.lock();
            this.logger.finer("Service info lock obtained. Transferring data.");
            this.serviceInfos.clear();
            this.serviceInfos.addAll(Arrays.asList(infos));

            this.logger.finer("Data transferred.");
        } finally {
            this.serviceInfosLock.unlock();
            this.logger.finer("Service info unlocked.");
        }

        // Process all callbacks.
        final Collection<CallbackRequest> toRemove = new ArrayList<CallbackRequest>();

        //
        // TODO: This callback handling needs improvement. Check for unsynchronized access to the allRequest structure
        //

        // Check all callbacks, if they need, well, a callback
        for (CallbackRequest cr : this.allRequests) {
            final Collection<DiscoveredPlugin> found = resolve(cr.req, cr.moreOptions);
            if (found.size() > 0) {
                cr.oc.getCallback().pluginsDiscovered(found);
                toRemove.add(cr);
            }
        }

        this.logger.finer("Callbacks executed. Removing items.");

        // Remove all resolved callbacks
        for (CallbackRequest callbackRequest : toRemove) {
            callbackRequest.cancelTimeout();
            this.allRequests.remove(callbackRequest);
        }

        this.logger.finer("Disover pass ended.");
    }

    @SuppressWarnings("unchecked")
    private DiscoveryManager getRemoteProxyToDiscoveryManager(String ip, int port) {

        final String address = ip + ":" + port;
        final int timeout = 500; // 500 ms RemoteAPIImpl if need detailed version...

        this.logger.fine("Construction proxy to remote endpoint " + address);
        final DiscoveryManager newClient = Proxies.newClient(EXPORT_NAME, address, getClass().getClassLoader(), DiscoveryManager.class);

        // Execute collection asynchronously (TODO: cache pool usage could be improved)
        final ExecutorService cachePool = Executors.newCachedThreadPool(this.threadFactory);
        final ExecutorCompletionService<String> ecs = new ExecutorCompletionService(cachePool);
        final Future<String> future = ecs.submit(new Callable<String>() {

            public String call() throws Exception {
                return AccessController.doPrivileged(new PrivilegedAction<String>() {
                    public String run() {
                        return newClient.ping(667) == 667 ? "OK" : null;
                    }
                });
            }
        });

        // Wait at most half a second (TODO: Make this configurable)
        try {
            final String string = future.get(timeout, TimeUnit.MILLISECONDS);
            if (string == null) return null;

            return newClient;
        } catch (final InterruptedException e) {
            this.logger.warning("Error while waiting for a getRemoteProxy() result");
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        } catch (final TimeoutException e) {
            this.logger.fine("Connection to the manager times out ... very strange.");
            //e.printStackTrace();
        } catch (final SecurityException e) {
            e.printStackTrace();
        } finally {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    future.cancel(true);
                    cachePool.shutdownNow();
                    return null;
                }
            });
        }
        return null;
    }

    /**
     * Refreshes all known discovery managers.
     */
    private void refreshKnownDiscoveryManagers() {

        this.serviceInfosLock.lock();
        try {
            final List<RemoteManagerEndpoint> endpoints = new ArrayList<RemoteManagerEndpoint>();

            // Check all service infos with discovery managers
            for (ServiceInfo serviceInfo : this.serviceInfos) {

                final InetAddress address = serviceInfo.getAddress();
                final int port = serviceInfo.getPort();

                endpoints.add(new RemoteManagerEndpoint(address, port));
            }

            // Eventually update the endpoints
            this.remoteManagersEndpoints = endpoints;
        } finally {
            this.serviceInfosLock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remotediscovery.RemoteDiscovery#announcePlugin(net.xeoh.plugins.base.Plugin, net.xeoh.plugins.remote.PublishMethod, java.net.URI)
     */
    public void announcePlugin(Plugin plugin, PublishMethod publishMethod, URI uri) {
        this.localTCPIPManager.anouncePlugin(plugin, publishMethod, uri);
    }

    /**
     * @return
     */
    private boolean awaitInitializationTime() {
        // Check if we're still on startup lock
        final long delay = this.timeOfStartup + this.startupLock - System.currentTimeMillis();
        if (delay > 0) {
            // If we are, halt this thread until lock time has passed. Unfortunatly the lookup is a 
            // bit "unstable" during the first few miliseconds.
            try {
                java.lang.Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remotediscovery.RemoteDiscovery#discover(java.lang.Class, net.xeoh.plugins.remotediscovery.options.DiscoverOption[])
     */
    public Collection<DiscoveredPlugin> discover(Class<? extends Plugin> plugin,
                                                 DiscoverOption... options) {

        this.logger.fine("Got request to discover " + plugin);

        // Our options ...
        final OptionUtils<DiscoverOption> ou = new OptionUtils<DiscoverOption>(options);

        
        
        // Wait until init is done ...
        try {
            this.startupLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return new ArrayList<DiscoveredPlugin>();
        }

        // Timelock: Wait for a certain time to pass since startup
        if (this.lockMode.equals("timelock")) {
            if (awaitInitializationTime() == false) { return new ArrayList<DiscoveredPlugin>(); }
        }

        // Onepass: Wait for the discover to execute once
        if (this.lockMode.equals("onepass")) {

            // Also wait some time here, otherwise we won't find all plugins in all cases
            awaitInitializationTime();

            // Wait at least one discoverThreadTurn
            final long lastDisoverValue = this.discoverThreadCounter.get();
            final long startOfWait = System.currentTimeMillis();
            while (this.discoverThreadCounter.get() == lastDisoverValue) {
                try {
                    java.lang.Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return new ArrayList<DiscoveredPlugin>();
                }

                // Safety check
                if (System.currentTimeMillis() > startOfWait + 1000) { throw new IllegalStateException("We are waiting way too long."); }
            }
        }

        // Check if we should use callbacks
        if (ou.contains(OptionCallback.class)) {
            this.allRequests.add(new CallbackRequest(plugin, ou.get(OptionCallback.class), options));
        }

        // Eventually resolve the request.
        return resolve(plugin, options);
    }

    /**
     * Checks all known service infos for matches to the given class, returns all matching entries.
     * 
     * @param plugin
     * @param options 
     * @return
     */
    private Collection<DiscoveredPlugin> resolve(final Class<? extends Plugin> plugin,
                                                 DiscoverOption[] options) {

        // Refreshes all known remote discovery managers.
        refreshKnownDiscoveryManagers();

        // The result we intend to return
        final Collection<DiscoveredPlugin> result = new ArrayList<DiscoveredPlugin>();
        final Collection<RemoteManagerEndpoint> endpoints = this.remoteManagersEndpoints;

        // Query all managers 
        for (final RemoteManagerEndpoint endpoint : endpoints) {

            // The manager to use ...
            final DiscoveryManager mgr = getRemoteProxyToDiscoveryManager(endpoint.address.getHostAddress(), endpoint.port);

            // No matter what happens, close the manager
            try {
                if (mgr == null) {
                    this.logger.info("Remote DiscoveryManager at " + endpoint.address.getHostAddress() + ":" + endpoint.port + " did not answer even though it appears to be there. .");
                    continue;
                }

                // 
                final AtomicInteger pingTime = new AtomicInteger(Integer.MAX_VALUE);

                // Get ping time
                AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    public Object run() {
                        try {
                            final long start = System.nanoTime();

                            // Perform the ping
                            mgr.ping(new Random().nextInt());

                            // Obtain the time
                            final long stop = System.nanoTime();
                            final long time = (stop - start) / 1000;

                            // And set it
                            RemoteDiscoveryImpl.this.logger.finer("Ping time to manager was " + time + "µs");
                            pingTime.set((int) time);
                        } catch (Exception e) {
                            RemoteDiscoveryImpl.this.logger.info("Error getting ping time of remote manager " + endpoint.address + ":" + endpoint.port);
                        }
                        return null;
                    }
                });

                // Query the information (needs to be inside a privileged block, otherwise applets might complain.
                final ExportInfo exportInfo = AccessController.doPrivileged(new PrivilegedAction<ExportInfo>() {
                    public ExportInfo run() {
                        return mgr.getExportInfoFor(plugin.getCanonicalName());
                    }
                });

                // If the plugin in not exported, do nothing
                if (!exportInfo.isExported) continue;

                // If it is, construct required data.
                for (ExportedPlugin p : exportInfo.allExported) {
                    final PublishMethod method = PublishMethod.valueOf(p.exportMethod);
                    final URI uri = p.exportURI;

                    String _newURI = "";

                    _newURI += uri.getScheme();
                    _newURI += "://";

                    if (endpoint.address != null) {
                        _newURI += endpoint.address.getHostAddress();
                    } else {
                        _newURI += "127.0.0.1";
                    }
                    _newURI += ":";
                    _newURI += uri.getPort();
                    _newURI += uri.getPath();

                    try {
                        // TODO: Compute distance properly.
                        result.add(new DiscoveredPluginImpl(method, new URI(_newURI), pingTime.get(), p.timeSinceExport));
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                this.logger.warning("Error talking to " + endpoint.address + ":" + endpoint.port + ". This usually means you have some version mess on the network.");
                e.printStackTrace();
            } finally {
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
                        this.logger.fine("Error closing remote DiscoveryManager ...");
                    }
                }
            }
        }

        // If we have no result there is nothing to do
        if (result.size() == 0) return result;

        // Prepapare filter options ...
        final OptionUtils<DiscoverOption> ou = new OptionUtils<DiscoverOption>(options);

        DiscoveredPlugin best = result.iterator().next();

        if (ou.contains(OptionNearest.class)) {
            // Check all plugins
            for (DiscoveredPlugin p : result) {
                // If this one is closer, replace them
                if (p.getDistance() < best.getDistance()) {
                    best = p;
                }
            }

            // Remove all other plugins
            result.clear();
            result.add(best);
        }

        if (ou.contains(OptionYoungest.class)) {
            // Check all plugins
            for (DiscoveredPlugin p : result) {
                // If this one is closer, replace them
                if (p.getTimeSinceExport() < best.getTimeSinceExport()) {
                    best = p;
                }
            }

            // Remove all other plugins
            result.clear();
            result.add(best);
        }

        if (ou.contains(OptionOldest.class)) {
            // Check all plugins
            for (DiscoveredPlugin p : result) {
                // If this one is closer, replace them
                if (p.getTimeSinceExport() > best.getTimeSinceExport()) {
                    best = p;
                }
            }

            // Remove all other plugins
            result.clear();
            result.add(best);
        }

        // Debug plugins
        for (DiscoveredPlugin p : result) {
            this.logger.fine("Returning plugin " + p);
        }

        return result;
    }

    /** 
     * out of net.xeoh.plugins.remote.impl.ermi.RemoteAPIImpl
     * @return
     */
    private static int getFreePort() {
        try {
            final ServerSocket serverSocket = new ServerSocket(0);
            final int port = serverSocket.getLocalPort();
            serverSocket.close();
            return port;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 1025 + new Random().nextInt(50000);
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remotediscovery.RemoteDiscovery#revokePlugin(net.xeoh.plugins.base.Plugin, net.xeoh.plugins.remote.PublishMethod, java.net.URI)
     */
    public void revokePlugin(Plugin plugin, PublishMethod publishMethod, URI uri) {
        this.localTCPIPManager.revokePlugin(plugin, publishMethod, uri);
    }
}
