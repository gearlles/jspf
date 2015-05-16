We try to explain by example here, so adopt the strategies to your scenario accordingly.




# More Examples #

```
@PluginImplementation
@Author(name="Ralf Biedert")
public class BigPluginImpl implements BigPlugin {

    @InjectPlugin
    public CoolPlugin coolPlugin;

    @InjectPlugin
    public InformationBroker broker;


    @Thread
    public void backgroundTask() {
        while(true) broker.publish(new StringItem("item:uri", "value"));
    }

    @Init
    public void init() {
        connectDevice();
    }

    @Shutdown
    public void shutdown() {
        sayGoodbye()
    }

    @Capabilities
    public String[] caps() {
        return new String[] {"mimetype/big", "functionality:example"};
    }

    @PluginLoaded
    public void newPlugin(RemoteAPI plugin) {
        System.out.printf("Detected new plugin " + plugin);
    }
}
```

<br /><br />


# JSPF and Scala #

Starting with v0.8, implementing plugins in Scala should work well. The only noticeable difference is the use of InjectPlugin:

```
@PluginImplementation
class TestScalaBusImpl extends TestBus {

    // Use InjectPlugin like this (JSPF v0.8 with Scala 2.8)
    @(InjectPlugin @beanSetter)   
    @BeanProperty
    var pm:PluginManager = null
          
    @Override
    def sendOnBus() = { }
        
    @Init
    def init() = {
        println("Hello World")
    }
    
    @PluginLoaded()
    def newPlugin(p:Plugin) = {
        println("loaded + " + p)
    }
}

```

<br /><br />


# Exporting and Importing Plugins over the Net #

Exporting and importing is really easy. And the best thing is: you don't have to hassle with host names or port numbers.

```
// Get exporter, this one uses Lipe
RemoteAPI remote = pm.getPlugin(RemoteAPILipe.class)

// after this line returns the plugin is exported
remote.exportPlugin(myPlugin);

// import it again over the network, detect its location automatically
MyPlugin myPluginNetwork = remote.getRemoteProxy(new URI("discover://any"), MyPlugin.class);


// Also supported
//     discover://nearest  - If several sources are found, use the one with the lowest ping
//     discover://youngest  - Use the one with the least uptime (e.g., the one just started)
//     discover://oldest  - Use the one with the longest uptime

```

Note: It is recommended that you use either _any_ or _nearest_, as both will be the fastest methods if the remote endpoint resides on the same machine. This is, however, no requirement, the other options work as well.

<br /><br />

# Enable Caching #

The following code should give you faster loading times after the first startup:

```
final JSPFProperties props = new JSPFProperties();
        
props.setProperty(PluginManager.class, "cache.enabled", "true");
props.setProperty(PluginManager.class, "cache.mode",    "weak"); //optional
props.setProperty(PluginManager.class, "cache.file",    "jspf.cache");

PluginManager pm = PluginManagerFactory.createPluginManager(props);
```




<br /><br />



# Understanding Options #

Good software has small and intuitive interfaces. At the same time however, you want them to be extensible and flexible. For the JSPF we created a flexible options mechanism  which allows for both. Lets have a look at the PluginManager interface, the one you'll be in contact most frequently:


```

public interface PluginManager extends Plugin {

    /** Adds plugins from a given source*/
    public void addPluginsFrom(URI url, AddPluginsFromOption... options);

    /** Returns a given plugin */
    public <P extends Plugin> P getPlugin(Class<P> plugin, GetPluginOption... options);

    /** Shuts down this manager. */
    public void shutdown();
}
```

As you can see, it only has three methods (an no overloads) and still enables you to perform many more operations in detail that are implicitly available. A very simple example of an unnecessary method is _pluginExists(Plugin p)_, which can just be imitated by calling _getPlugin()_ and checking for a null value. However, there are even more possibilities.

Often it is the case that you want to provide a core set of functions along with a number of _derivates_ of these, which behave differently but do, in principle, almost the same thing.   In the case above this could be _getPlugin()_, in it's standard definition the functions says  _"I return you an object implementing the given interface, I suppose you don't care which one."_. However, very often you also want to provide derivations of the base functionality. This is where options come into play. How do they work?

Options in our framework always look the same. Like in the example above you have, you have a "_base call_" which takes a (very small) number of fixed parameters, like a Plugin-class in this case, and a varargs set of objects implementing a given interfaces specific to the method:

<br />

```
    public <P extends Plugin> P getPlugin(Class<P> plugin, GetPluginOption... options);
```

<br />

This mechanism has a number of advantages:

  * All options are type safe. You can only pass options which have been specifically designed for the method. It is (more or less) impossible to accidentally pass an object the method wouldn't expect, or at least gracefully ignore.

  * The number of options is unlimited. You can pass zero or twenty of them and you don't have to define the number or their order beforehand.

  * Options can be added or removed later on without changing the interface.

  * In case no options are needed by the user, he doesn't even see them, as unused varargs don't have to be mentioned or set to null.

