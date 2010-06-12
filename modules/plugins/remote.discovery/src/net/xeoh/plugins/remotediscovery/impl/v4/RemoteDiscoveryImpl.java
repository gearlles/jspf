package net.xeoh.plugins.remotediscovery.impl.v4;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.Init;
import net.xeoh.plugins.base.annotations.events.Shutdown;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;
import net.xeoh.plugins.base.util.OptionUtils;
import net.xeoh.plugins.remote.PublishMethod;
import net.xeoh.plugins.remotediscovery.DiscoveredPlugin;
import net.xeoh.plugins.remotediscovery.RemoteDiscovery;
import net.xeoh.plugins.remotediscovery.impl.v4.probes.local.LocalProbe;
import net.xeoh.plugins.remotediscovery.impl.v4.probes.network.NetworkProbe;
import net.xeoh.plugins.remotediscovery.options.DiscoverOption;
import net.xeoh.plugins.remotediscovery.options.discover.OptionNearest;

/**
 * 
 * 
 * @author Ralf Biedert
 */
@PluginImplementation
public class RemoteDiscoveryImpl implements RemoteDiscovery {

    /**
     * Constructs daemonic threads
     */
    public static ThreadFactory threadFactory = new ThreadFactory() {

        public java.lang.Thread newThread(Runnable r) {
            java.lang.Thread rval = new java.lang.Thread(r);
            rval.setDaemon(true);
            return rval;
        }
    };

    /** 
     * out of net.xeoh.plugins.remote.impl.ermi.RemoteAPIImpl
     * @return .
     */
    public static int getFreePort() {
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
    public PluginManager pluginManager;

    /** */
    final Logger logger = Logger.getLogger(this.getClass().getName());

    /** All of our probes */
    NetworkProbe networkProbe;
    
    /** Local probe */
    LocalProbe localProbe;

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remotediscovery.RemoteDiscovery#announcePlugin(net.xeoh.plugins.base.Plugin, net.xeoh.plugins.remote.PublishMethod, java.net.URI)
     */
    public void announcePlugin(Plugin plugin, PublishMethod publishMethod, URI uri) {
        this.localProbe.announcePlugin(plugin, publishMethod, uri);
        this.networkProbe.announcePlugin(plugin, publishMethod, uri);
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remotediscovery.RemoteDiscovery#discover(java.lang.Class, net.xeoh.plugins.remotediscovery.options.DiscoverOption[])
     */
    public Collection<DiscoveredPlugin> discover(final Class<? extends Plugin> plugin,
                                                 final DiscoverOption... options) {

        final OptionUtils<DiscoverOption> ou = new OptionUtils<DiscoverOption>(options);
        
        this.logger.fine("Discover request for plugin " + plugin);
        
        // Check if we are allowed to use the local discovery (nearest or nothing specified), 
        // and return with it instantly.
        if(ou.contains(OptionNearest.class) || options.length == 0) {
            this.logger.fine("Using local discovery");
            Collection<DiscoveredPlugin> discover = this.localProbe.discover(plugin, options);
            if(discover.size() > 0) {
                this.logger.fine("Results found, returning these.");
                return discover;
            }
            
            this.logger.fine("Falling back to classic.");
        }
        return this.networkProbe.discover(plugin, options);
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remotediscovery.RemoteDiscovery#revokePlugin(net.xeoh.plugins.base.Plugin, net.xeoh.plugins.remote.PublishMethod, java.net.URI)
     */
    public void revokePlugin(Plugin plugin, PublishMethod publishMethod, URI uri) {
        this.networkProbe.revokePlugin(plugin, publishMethod, uri);
        this.localProbe.revokePlugin(plugin, publishMethod, uri);
    }

    /** */
    @Init
    public void init() {
        this.networkProbe = new NetworkProbe(this.pluginManager);
        this.localProbe = new LocalProbe(this.pluginManager);

        this.networkProbe.startup();
        this.localProbe.startup();
    }

    /** */
    @Shutdown
    public void shutdown() {
        this.networkProbe.shutdown();
        this.localProbe.shutdown();
    }
}
