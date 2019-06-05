package com.sshtools.common.ssh;

/**
 * <p>An abstract forwarding channel implementation for use with both local
 * and remote forwarding operations.</p>
 *
 * A forwarding channel acts as a tunnel, connections are listened for at the tunnel start point and any data is forwarded from the start point through the ssh connection and then onto the end point.
 * 
 *  Local forwards have the tunnel start point on the client, and the data flows from the start point through the client, along the ssh connection to the server, out to the endpoint which can be anywhere.
 *  Remote forwards have the tunnel start point on the Server, and the data flows from the start point through the server, along the ssh connection to the client, out to the endpoint which can be anywhere.
 *  
 * 
 */
public abstract class ForwardingChannel<T extends SshContext>
    extends ChannelNG<T> {

	/**Tunnel endpoint hostname*/
    protected String hostToConnect;
    /**Tunnel endpoint port number*/
    protected int portToConnect;
    /**Tunnel startpoint hostname*/
    protected String originatingHost;
    /**Tunnel startpoint port number*/
    protected int originatingPort;

    /**
     * Construct the forwarding channel.
     * @param channelType String
     * @param maximumPacket int
     * @param windowSize int
     * @see com.sshtools.common.ssh.ChannelNG#Channel(String channelType, int maximumPacketSize, int initialWindowSize)
     */
    public ForwardingChannel(String channelType, SshConnection con, int maximumPacketSize, int initialWindowSize, int maximumWindowSpace, int minimumWindowSpace) {
        super(channelType, con, maximumPacketSize, initialWindowSize, maximumWindowSpace, minimumWindowSpace);
    }

    /**
     * The hostname of the endpoint of tunnel.
     * @return String
     */
    public String getHost() {
        return hostToConnect;
    }

    /**
     * The port number of the endpoint of tunnel.
     * @return int
     */
    public int getPort() {
        return portToConnect;
    }

    /**
     * The hostname of the startpoint of tunnel.
     * @return String
     */
    public String getOriginatingHost() {
        return originatingHost;
    }

    /**
     * The port number of the startpoint of tunnel.
     * @return int
     */
    public int getOriginatingPort() {
        return originatingPort;
    }
    
    protected boolean checkWindowSpace() {
    	throw new UnsupportedOperationException();
    }
}
