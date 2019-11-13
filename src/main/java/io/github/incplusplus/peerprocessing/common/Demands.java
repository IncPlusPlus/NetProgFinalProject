package io.github.incplusplus.peerprocessing.common;

public enum Demands implements Header {
	/**
	 * Sent from the server indicating the
	 * client should identify itself as one
	 * of the known {@link MemberType}s
	 */
	IDENTIFY,
	/**
	 * Submit a query either from a client to the server
	 * or from the server to a slave.
	 */
	QUERY,
	/**
	 * Sent from the server. Asks client for their name
	 */
	@Deprecated
	PROVIDE_CLIENT_NAME
}
