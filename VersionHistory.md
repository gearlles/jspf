## (0.9.0, May 2010) ##
  * Prevented plugins from being loaded several times
  * And again a new discovery (v4 this time). Should be significantly faster for local lookups.
  * Fixed some texts and warnings
  * Fixed LipeRMI console spamming
  * Fixed export URIs (e.g., http://xxx/MyServiceImpl -> http://xxx/de.interface.MyService/1 )
  * Fixed and realigned a number of property names (e.g., port -> server.port in xmlrpc)
  * Prepared @RecognizesOption, can be used to emit warning if a plugin doesn't understand an option

<br /><br />

## (0.8.0, January 2010) ##
  * Meta handling introduced. You can now intercept calls to plugins and alter parameters or the return value. This works by using 1) the PluginSupervisor and 2) by setting "supervision.enabled" to "true".
  * Added warnings if plugins start too slowly. This should give you an indicator where to start optimizing for startup performance.
  * ClassURI added. Allows for really fast startups in the case you need them.
  * logging.level is now a property of PluginManager.class, simplifies debugging  a bit.
  * Pluglets (Just for testing, may be removed again in the future)
  * discover://youngest, discover://oldest. Now you can actually discover the most recent / longest running version of a network service.
  * @InjectPlugin now also works on methods (just make the method accept a plugin of some type)
  * Added Scala support (see the [Usage Guide](UsageGuide.md) for details)
  * Significantly improved the speed of the cache. (Makes the startup even faster)
  * Switched to Java 1.6. (And again some speedups)
  * Introduced simple function caching (Preliminary...)
  * Tons of bugs and deadlocks fixed.

<br /><br />




## 0.7.0 (2009/08/25) ##

  * In @InjectPlugin the option requiredPlugins() == removed, these are automatically inferred by looking at the @InjectPlugin options.
  * Removed TrueZip. Now we handle JARs on our own. This results in
  * A greatly reduced core size, now only 120k
  * Revised caching. Plugins should load faster now.
  * Introduced weak hashing. Should improve startup even more (must be enabled "cache.mode"="weak")
  * Improved startup speed of discovery plugin.

<br /><br />

## 0.6.1 (2009/??) ##

  * Introduced JSPFProperties
  * Cache settings are now PluginManager.cache.enabled and PluginManager.cache.file
  * Plugins may now be added from HTTP sources (cool, but DANGEROUS. Check that you trust the author, server and network)
  * Fixed a bug in the remote discovery which could have stopped the network detection from working in certain situation

<br /><br />

## 0.6.0 (2009/08/04) ##

  * removed three jars which should already be inside the JRE (sax2 & related)
  * added an modified version of LIPE RMI. Callbacks are now supported.
  * changed the discovery, multiple exports on the same port should be handled gracefully now.
  * @PluginLoaded added. Pluings may now be notified if other plugins are loaded.
  * fixed shutdown problem, should work now
  * improved plugin loading, spurious activation of static initializers should not happen anymore
  * Delight XMLRPC supported

<br /><br />

## 0.5.0 (2009/??) ##

  * Service discovery works. We can now find remote plugins by specifying discover://any or discover://nearest.

> <br /><br />

## 0.4.0 (2009/05/07) ##

  * Okay, the DWR plugin commited last time works, but browsers seem to have problems with it due to cross domain scripting regulations. Maybe this "heals" itself the next couple month (dwr might have to be updated)
  * Introduced JSON exporter. Advantage: Fast, and works better with browsers (still  have to tweak some security settings ('UniversalBrowserRead'). Disadvantage: Doesn't handle complex objects well.
  * Changed URLs to URIs. Java URLs are a pain in the ass. URIs work better and don't check the protocol. Im am very sorry, but this breaks code, you have to change all occurrences from URL to URI. Also, loading files from classpath changed. It's not [file://#classpath](file://#classpath) anymore,  it's classpath:// now.
  * Introduced options and changed getPlugins(). This also breaks code, but should help to increase further interface stability a lot. It is planned that complex functions now accept multiple options as varargs, based on the Python principle: simple things stay simple, complex things still possible.


<br /><br />
## 0.3.4 (2009/05/05) ##

  * Plugins may now be exported to JavaScript (using DWR, thanks to Thomas Lottermann)
  * Various small fixes.


<br /><br />
## 0.3.3 (2009/??) ##

  * Changed the InformationBroker interface for even more type safety


<br /><br />
## 0.3.2 (2009/02/25) ##

  * ERMI now supports timeouts
  * Caching now configurable
  * Added shutdown hook
  * Added a information broker
  * @Init supports return type to signal plugin loading failed


<br /><br />
## 0.3.1 (2009/01/27) ##

  * Prepared for shudown hooks
  * Merged with other version
  * Added Essence RMI support (nice stuff guys!)


<br /><br />
## 0.3.0 (2008/06/12) ##

  * Added caching of JARs' contents to speedup loading


<br /><br />
## 0.2.9(2008/05/28) ##

  * Extended facade to allow easy loading by capabilities.


<br /><br />
## 0.2.8 (2009/01/20) ##

  * Now supports loading all files from the local classpath (which is great), so let's hope it works on all platforms ...
  * Also supports loading individual plugins from the classpath (use with care)


<br /><br />
## 0.2.7 (2008/05/23) ##

  * Even more bugfixes for plugin loading.


<br /><br />
## 0.2.6 (2008/05/20) ##

  * Changed plugin loading code. Mixing JARs and classpaths should work fine now


<br /><br />
## 0.2.5 (2008/05/20) ##

  * Cleared up remote plugin interface structure.


<br /><br />
## 0.2.4 (2008/05/20) ##

  * Fixed plugin loading deadlock that could occur in complex projects


<br /><br />
## 0.2.3 (2008/05/20) ##

  * Tons of bugfixes
  * XMLRPC Testcase works again
  * Preliminarily changed build system to ant so that I (Ralf)  can continue working on my own projects until maven works as it should


<br /><br />
## 0.2.2 (2008/05/18) ##

  * Switched build structure to maven (thank Nicolas Delsaux)
  * Intruduced faceades for the core plugins
  * Changed behavior of XMLRPC plugin
  * Added different PublishMethod enums so implementation of these services can be done without changing the library itself.


<br /><br />
## 0.2.1 (2008/05/17) ##

  * Fixed MacOS (and probably Linux) showstopper that prevented one from adding plugins on these platforms. Now verified that the system works on MacOS 1== 0.5 with Java 1.5.


<br /><br />
## 0.2 (2008/05/17) ##

  * Loading a single JAR was not possible due to a misplaced return statement
  * Testcases created
  * Parameter checking introduced
  * Thread-safety to core plugins added
  * Changelog created
  * XMLRPC package moved
  * Joined all external JARs into one single JAR for beautification
  * Added (but not yet implemented) == method to unexport plugins over network
  * Prevented the XMLRPC server from starting automatically -> reduced startup time, no need for System.exit() == as long as you don't export plugins.


<br /><br />
## 0.1 (2008/05/14) ##

  * Initial release