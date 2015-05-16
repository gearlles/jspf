## v1.0 ##

  * **Check System.loadLibrary() works well**
> Especially check that it works with different plugins loading the same library. Find and document work-arounds for the problems that might arise.


## Unsorted ##


  * **Add Discovery caching**
> Cache discovered network items for multiple clients / startups. We need this in order to reduce the Discovery startup time from a seconds to milliseconds.

  * **Add Discovery localhost tricks**
> Try to improve the discovery of localhost services by using non-network facilities (files / handles / ...)

  * **How to improve or enable hot-swapping of plugins.**
> While in some cases it isn't possible, there might be some cases in which plugins are only loosely coupled (e.g., stateless plugins). How can we make them hot-swappable?


  * **Finish Plugin Doctor**
> A tool to create single-JAR plugins from given projects and to inspect plugins.


