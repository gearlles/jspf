package quickstart.outputservice.impl;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.Init;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;
import quickstart.dataservice.DataService;
import quickstart.outputservice.OutputService;

/**
 * @author rb
 *
 */
@PluginImplementation
public class TheOtherOutputServiceImpl implements OutputService {

    /**
     * Even more magic. If the service has been loaded (ensured by the requiredPlugins)
     * before this plugin, inject it.
     */
    @InjectPlugin(requiredCapabilities = { "plugin:DataService" })
    public DataService service;

    public void doSomething() {
        System.out.println(this.service.provideData() + " another one bites the dust");
    }

    /**
     * Magic. Will be called after the plugin is fully loaded.
     */
    @Init
    public void initA() {
        System.out.println("This time I AM THERE");
    }

    /**
     * Magic. Will be called after the plugin is fully loaded.
     */
    @Init
    public void initB() {
        System.out.println("It's a little longer");
    }
}
