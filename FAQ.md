# Usage #

  * **What are the default plugins?**

> Any freshly created `PluginManager` will contain at least these plugins:  [PluginInformation](http://data.xeoh.net/jspf/api/net/xeoh/plugins/base/PluginInformation.html), [InformationBroker](http://data.xeoh.net/jspf/api/net/xeoh/plugins/informationbroker/InformationBroker.html), [PluginConfiguration](http://data.xeoh.net/jspf/api/net/xeoh/plugins/base/PluginConfiguration.html) (and, for convenience, a circular reference to the [PluginManager](http://data.xeoh.net/jspf/api/net/xeoh/plugins/base/PluginManager.html) itself). You can get them by using the `getPlugin()` function, or by injecting them into other plugins (see next entry).


  * **How can I obtain the plugin manager from inside a plugin?**

> The plugin manager that created your component can be obtained by adding these lines to your component's implementation:

```
@InjectPlugin
public PluginManager pluginManager;
```

> The name of the field does not matter, it only has to be public.



  * **What is the best export method for plugins?**

> There is no best one. In general, however, I prefer LipeRMI, because it supports callbacks and transparently wraps complex return types. XMLRPC is nice for IPC with other languages but does not support all data types, ERMI claims to be the fastest one but lacks callbacks. If you are in doubt which one you should use, just use several ones at the same time. Plugins can be exported concurrently with different mechanisms without problems.


  * **Does it work inside Java Applets?**

> Yes it does. However, you have to sign the applet. Additionally, all plugins have to be enabled manually (see below).

  * **How do I enable a plugin _manually_? Why would I want to do that?**

> Enabling a plugin manually means that you tell the manager directly which class it should try to spawn as a plugin. While you lose some comfort, this allows you to spawn plugins in certain conditions where access to the classpath is not easily possible; applets are such an example. You spawn plugins manually like this:

```
pm.addPluginsFrom(new URI("classpath://net.xeoh.plugins.remote.impl.lipermi.RemoteAPIImpl"));

// or

pm.addPluginsFrom(ClassURI.PLUGIN(RemoteAPIImpl.class));

```



  * **How can I list all Plugins implementing a certain interface?**

> Use the Manager's util class:

```
PluginManagerUtil pmu = new PluginManagerUtil(pluginManager);
pmu.getPlugins(MyInterface.class);
```





  * **My application does not shut down properly since I started using JSPF ...**

> Technically that's not a question. However, the reason for this behavior is usually that some plugin you used created a non-daemonic background thread, like for example remote plugins which exported functionality over the network.  All you have to do is to call the PluginManager's shutdown method so that all plugins know when they aren't needed anymore, e.g. at the end of the main method.

```
pm.shutdown()
```



  * **Can I download plugins from the net?**

> This feature is enabled, but experimental. Only use it if you trust the plugin's author, the server and your communications infrastructure. It works just like you'd expect it to work:


```
pm.addPluginsFrom(new URI("http://server.com/plugin.jar"));
```


  * **Does it handle circular dependencies (i.e. A requires B to spawn and B requires A)?**

> No, and it never will. Circular dependencies are ugly and it is usually easy to replace them.



  * **The first call to find a remote service takes ~1s, why?**

> In case you use the automatic discovery jmDNS needs some _warmup_ time. After the discovery started the first time, we delay all calls until one second has elapsed. After this first second no delay should be noticeable.




  * **The startup takes 1-2 seconds, can I improve this time?**

> Yes, just enable caching. See the [Usage Guide](UsageGuide.md) how this is done. The reason for this delay is that JSPF has to search the whole classpath for existing plugins. If you enable caching a list of found plugins is saved between invocations.


  * **What is caching?**

> JSPF can memorize all plugins it found within a JAR file and store them for later use. This feature is disabled by default. See the previous point.



  * **What is weak caching?**

> In some cases the generation of an MD5 ('strong' for our purposes; yes, we know that MD5 has already been broken for security related applications) can take some time. If you enable weak hashing the PluginManager will not at all try open the file, but just take into account its filename and size.


  * **Some simple methods seem to hang / throw exceptions unexpectedly.**

> A couple of times I had bugs with apparently simple methods that do not _work as expected_. In all these circumstances I forgot that this method belonged to an object which was imported using a RemoteAPI over the network. If the connection breaks, for example because the peer was closed in the meantime, all methods of this object will throw exceptions or won't respond unless they timeout.

> Please notice that, due to the convenience some RemoteAPI plugins offer, even very deeply nested objects like Listeners, Callbacks and others are transparently exported as well, which might lead to problems at places not obviously connected to some remote functionality.



  * **What is a multiplugin? How do I use it?**

> We had a number of requests from people who wanted different plugins to share class data and dependencies. _Multiplugins_ are the current solution to that. You create a multiplugin simply by creating a folder in the form `monster.plugin`, i.e., the folder has to end with `.plugin`. Now you can put a number of JARs into it, which will all be shared by the same class-loader, thus sharing code and dependencies. Multiplugins are treated the same way as ordinary plugins:

```
pm.addPluginsFrom(new File("plugins/").toURI()); // Load all plugins, including multiplugins
pm.addPluginsFrom(new File("plugins/monster.plugin").toURI()); // Only load the given multiplugin.
```

> Right now (version 0.9.1) multiplugins are beta. Please give feedback in the forum.



  * **How can I add plugins in nested directories?**

> With a simple [jCores](http://jcores.net) line (already included in JSPF) you can find all JARs below a given folder and add them individually:

```
List<File> list = $("plugins/").file().dir().filter(".*jar$").print().list();
for (File file : list) {
    pm.addPluginsFrom(file.toURI());
}
```


  * **Does it work with GWT?**

> No, JSPF depends on quite a number of features the standard Java 6 platform provides and there is no simple way how this could be made working inside a browser.




<br /><br />


# Development #

  * **Why are there no start() / stop() methods for plugins? Why do all plugins start automatically?**

> Because otherwise it wouldn't be _simple_ anymore. If you want to start plugins explicitly feel free to create an `ActivatablePlugin` that provides start and stop methods and derive your plugins from that.


  * **Does it mix well with static initializers?**

> Short answer: _No_. Long answer: _No, it doesn't_.  Static initializers are an abomination and cause many problems in complex cases where multiple classloaders are involved -- which is the case for JSPF. So, if you enjoy doing many things in static initializers, better refrain from using JSPF. Final static variables however (like constants) are perfectly fine.



  * **How does classloading work internally?**

> The specific details might change between versions, but in principle you can assume the following: If the plugin framework detects that it is in Applet-mode, then it will use the classloader which created the PluginManager. In case you add plugins from the classpath all these items should share the same classloader. In case you add plugins from other sources (like external JARs), the PluginManager assumes they are self contained and puts each of them into their own ClassRealm (aka classloader).


  * **I need to be able to unregister plugins 'real-time', without having to restart the application. Is there a way?**

> Unloading a plugin isn't possible, and that it is very very hard to implement it properly. Reason:

> As soon as you load a plugin "P" it will be registered at several places (that could be fixed).

> However, as soon as another plugin Q request P then it will have a reference to it. If you know just 'removed' P then Q would also lose all its state changes it did to P; for example: You load P and Q, and Q invokes P.startSomething(). Now P starts running, but now yet another process tries to hot-swap P. You remove P from the manager and all references inside Q; load a new P (let's call it P') and then you set P' inside Q again.

> Now however Q still thinks that it sees its old P and also thinks that it should have been already started with .startSomething().

> Unfortunately there is no way to handle this situation gracefully. Thats why JSPF doesn't support it.



<br /><br />


# Stability #

  * **Why is the version number so low?**

> There is no real reason. As soon as I have the feeling that a critical mass of people have tested the framework the version will turn to 1.0


  * **Is it stable?**

> A couple of people have used it for several projects now and never had any show stoppers. There are, of course, still unknown bugs within, as well as unimplemented features. You should keep in mind however that the framework only _helps_ you writing clean and modularized code, it doesn't do it on its own.

  * **What platforms are supported?**

> The framework has been successfully tested on Mac OS X, Windows and Linux with Java 6.




<br /><br />

# Troubleshooting #

  * **I get an java.lang.InstantiationException: <your plugin name here>**

> Most likely your plugin has no no-arg constructor, try adding one and you should be fine.


  * **I get an java.lang.ClassNotFoundException: net.xeoh.plugins.base.impl.JARCache$JARInformation on startup...**

> In version 0.6.2 we changed some cache details. Old cache files cannot be loaded anymore, just delete the file, it will be recreated on the next application startup. This exception does not change the runtime behavior of your app, it is caught gracefully and only shown due to curiosity reasons ;-)

  * **I get an java.lang.AbstractMethodError.**

> Happened to us once when we mixed an very old plugin with a number of new ones. Frankly no idea what the real cause was, but it went away when we updated the old version.

  * **Some annotations don't seem to work! / I get NullPointer exceptions inside my plugins!**
> You most likely forgot to make the corresponding item public, or the requested plugin simply wasn't found.

  * **I get an Exception in thread "main" java.lang.ArrayStoreException: sun.reflect.annotation.TypeNotPresentExceptionProxy.**
> (Should apply only to old versions) Most likely you forgot to include a plugin an other plugins depends on. For example the discovery plugin when using remote plugins.


  * **I put a number of plugin into a ZIP file, but they don't load. What's wrong?**

> JSPF doesn't inspect ZIP files (or nested JARs) and I am undecided if this feature will be implemented in the future. For the moment, please put all plugin JARs into a plain vanilla folder.


  * **Some plugins just don't seem to load, how can I debug what's going on?**

> Since version 0.9.1 the best way to start debugging is to add plugins with the `OptionReportAfter` option. It should print the internal status when it's done:

```
pm.addPluginsFrom(new URI("classpath://*"), new OptionReportAfter());
```

> Plugins with a status of **SPAWNED** should be fine. **CONTAINS\_UNRESOLVED\_DEPENDENCIES** means there is another plugin missing, **DISABLED** means the plugin has been manually disabled, **FAILED** likely means there was error in the constructor / init() method of your plugin.

  * **I added a plugin with addPluginsFrom(new File(".../MyPlugin.class").toURI()); but it does not load. What is wrong?**

> Adding plugins like this is not supported. You cannot add a single plugin by its file path. Only the methods listed on the [front page](http://code.google.com/p/jspf) are supported. Specifically we recommend using `classpath://*`, `plugins/`  or `plugins/myplugin.jar`.

<br /><br />

# General #

  * **Why do I need this? All you achieve can be done otherwise, too!**

> You probably don't need it if you ask. From a technical point of view, this framework solves nothing that could not be done otherwise by other solutions. So it's not about the _what_, it's about the _how_.

> There are so many component frameworks, library toolkits, techniques, whatever out there which allow you to create different implementation of the same service. Most of them have, however, one big problem: the are, well, big and bulky and need lot of configuration before something works. This framework, on the other hand, tries to walk the Python way: simple things are kept simple and should work out of the box.


  * **So does this try to replace JPF, OSGi, ... ?**

> No! JSPF was _not_ created to compete with any existing plugin-framework solution -- at least not the ones we know.  Please also have a look at these projects to find out what's best for you [JPF](http://jpf.sourceforge.net/) or [OSGi](http://en.wikipedia.org/wiki/OSGi).


  * **What is the targeted audience?**

> We see two principal scenarios to use the Java Simple Plugin Framework:  1) Teams, regardless of their size, who just want to use plugin framework to extend their existing projects with dynamic code loading.  2) Teams of about 1 - 10 people who create a new application from the scratch, but still want to have clean code, reusability and all the other benefits it offers.





  * **What does this _"5 minutes and it works"_ mean?**

> The full version of the _slogan_ is: _Even if you never used this framwork before, if it takes you more than five minutes to develop your first plugin, we did something wrong._


  * **Really?**

> If you are an experienced Java programmer (but have no idea about any specific plugin framework), we really think it shouldn't take you more than five minutes to get started.