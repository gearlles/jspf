package net.xeoh.plugins.base.impl;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.impl.registry.PluginMetaInformation;

/**
 * Spawn result, encapsulates the results of our attempt to spawn 
 * a given plugin.
 * 
 * @author Ralf Biedert
 */
public class SpawnResult {
    /**
     * Specifies if the object was a plugin or pluglet.
     * 
     * @author Ralf Biedert
     */
    public static enum SpawnType {
        /** First class plugin citizens. */
        PLUGIN,
        /** Small pluglets. */
        PLUGLET
    }

    /** The actual pluggable spawned */
    public final Plugin plugin;

    /** Information on this plugin */
    public final PluginMetaInformation metaInformation;

    /** Type of this spawn */
    public final SpawnType spawnType;

    /**
     * Creates a new spawn results. 
     * 
     * @param plugin
     * @param type 
     */
    public SpawnResult(Plugin plugin, SpawnType type) {
        this.plugin = plugin;
        this.spawnType = type;
        this.metaInformation = new PluginMetaInformation(this.spawnType);
    }
}