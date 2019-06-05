
package com.sshtools.common.ssh;

import java.io.IOException;

/**
 * A service is a protocol that operates on top of the {@link TransportProtocol}.
 */
public interface Service {

    /**
     * Process a transport message. When a message is received by the
     * {@link TransportProtocol} that is not a transport level message the
     * message is passed onto the active service using this method. The service
     * processes the message and returns a value to indicate whether the message
     * was used.
     * 
     * @param msg
     * @return <tt>true</tt> if the message was processed, otherwise
     *         <tt>false</tt>
     * @throws IOException
     */
    public boolean processMessage(byte[] msg) throws IOException;

    /**
     * Start the service.
     */
    public void start();

    /**
     * Stop the service
     */
    public void stop();

    /**
     * How long does the service allow idle for?
     * @return
     */
	public int getIdleTimeoutSeconds();

	/**
	 * The service name
	 * @return
	 */
	public String getName();

	/**
	 * The service has reached idle timeout seconds
	 * @return 
	 */
	public boolean idle();
}