<br />

Calls to the method _getPlugin()_ can be seen below. All of them are accepted by the same method.

```
pm.getPlugin(MyPlugin.class);    
pm.getPlugin(MyPlugin.class, new OptionID("id:3"));    
pm.getPlugin(MyPlugin.class, new OptionFromAuthor("donald.duck"));    
pm.getPlugin(MyPlugin.class, new OptionID("id:3"), new OptionWrapInBenchmark());    
```


<br />

Handling these options inside the plugin is equally straightforward, you can use the already provided _OptionsUtils_ class. Here are a number of example use cases:

```
public <P extends Plugin> P getPlugin(final Class<P> requestedPlugin,
                                          GetPluginOption... options) {

    // Create our options handler
    OptionUtils<GetPluginOption> ou = new OptionUtils<GetPluginOption>(options);
    

    // Return the value of Option X and use a default value if it's not there 
    int myValue =  ou.get(OptionX.class, new OptionX(667)).getX();

    // Check if some option is there
    if(ou.contains(OptionY.class)) {
        doSomething()
    }   
```


As you can see, creating interfaces this way gives you great flexibility and keeps the interfaces small but yet extensible. The obvious disadvantages of this method are that it takes usually a minute or two to design the interface and create the corresponding options classes, and that it's not recommended for _high-performance_ interfaces which are called very often (like several thousand invocations per second) with multiple arguments. Decide for yourself if the costs outweigh the benefits.


<br />



<br /><br />




# Recommended Layout #

We usually create a package of the structure org._domain_._applicationname_. Within this folder you would find a single application main class, containing, amongst others, this code:

```
PluginManager pm = PluginManagerFactory.createPluginManager();
pm.addPluginsFrom(new URI("classpath://*"));
```

These lines will create a new plugin manager instance and tell it to load all classes it sees within the classpath. Except applets and some special cases this should work just fine. Of course you might want to add other locations as well (for example a special plugin-directory).

Below the main package we ususally create a subpackage named _plugins_ or _services_. Inside this package go other subpackages, one for each plugin:

  * plugins
    * storage
    * uriservice
    * systemapi

Inside a single plugin directory, you'd now place the _interface_ of the plugin. How you design your interface is totally up to you, be we'd strongly recommend to keep it small. You could place, for example, inside the package org._domain_._applicationname_.plugins.systemapi the following interface file:

```
public interface SystemAPI extends Plugin {
    /** Tells the system to open the given url using the default application */
    public void openURL(URL href);
}
```

Enums and other interfaces could go here as well. The only thing remaining would be the interface's implementation.
> This usually goes into an _impl_ subfolder, one for each plugin:

  * systemapi
    * SystemAPI.java
    * impl
      * win32
        * SystemAPIImpl.java
      * linux
        * SystemAPIImpl.java
      * mac
        * SystemAPIImpl.java


In there one SystemAPIImpl.java file might look like that:



```
@PluginImplementation
public class SystemAPIImpl implements SystemAPI {
    @Init
    public boolean init() {
        return getOS().equals("OSX");
    }

    public void openURL(URL href) {
         doSomething(href);
    }
}
```

<br /><br />


# Plugin Design Practices #

These hints are by no means required. For small projects you should not encounter any problems if you put all plugins into one big classpath. However, if you intend to publish you plugins as single-JAR plugins for general usage, consider these points:

  * A well designed plugin consists of a small and self-disclosing interface. Inside this interface try to use, if possible, rather standard java types than specialized classes or other interfaces. Also, instead of building one large interface, create several small ones.

  * Consider using options (see above) instead of several overloaded methods.

  * If you publish single-JAR-plugins: Every plugin-JAR should be self contained. This means the plugin must be error-free-inspectable by other users  who only have the core plugin available. To achieve this, the plugin-jar should contain all interfaces it depends on and all libraries it uses (`*`see the 2nd comment in the comment section below). If you do not follow this rule, this can happen:

> When an _incomplete plugin_ is deployed into a plugin directory it might get loaded at a random point in time. If a class in your plugin depends on an interface or class that is not in the classpath (and not in your plugin, because you forgot to put it in there), its creation will fail due to a  ClassNotFoundException. If however, you put all dependent interfaces and classes into its jar, the framework will be able to load it properly and inspect its explicitly defined dependencies (using the  requiredPlugins() attribute). It can then be put on hold until all these are loaded as well.

  * If possible, try to deliver two artifacts: your plugin as a whole, self contained with all related libraries, and a very small interface-jar that only contains your interfaces. This interface jar may be included by other plugin vendors using or implementing that interface. It is also safe to add the interface to an application's classpath.