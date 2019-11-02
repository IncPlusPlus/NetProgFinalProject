package io.github.incplusplus.peerprocessing.common;

public enum Demands implements Header {
	/**
	 * Sent from the server indicating the
	 * client should identify itself as one
	 * of the known {@link ClientType}s
	 */
	IDENTIFY,
	/**
	 * Sent from client to server and from server to slave
	 * to tell the destination machine to
	 * solve a particular query.
	 */
	SOLVE,
	/**
	 * Sent from the server. Asks client for their name
	 */
	@Deprecated
	PROVIDE_CLIENT_NAME
}
