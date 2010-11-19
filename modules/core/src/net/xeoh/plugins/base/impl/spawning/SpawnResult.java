package net.xeoh.plugins.base.impl.spawning;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.impl.registry.PluginMetaInformation;

/**
 * Spawn result, encapsulates the results of our attempt to spawn
 * a given plugin.
 * 
 * @author Ralf Biedert
 */
public class SpawnResult {

    /** The actual pluggable spawned */
    public final Plugin plugin;

    /** Information on this plugin */
    public final PluginMetaInformation metaInformation;

    /**
     * Creates a new spawn results.
     * 
     * @param plugin
     */
    public SpawnResult(Plugin plugin) {
        this.plugin = plugin;
        this.metaInformation = new PluginMetaInformation();
    }
}