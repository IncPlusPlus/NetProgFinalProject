package io.github.incplusplus.peerprocessing.common;

import java.io.Closeable;
import java.io.IOException;
import java.util.UUID;

public interface ProperClient extends Closeable {
	
	/**
	 * Begin reading or writing as expected.
	 */
	void begin() throws IOException;
	
	/**
	 * @return whether or not this client is still connected
	 * to a server and running.
	 */
	boolean isClosed();
	
	/**
	 * @return whether or not this client has introduced itself
	 * or not.
	 */
	boolean isPolite();
	
	/**
	 * @param verbose whether to enable logging within this client
	 */
	void setVerbose(boolean verbose);
	
	/**
	 * @return a UUID representing this clients connection to the server
	 */
	UUID getConnectionId();
	
	/**
	 * Disconnect from the server. Calls {@link ProperClient#close()}
	 *
	 * @throws IOException if an I/O error occurs
	 */
	default void disconnect() throws IOException {
		close();
	}

	String getDestinationHostname();

	int getDestinationPort();
}
