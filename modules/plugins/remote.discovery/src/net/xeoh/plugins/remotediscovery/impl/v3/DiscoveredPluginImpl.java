package net.xeoh.plugins.remotediscovery.impl.v3;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import net.xeoh.plugins.remote.PublishMethod;
import net.xeoh.plugins.remotediscovery.DiscoveredPlugin;

/**
 * @author Thomas Lottermann
 */
public class DiscoveredPluginImpl implements DiscoveredPlugin {

    private final List<String> capabilities;
    private final PublishMethod publishMethod;
    private final URI publishURI;
    private final int distance;
    private final long timeSinceExport;

    /**
     * @param capabilities
     * @param publishMethod
     * @param publishURI
     * @param distance 
     * @param timeSincceExport 
     */
    public DiscoveredPluginImpl(List<String> capabilities, PublishMethod publishMethod,
                                URI publishURI, int distance, long timeSincceExport) {
        this.capabilities = capabilities;
        this.publishMethod = publishMethod;
        this.publishURI = publishURI;
        this.distance = distance;
        this.timeSinceExport = timeSincceExport;
    }

    /**
     * @param publishMethod
     * @param publishURI
     * @param distance 
     */
    public DiscoveredPluginImpl(PublishMethod publishMethod, URI publishURI, int distance) {
        this(new ArrayList<String>(), publishMethod, publishURI, distance, 0);
    }

    /**
     * @param publishMethod
     * @param publishURI
     * @param distance
     * @param timeSinceExport
     */
    public DiscoveredPluginImpl(PublishMethod publishMethod, URI publishURI,
                                int distance, long timeSinceExport) {
        this(new ArrayList<String>(), publishMethod, publishURI, distance, timeSinceExport);
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remotediscovery.DiscoveredPlugin#getCapabilities()
     */
    public List<String> getCapabilities() {
        return this.capabilities;
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remotediscovery.DiscoveredPlugin#getPublishMethod()
     */

    public PublishMethod getPublishMethod() {
        return this.publishMethod;
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remotediscovery.DiscoveredPlugin#getPublishURI()
     */
    public URI getPublishURI() {
        return this.publishURI;
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remotediscovery.DiscoveredPlugin#getDistance()
     */
    public int getDistance() {
        return this.distance;
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.remotediscovery.DiscoveredPlugin#getTimeSinceExport()
     */
    public long getTimeSinceExport() {
        return this.timeSinceExport;
    }

}
