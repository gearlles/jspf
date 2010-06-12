package net.xeoh.plugins.base.impl;

import net.xeoh.plugins.base.Pluggable;
import net.xeoh.plugins.base.impl.registry.PluggableMetaInformation;

/**
 * Spawn result 
 * 
 * @author Ralf Biedert
 *
 */
public class SpawnResult {
    /**
     * @author rb
     */
    public static enum SpawnType {
        /**
         * 
         */
        PLUGIN,
        /**
         * 
         */
        PLUGLET
    }

    /** */
    public final Pluggable pluggable;

    /** */
    public final PluggableMetaInformation metaInformation;

    /** */
    public final SpawnType spawnType;

    /**
     * @param plugin
     * @param type 
     */
    public SpawnResult(Pluggable plugin, SpawnType type) {
        this.pluggable = plugin;
        this.spawnType = type;
        this.metaInformation = new PluggableMetaInformation(this.spawnType);
    }
}