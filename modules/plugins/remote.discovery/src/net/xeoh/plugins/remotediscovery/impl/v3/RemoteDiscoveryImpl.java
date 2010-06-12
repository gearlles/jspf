package net.xeoh.plugins.remotediscovery.impl.v3;

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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
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

    private static final String EXPORT_NAME = "DiscoveryManager";

    private static final String NAME = "JSPF";

    private static final String TYPE = "_jspfplugindiscovery._tcp.local.";

    /**
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

    /** */
    @InjectPlugin
    public PluginConfiguration pluginConfiguration;

    /** */
    @InjectPlugin
    public PluginInformation pluginInformation;

    /** How we lock for the first call? */
    private String lockMode;

    private int startupLock;

    /** All callbacks for discovered plugins */
    protected final List<CallbackRequest> allRequests = new ArrayList<CallbackRequest>();

    protected final Lock jmdnsLock = new ReentrantLock();

    /** The local manager */
    protected final DiscoveryManagerTCPIPImpl localManager = new DiscoveryManagerTCPIPImpl();

    /**
     * Contains a list of remote DiscoveryManagers all over the network and also
     * the local manager
     */
    protected final HashSet<RemoteManagerEndpoint> remoteManagersEndpoints = new HashSet<RemoteManagerEndpoint>();

    /** usually should contain only all DiscoveryManager; */
    protected final Collection<ServiceInfo> serviceInfos = new ArrayList<ServiceInfo>();

    /** */
    protected final Lock serviceInfosLock = new ReentrantLock();

    protected final CountDownLatch startupLatch = new CountDownLatch(1);

    final CheckCacheCall checkCache = new CheckCacheCall(this);

    final DiscoverCall discover = new DiscoverCall(this);

    /** */
    AtomicLong discoverThreadCounter = new AtomicLong();

    JmDNS jmdns;

    /** Server of the exported local manager */
    VanillaRmiServer<DiscoveryManager> localManagerExportServer;

    /** */
    final Logger logger = Logger.getLogger(this.getClass().getName());

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

    long timeOfStartup;

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.xeoh.plugins.remotediscovery.RemoteDiscovery#announcePlugin(net.xeoh
     * .plugins.base.Plugin, net.xeoh.plugins.remote.PublishMethod,
     * java.net.URI)
     */
    public void announcePlugin(Plugin plugin, PublishMethod publishMethod, URI uri) {
        this.localManager.anouncePlugin(plugin, publishMethod, uri);
    }

    /** */
    @SuppressWarnings("boxing")
    @Thread(isDaemonic = true)
    public void backgroundInit() {
        // (Ralf:) zomfg, this is one of the worst hacks i've done the last
        // couple of months. Deep within jmdns we placed a variable to
        // override a check if it is already save to transmit something. jmDNS
        // usually takes 5 seconds to reach that state, but that's
        // too long for us. If you set this variable the check will be skipped
        // and the request should take place much faster.
        // Appears to work(tm).
        ServiceResolver.ANNOUNCE_OVERRIDE = true;

        final PluginConfigurationUtil pcu = new PluginConfigurationUtil(this.pluginConfiguration);
        this.startupLock = pcu.getInt(RemoteDiscovery.class, "startup.locktime", 1000);
        this.lockMode = pcu.getString(RemoteDiscovery.class, "startup.lockmode", "onepass");

        // TODO put this in a thread
        this.checkCache.loadCache();

        try {
            this.jmdnsLock.lock();
            this.jmdns = JmDNS.create(); // Maybe init with local loopback in case no other network card is present, otherwise returns null 

            this.timeOfStartup = System.currentTimeMillis();

            final int port = getFreePort();
            final ServiceInfo service = ServiceInfo.create(TYPE, NAME + " @" + this.timeOfStartup, port, 0, 0, EXPORT_NAME);

            this.localManagerExportServer = Proxies.newServer(EXPORT_NAME, port, (DiscoveryManager) this.localManager);
            this.jmdns.registerService(service);

            // Set it again, this time for the lock below
            this.timeOfStartup = System.currentTimeMillis();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            this.logger.warning("Error starting discovery.");
        } finally {
            this.startupLatch.countDown();
            this.jmdnsLock.unlock();
        }

        // and load our cache...
        // todo make this configurable...
        //		this.checkCache.loadCache(fileName)
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remotediscovery.RemoteDiscovery#discover(java.lang.Class, net.xeoh.plugins.remotediscovery.options.DiscoverOption[])
     */
    public Collection<DiscoveredPlugin> discover(Class<? extends Plugin> plugin,
                                                 DiscoverOption... options) {

        refreshKnownDiscoveryManagers();

        //* new 2
        final CompletionService<Collection<DiscoveredPlugin>> compService = new ExecutorCompletionService<Collection<DiscoveredPlugin>>(Executors.newFixedThreadPool(2, this.threadFactory));

        this.checkCache.initCall(plugin, options);
        this.discover.initCall(plugin, options);

        List<Future<Collection<DiscoveredPlugin>>> futures = new ArrayList<Future<Collection<DiscoveredPlugin>>>();
        futures.add(compService.submit(this.checkCache));
        futures.add(compService.submit(this.discover));

        Future<Collection<DiscoveredPlugin>> completedFuture;
        Collection<DiscoveredPlugin> foundPlugins = null;

        while (!futures.isEmpty()) {
            try {
                completedFuture = compService.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }

            futures.remove(completedFuture);

            try {
                foundPlugins = completedFuture.get();

                if (foundPlugins != null && !foundPlugins.isEmpty() && !futures.isEmpty()) {
                    // so we found something, just remote the other one...
                    futures.get(0).cancel(true);
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        return foundPlugins;
    }

    /** */
    @net.xeoh.plugins.base.annotations.Timer(period = 260)
    public void discoverThread() {

        this.logger.fine("Starting new discover pass");

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
        // TODO: This callback handling needs improvement. Check for
        // unsynchronized access to the allRequest structure
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

        this.logger.fine("Disover pass ended.");
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remotediscovery.RemoteDiscovery#revokePlugin(net.xeoh.plugins.base.Plugin, net.xeoh.plugins.remote.PublishMethod, java.net.URI)
     */
    public void revokePlugin(Plugin plugin, PublishMethod publishMethod, URI uri) {
        // TODO Auto-generated method stub      
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

        //		this.checkCache.printCache();
        this.checkCache.saveCache();
    }

    private boolean awaitInitializationTime() {
        // Check if we're still on startup lock
        final long delay = this.timeOfStartup + this.startupLock - System.currentTimeMillis();
        if (delay > 0) {
            // If we are, halt this thread until lock time has passed.
            // Unfortunatly the lookup is a
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

    /**
     * Returns the remote proxy to a discovery manager for a given IP / port
     * 
     * @param ip
     * @param port
     * @return
     */
    @SuppressWarnings("unchecked")
    DiscoveryManager getRemoteProxy(String ip, int port) {
        try {

            final String address = ip + ":" + port;
            final int timeout = 500; // 500 ms RemoteAPIImpl if need detailed
            // version...

            final DiscoveryManager newClient = Proxies.newClient(EXPORT_NAME, address, RemoteDiscoveryImpl.class.getClassLoader(), DiscoveryManager.class);

            /*
             * TODO: With this one we still get the message:
             * 
             * "org.freshvanilla.net.VanillaDataSocket allocateBuffer
             * INFO: DiscoveryManager: Running low on memory, pausing..."
             * 
             * but, there is no TimeoutException....
             * 
             * @sa DiscoveredPluginTest.testMultiplePlugins()
             */
            @SuppressWarnings("unused")
            int pingRes = newClient.ping(123321);

            // Execute collection asynchronously (TODO: cache pool usage could be
            // improved)
            final ExecutorService cachePool = Executors.newCachedThreadPool();
            final ExecutorCompletionService<String> ecs = new ExecutorCompletionService(cachePool);
            final Future<String> future = ecs.submit(new Callable<String>() {

                public String call() throws Exception {
                    return AccessController.doPrivileged(new PrivilegedAction<String>() {
                        public String run() {
                            return newClient.toString();
                        }
                    });
                }
            });

            // Wait at most half a second (TODO: Make this configurable)
            try {
                final String string = future.get(timeout, TimeUnit.MILLISECONDS);
                /*
                 * TODO: Probably it is possible to put here some conversion routines... Because it looks like that
                 * only the ExportInfo makes trouble.... Or just make the ExportInfo much better, ie downwards compatible
                 */
                if (string == null || newClient.getVersion() != this.localManager.getVersion()) { return null; }

                return newClient;
            } catch (final InterruptedException e) {
                // TODO: This one is not an error anymore. 
                // Interruption is called because we are running cache thread.
                //				System.err
                //						.println("Error while waiting for a getRemoteProxy() result");
                //				e.printStackTrace();
            } catch (final ExecutionException e) {
                e.printStackTrace();
            } catch (final TimeoutException e) {
                e.printStackTrace();
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
        } finally {
        }
    }

    /**
     * Refreshes all known discovery managers.
     */
    private void refreshKnownDiscoveryManagers() {

        this.serviceInfosLock.lock();
        try {
            this.remoteManagersEndpoints.clear();

            // Check all service infos with discovery managers
            for (ServiceInfo serviceInfo : this.serviceInfos) {

                final InetAddress address = serviceInfo.getAddress();
                final int port = serviceInfo.getPort();

                System.out.println("mgr: " + address + " " + port);
                this.remoteManagersEndpoints.add(new RemoteManagerEndpoint(address, port));
            }
        } finally {
            this.serviceInfosLock.unlock();
        }
    }

    /**
     * Checks all known service infos for matches to the given class, returns
     * all matching entries.
     * 
     * @param plugin
     * @param options
     * @return
     */
    private Collection<DiscoveredPlugin> resolve(final Class<? extends Plugin> plugin,
                                                 DiscoverOption[] options) {

        // Refreshes all known remote discovery managers.
        //        refreshKnownDiscoveryManagers();

        // The result we intend to return
        Collection<DiscoveredPlugin> result = new ArrayList<DiscoveredPlugin>(); // new ArrayList<DiscoveredPlugin>();

        // Query all managers 
        for (final RemoteManagerEndpoint endpoint : this.remoteManagersEndpoints) {

            // The manager to use ...
            final DiscoveryManager mgr = RemoteDiscoveryImpl.this.getRemoteProxy(endpoint.address.getHostAddress(), endpoint.port);

            // No matter what happens, close the manager
            try {
                if (mgr == null) {
                    this.logger.info("Remote DiscoveryManager at " + endpoint.address.getHostAddress() + ":" + endpoint.port + " did not answer or has not the right version.");
                    continue;
                }

                // TODO: here are probably not the complete information as it was before....
                // RemoteDiscoveryImpl.this.logger.info("Error getting ping time of remote manager " + 
                //          endpoint.address + ":" + endpoint.port);
                final AtomicInteger pingTime = getPingTime(mgr, endpoint.address.getHostAddress(), endpoint.port);

                // Query the information (needs to be inside a privileged block, otherwise applets might complain.
                final ExportInfo exportInfo = AccessController.doPrivileged(new PrivilegedAction<ExportInfo>() {
                    public ExportInfo run() {
                        return mgr.getExportInfoFor(plugin.getCanonicalName());
                    }
                });

                // If the plugin in not exported, do nothing
                if (!exportInfo.isExported) continue;

                // If it is, construct required data.
                String hostAddress = (endpoint.address != null) ? endpoint.address.getHostAddress() : "127.0.0.1";
                DiscoveredPlugin discPlugin = constructPlugins(hostAddress, exportInfo.allExported, pingTime.get());
                this.checkCache.addToCache(plugin, endpoint, discPlugin);
                result.add(discPlugin);
            } catch (ClassCastException e) {
                // This should not be happen anymore....
                this.logger.warning(" Trying to convert a object from v2 to v3.");
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
        if (result.size() == 0) { return new ArrayList<DiscoveredPlugin>(); }

        //        doFilter(result, options);

        return result;
    }

    final DiscoveredPlugin constructPlugins(final String hostAddress,
                                            final Collection<ExportedPlugin> plugins,
                                            final int pingTime) {
        // If it is, construct required data.
        for (ExportedPlugin p : plugins) {
            final PublishMethod method = PublishMethod.valueOf(p.exportMethod);
            final URI uri = p.exportURI;

            String _newURI = "";

            _newURI += uri.getScheme();
            _newURI += "://";
            _newURI += hostAddress;
            _newURI += ":";
            _newURI += uri.getPort();
            _newURI += uri.getPath();

            try {
                // TODO: Compute distance properly.
                return new DiscoveredPluginImpl(method, new URI(_newURI), pingTime, p.timeSinceExport);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    final void doFilter(Collection<DiscoveredPlugin> plugins, DiscoverOption[] options) {
        final OptionUtils<DiscoverOption> ou = new OptionUtils<DiscoverOption>(options);

        DiscoveredPlugin best = plugins.iterator().next();

        if (ou.contains(OptionNearest.class)) {
            // Check all plugins
            for (DiscoveredPlugin p : plugins) {
                // If this one is closer, replace them
                if (p.getDistance() < best.getDistance()) {
                    best = p;
                }
            }

            // Remove all other plugins
            plugins.clear();
            plugins.add(best);
        }

        if (ou.contains(OptionYoungest.class)) {
            // Check all plugins
            for (DiscoveredPlugin p : plugins) {
                // If this one is closer, replace them
                if (p.getTimeSinceExport() < best.getTimeSinceExport()) {
                    best = p;
                }
            }

            // Remove all other plugins
            plugins.clear();
            plugins.add(best);
        }

        if (ou.contains(OptionOldest.class)) {
            // Check all plugins
            for (DiscoveredPlugin p : plugins) {
                // If this one is closer, replace them
                if (p.getTimeSinceExport() > best.getTimeSinceExport()) {
                    best = p;
                }
            }

            // Remove all other plugins
            plugins.clear();
            plugins.add(best);
        }
    }

    /*
     * we are passing the InterruptedException, because this fct can be interrupted by cache-thread.
     */
    Collection<DiscoveredPlugin> doRemoteDiscover(Class<? extends Plugin> plugin,
                                                  DiscoverOption... options)
                                                                            throws InterruptedException {
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

            // Also wait some time here, otherwise we won't find all plugins in
            // all cases
            awaitInitializationTime();

            // Wait at least one discoverThreadTurn
            final long lastDisoverValue = this.discoverThreadCounter.get();
            final long startOfWait = System.currentTimeMillis();
            while (this.discoverThreadCounter.get() == lastDisoverValue) {
                java.lang.Thread.sleep(100);

                // Safety check
                if (System.currentTimeMillis() > startOfWait + 1000) { throw new IllegalStateException("We are waiting way too long."); }
            }
        }

        for (DiscoverOption option : options) {

            // If we have a callback option, add to request list.
            if (option instanceof OptionCallback) {
                this.allRequests.add(new CallbackRequest(this, plugin, (OptionCallback) option, options));
            }
        }

        // Eventually resolve the request.
        return resolve(plugin, options);

    }

    final AtomicInteger getPingTime(final DiscoveryManager mgr, final String address,
                                    final int port) {
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
                    RemoteDiscoveryImpl.this.logger.info("Error getting ping time of remote manager " + address + ":" + port);
                }
                return null;
            }
        });

        return pingTime;
    }

    void syso(final String s) {
        synchronized (System.out) {
            System.out.println(s);
        }
    }
}
