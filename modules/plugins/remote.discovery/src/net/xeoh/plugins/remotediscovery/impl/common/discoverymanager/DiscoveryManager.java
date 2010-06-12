package net.xeoh.plugins.remotediscovery.impl.common.discoverymanager;

/**
 * 
 * 
 * @author rb
 *
 */
public interface DiscoveryManager {

    /**
     * Returns the version of this manager.
     *  
     * @return Version number 
     * @since 100
     */
    public int getVersion();

    /**
     * Return an export-info for the queried plugin.
     * 
     * @param pluginInteraceName
     * @since 100
     * @return .
     */
    public ExportInfo getExportInfoFor(String pluginInteraceName);

    /**
     * Performs a ping returning the passed value.
     * 
     * @param value 
     * @return value 
     * @since 100
     */
    public int ping(int value);

    /**
     * Returns the time since the startup of this manager 
     * 
     * @since 200
     * @return .
     */
    public long getTimeSinceStartup();
}
