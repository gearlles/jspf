package net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.impl;

import java.io.Serializable;
import java.net.URI;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.remote.PublishMethod;

/**
 * @author rb
 */
public class ExportEntry implements Serializable {
    /** */
    private static final long serialVersionUID = 953738903386891643L;

    /** */
    public Plugin plugin;

    /** */
    public PublishMethod method;

    /** */
    public URI uri;

    /** */
    public long timeOfExport;
}