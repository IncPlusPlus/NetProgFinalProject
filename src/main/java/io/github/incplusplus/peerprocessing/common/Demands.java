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
	 * Sent from client to server and from server to slave
	 * to tell the destination machine to
	 * solve a particular query.
	 * @apiNote THIS IS ONLY FOR SENDING things related to
	 * the {@link MathQuery} class. It is for sending a raw query string
	 * from a client to a server and sending a {@link MathQuery} class
	 * from a server to a slave. The only acceptable response is
	 * {@link Responses#SOLUTION}.
	 * @deprecated in favor of {@link #QUERY}
	 */
	@Deprecated
	SOLVE,
	/**
	 * Sent from the server. Asks client for their name
	 */
	@Deprecated
	PROVIDE_CLIENT_NAME
}
