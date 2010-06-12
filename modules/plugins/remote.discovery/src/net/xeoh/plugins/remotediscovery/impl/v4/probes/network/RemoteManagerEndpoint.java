package net.xeoh.plugins.remotediscovery.impl.v4.probes.network;

import java.net.InetAddress;

/**
 * 
 * @author rb
 *
 */
public class RemoteManagerEndpoint {
    /** */
    public InetAddress address;
    
    /** */
    public int port;

    /**
     * @param adr
     * @param port
     */
    public RemoteManagerEndpoint(InetAddress adr, int port) {
        this.address = adr;
        this.port = port;
    }

    /**
     * 
     */
    public RemoteManagerEndpoint() {
        // TODO Auto-generated constructor stub
    }
}