package io.github.incplusplus.peerprocessing.common;

import java.util.UUID;

public class Introduction {
	private UUID id;
	private String name;
	private ClientType type;
	
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
	
	public ClientType getType() {
		return type;
	}
	
	public void setType(ClientType type) {
		this.type = type;
	}
}
