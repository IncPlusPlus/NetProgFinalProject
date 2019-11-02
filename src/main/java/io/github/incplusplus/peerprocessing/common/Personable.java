package io.github.incplusplus.peerprocessing.common;

/**
 * Classes that implement this interface are personable and will
 * introduce themselves when asked by a server.
 */
public interface Personable {
	/**
	 * Introduces this {@linkplain Personable} object to a server.
	 */
	void introduce();
}
