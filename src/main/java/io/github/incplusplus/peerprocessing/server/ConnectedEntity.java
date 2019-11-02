package io.github.incplusplus.peerprocessing.server;

import java.util.UUID;

/**
 * Represents a type of object that can
 * connect to a server.
 */
public class ConnectedEntity {
	private UUID id;
	private String name;
	
	public UUID getId() {
		return id;
	}
	
	public void setId(UUID id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
