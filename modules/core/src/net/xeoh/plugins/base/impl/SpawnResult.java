package net.xeoh.plugins.base.impl;

import net.xeoh.plugins.base.Pluggable;
import net.xeoh.plugins.base.impl.registry.PluggableMetaInformation;

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
    public final Pluggable pluggable;

    /** Information on this plugin */
    public final PluggableMetaInformation metaInformation;

    /** Type of this spawn */
    public final SpawnType spawnType;

    /**
     * Creates a new spawn results. 
     * 
     * @param plugin
     * @param type 
     */
    public SpawnResult(Pluggable plugin, SpawnType type) {
        this.pluggable = plugin;
        this.spawnType = type;
        this.metaInformation = new PluggableMetaInformation(this.spawnType);
    }
}