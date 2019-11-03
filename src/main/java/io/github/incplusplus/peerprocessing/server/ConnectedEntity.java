package io.github.incplusplus.peerprocessing.server;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

/**
 * Represents a type of object that can
 * connect to a server.
 */
public abstract class ConnectedEntity implements Runnable {
	private final PrintWriter outToClient;
	private final BufferedReader inToClient;
	private final Socket socket;
	private final UUID connectionUUID;
	private volatile ConnectionState connectionState;
	
	public ConnectedEntity(PrintWriter outToClient, BufferedReader inToClient, Socket socket,
	                       UUID connectionUUID) {
		this.outToClient = outToClient;
		this.inToClient = inToClient;
		this.socket = socket;
		this.connectionUUID = connectionUUID;
	}
	
	public PrintWriter getOutToClient() {
		return outToClient;
	}
	
	public BufferedReader getInToClient() {
		return inToClient;
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	public UUID getConnectionUUID() {
		return connectionUUID;
	}
	
	public ConnectionState getConnectionState() {
		return connectionState;
	}
}
