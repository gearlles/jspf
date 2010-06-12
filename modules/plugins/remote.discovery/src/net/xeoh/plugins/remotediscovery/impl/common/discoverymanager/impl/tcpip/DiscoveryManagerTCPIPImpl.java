package net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.impl.tcpip;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.remote.PublishMethod;
import net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.DiscoveryManager;
import net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.ExportInfo;
import net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.ExportedPlugin;
import net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.impl.AbstractDiscoveryManager;
import net.xeoh.plugins.remotediscovery.impl.common.discoverymanager.impl.ExportEntry;

/**
 * @author Ralf Biedert
 */
public class DiscoveryManagerTCPIPImpl extends AbstractDiscoveryManager implements
        DiscoveryManager {

    /** A list of all exported entities we have */
    public final Collection<ExportEntry> allExported = new ArrayList<ExportEntry>();

    
    /* (non-Javadoc)
     * @see net.xeoh.plugins.remotediscovery.impl.v2.DiscoveryManager#getExportInfoFor(java.lang.String)
     */
    public ExportInfo getExportInfoFor(String pluginInteraceName) {
        this.logger.fine("Was queried for plugin name " + pluginInteraceName);

        final ExportInfo exportInfo = new ExportInfo();
        exportInfo.isExported = false;
        exportInfo.allExported = new ArrayList<ExportedPlugin>();

        for (ExportEntry entry : this.allExported) {
            final Collection<Class<? extends Plugin>> allPluginClasses = getAllPluginClasses(entry.plugin);
            final Collection<String> allPluginClassNames = new ArrayList<String>();

            for (Class<?> c : allPluginClasses) {
                allPluginClassNames.add(c.getCanonicalName());
            }

            // Now check if the plugins is really exported
            if (!allPluginClassNames.contains(pluginInteraceName)) continue;

            // It is, add it to the list
            exportInfo.isExported = true;

            final ExportedPlugin exported = new ExportedPlugin();
            exported.exportMethod = entry.method.name();
            exported.exportURI = entry.uri;
            exported.port = entry.uri.getPort();
            exported.timeSinceExport = System.currentTimeMillis() - entry.timeOfExport;

            exportInfo.allExported.add(exported);
        }

        return exportInfo;
    }

    /**
     * Tells the manager that a new plugin is available. 
     * 
     * @param plugin
     * @param method
     * @param url
     */
    @Override
    public void anouncePlugin(Plugin plugin, PublishMethod method, URI url) {

        final ExportEntry entry = new ExportEntry();
        entry.plugin = plugin;
        entry.method = method;
        entry.uri = url;
        entry.timeOfExport = System.currentTimeMillis();

        synchronized (this.allExported) {
            this.allExported.add(entry);
        }
    }

    /**
     * @param plugin
     * @param publishMethod
     * @param uri
     */
    @Override
    public void revokePlugin(Plugin plugin, PublishMethod publishMethod, URI uri) {
        final Collection<ExportEntry> toRemove = new ArrayList<ExportEntry>();

        synchronized (this.allExported) {
            for (ExportEntry e : this.allExported) {
                if (e.plugin == plugin && e.method == publishMethod && e.uri.equals(uri))
                    toRemove.add(e);
            }

        }
        this.allExported.removeAll(toRemove);
    }
}
