package net.xeoh.plugins.remotediscovery.impl.v3;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * 
 * @author rb
 * 
 */
final class RemoteManagerEndpoint implements Serializable {
    private static final long serialVersionUID = -7370418870963776624L;

    public InetAddress address;
    public int port;

    /**
     * @param adr
     * @param port
     */
    public RemoteManagerEndpoint(InetAddress adr, int port) {
        this.address = adr;
        this.port = port;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.address.hashCode() ^ this.port;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof RemoteManagerEndpoint)) return false;
        RemoteManagerEndpoint rme = (RemoteManagerEndpoint) o;

        return this.address == rme.address && this.port == rme.port;
    }
}