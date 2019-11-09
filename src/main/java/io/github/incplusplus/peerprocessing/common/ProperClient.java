package io.github.incplusplus.peerprocessing.common;

import java.io.Closeable;
import java.io.IOException;

public interface ProperClient extends Closeable {
	/**
	 * Initialize any connections, readers, and writers necessary.
	 * No exception will be thrown here. It is the user's responsibility
	 * to ensure the boolean returned by this method call is true.
	 * @return whether or not initialization was successful.
	 */
	boolean init();
	
	/**
	 * Begin reading or writing as expected.
	 */
	void begin();
	
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
	 *
	 * @param verbose whether to enable logging within this client
	 */
	void setVerbose(boolean verbose);
	
	/**
	 * Disconnect from the server. Calls {@link ProperClient#close()}
	 *
	 * @throws IOException if an I/O error occurs
	 */
	default void disconnect() throws IOException {
		close();
	}
}
