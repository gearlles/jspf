/*
 * RemoteAPI.java
 *
 * Copyright (c) 2008, Ralf Biedert All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. Redistributions in binary form must reproduce the
 * above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * Neither the name of the author nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package net.xeoh.plugins.remote;

import java.net.URI;

import net.xeoh.plugins.base.Plugin;

/**
 * Allows the network export and import of plugins. The plugins will usually be made 
 * available on the same machine and the local network. The currently preferred way 
 * to export and import plugins is using the LipeRMI implementation, as it allows
 * for listeners and transparent network callbacks.<br/><br/>
 *
 * Please note there may be constraints on the plugin usage depending on the remote type.
 * For example, XMLRPC might have problems with null or void types. Plugins implementing 
 * the RemoteAPI should be sensitive to the following configuration subkeys:<br/><br/>
 * 
 * 'export.server'  -   When exporting and constructing the URL, use this server port and IP.
 *
 * @author Ralf Biedert
 */
public interface RemoteAPI extends Plugin {
    /**
     * Exports a plugin over the network. The implementation decides how to do that. An 
     * export result is returned containing additional information about the import. Please 
     * keep in mind that the simpler the plugin's interface is, the more likely the export
     * will succeed (depends on the export method; Lipe can handle pretty much, XMLRPC 
     * doesn't).<br/><br/>
     * 
     * For example, if <code>plugin</code> is a local plugin and <code>remote</code> 
     * a remote plugin, you could export your local plugin like this:
     * <br/><br/>
     * 
     * <code>
     * exportPlugin(plugin);
     * </code><br/><br/>
     * 
     * Your plugin would afterwards be accessible from other VMs on the same machine and the
     * local network. Again, you should keep in mind that some details might differ, depending
     * on the export method you select, so don't be surprised if a specific method call
     * fails (as a rule of thumb, the more complex a method-signature looks like, the less
     * likely it is to work with all exporters). 
     *
     * @param plugin The plugin to export. 
     * @return The export result 
     */
    public ExportResult exportPlugin(Plugin plugin);

    /**
     * The method by which this remoteAPI publishes plugins with <code>exportPlugin()</code>.
     * Can be used to select the right export method. 
     *
     * @return The method the plugin uses to export.
     */
    public PublishMethod getPublishMethod();

    /**
     * Returns a proxy for the remotely exported object. This is the complement to 
     * <code>exportPlugin()</code>. A pseudo-plugin will be return implementing all 
     * methods of the requested interface that forwards all calls to the originally
     * exported plugin. Depending on the export type a number of method 
     * signatures might not work.<br/><br/>
     * 
     * For example, if <code>url</code> is a url contianed in the {@link ExportResult}
     * object returned by <code>exportPlugin()</code> and <code>remote</code> 
     * a remote plugin of the same type as the plugin (which might be of type 
     * <code>ChatService</code> was exported with, you could import your distant plugin 
     * like this:
     * <br/><br/>
     * 
     * <code>
     * ChatService service = getRemoteProxy(uri, ChatService.class);
     * </code><br/><br/>
     * 
     * For your convenince there also exist a number of special URIs, the so called 
     * <i>discovery URIs</i>. They enable you to detect services on the network without
     * having any prior knowledge about its location. These are:
     * 
     * <ul>
     * <li><code>discover://any</code> - discovers the next best service implementing the given interface</li>
     * <li><code>discover://nearest</code> - the nearest (in terms of network ping) instance</li>
     * <li><code>discover://youngest</code> - the most recently started instance</li>
     * <li><code>discover://oldest</code> - the instance running the longest time</li>
     * </ul>
     * 
     * These special URIs can dramatically change the time the method takes. For example, <code>any</code> 
     * and <code>nearest</code> should generally be the fastest in case a local service (on the same
     * machine) is found, while <code>youngest</code> and <code>oldest</code> always consider all
     * available services and take longer. Also, within the first five seconds of application lifetime 
     * the discovery passes can take up to five seconds, while the remaining passes or any discovery call
     * after five seconds usually take only a fraction of a second.
     *
     * @param <R>
     * @param uri The URI to discover. See {@link ExportResult} and the description above.
     * @param remote The Plugin interface to request. 
     * @return A pseudo-plugin serving as a stub to the remote object.
     */
    public <R extends Plugin> R getRemoteProxy(URI uri, Class<R> remote);

    /**
     * Stops the export of a plugin. 
     *
     * @param plugin The plugin to unexport (must have been exported previously by the same 
     * remote service)
     */
    public void unexportPlugin(Plugin plugin);
}
