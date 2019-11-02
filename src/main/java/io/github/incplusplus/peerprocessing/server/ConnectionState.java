package io.github.incplusplus.peerprocessing.server;

/**
 * Represents the current state of the connection from
 * a client (or slave).
 */
public enum ConnectionState {
	/**
	 * Nothing has happened yet except an attempt
	 * at a connection.
	 */
	CONNECTING,
	/**
	 * The client has taken no action yet besides
	 * initiating a connection.
	 */
	CONNECTED,
	/**
	 * The client has registered their name.
	 */
	REGISTERED,
	/**
	 * The client has connected their listener
	 * and has begun to listen. At this point,
	 * they're all set up to participate.
	 */
	LISTENING,
	/**
	 * Something happened to this connection.
	 * It's no longer valid.
	 */
	INVALID,
	/**
	 * They're gone!
	 */
	DISCONNECTED;
}
