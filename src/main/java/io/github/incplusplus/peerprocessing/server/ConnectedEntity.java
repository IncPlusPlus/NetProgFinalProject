package io.github.incplusplus.peerprocessing.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

import static io.github.incplusplus.peerprocessing.common.VariousEnums.DISCONNECT;

/**
 * Represents a type of object that can
 * connect to a server.
 */
public abstract class ConnectedEntity implements Runnable {
	private final PrintWriter outToClient;
	private final BufferedReader inFromClient;
	private final Socket socket;
	private final UUID connectionUUID;
	private volatile ConnectionState connectionState;
	
	public ConnectedEntity(PrintWriter outToClient, BufferedReader inFromClient, Socket socket,
	                       UUID connectionUUID) {
		this.outToClient = outToClient;
		this.inFromClient = inFromClient;
		this.socket = socket;
		this.connectionUUID = connectionUUID;
	}
	
	public PrintWriter getOutToClient() {
		return outToClient;
	}
	
	public BufferedReader getInFromClient() {
		return inFromClient;
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
	
	public void disconnect() throws IOException {
		getOutToClient().println(DISCONNECT);
		getOutToClient().close();
		getInFromClient().close();
		getSocket().close();
	}
}
