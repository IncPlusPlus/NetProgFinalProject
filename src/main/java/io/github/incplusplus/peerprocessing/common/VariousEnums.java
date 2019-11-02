package io.github.incplusplus.peerprocessing.common;

/**
 * This enum contains values that are processed by the server
 * and/or client
 */
public enum VariousEnums {
	/**
	 * Indicates to the server that the new connection it has
	 * just received is for a headless-style client where
	 * the client can send only in one console and passively receive
	 * in the other. This message would be sent by the writable console.
	 */
	CONNECT_HEADLESS_CLIENT_SENDER,
	/**
	 * Same as {@link #CONNECT_HEADLESS_CLIENT_SENDER} but
	 * this message would come from the reading-only console.
	 */
	CONNECT_HEADLESS_CLIENT_RECEIVER,
	/**
	 * Asks the server to register this new client.
	 * This command will only be accepted while this
	 * client has not yet registered itself.
	 */
	REGISTER_NEW_CLIENT,
	/**
	 * This is sent by the server to tell the client
	 * its name.
	 */
	SERVER_NAME,
	/**
	 * Sent from the server indicating the client
	 * should provide a registration key.
	 */
	PROVIDE_REG_KEY,
	/**
	 * Sent from client to attach
	 * receiving console window.
	 */
	REG_KEY,
	/**
	 * This registration key does not
	 * exist on the server.
	 */
	REG_KEY_REJECTED,
	/**
	 * Whatever was just sent is fine.
	 * Continue with registration flow.
	 */
	CONTINUE,
	/**
	 * This tells the server to kill off the
	 * client connections.
	 */
	DISCONNECT,
	/**
	 * Indicates that this is to be treated as a
	 * regular chat message from a client.
	 */
	MESSAGE;
}
