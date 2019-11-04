package io.github.incplusplus.peerprocessing.common;

import java.util.List;

import static io.github.incplusplus.peerprocessing.common.StupidSimpleLogger.printStackTrace;
import static java.util.Arrays.asList;

/**
 * A marker interface that indicates that a certain
 * enum is a header that might be sent in a message between
 * a client, server, or slave.
 */
public interface Header {
	List<Class<? extends Enum>> members = asList(Demands.class, ClientType.class, Responses.class, VariousEnums.class);
	
	static Header valueOf(String name) {
		Enum enumValue = null;
		for (Class<? extends Enum> i : members) {
			try {
				enumValue = Enum.valueOf(i, name);
			}
			catch (IllegalArgumentException ignored) {
			}
		}
		if (enumValue == null)
			throw new IllegalArgumentException("No enum constant found with name " + name);
		return (Header) enumValue;
	}
}
