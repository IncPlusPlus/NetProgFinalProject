package io.github.incplusplus.peerprocessing.server;

import io.github.incplusplus.peerprocessing.common.Introduction;
import io.github.incplusplus.peerprocessing.common.MemberType;

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
abstract class ConnectedEntity implements Runnable {
	private final PrintWriter outToClient;
	private final BufferedReader inFromClient;
	private final Socket socket;
	private final UUID connectionUUID;
	
	ConnectedEntity(PrintWriter outToClient, BufferedReader inFromClient, Socket socket,
	                UUID connectionUUID) {
		this.outToClient = outToClient;
		this.inFromClient = inFromClient;
		this.socket = socket;
		this.connectionUUID = connectionUUID;
	}
	
	PrintWriter getOutToClient() {
		return outToClient;
	}
	
	BufferedReader getInFromClient() {
		return inFromClient;
	}
	
	Socket getSocket() {
		return socket;
	}
	
	UUID getConnectionUUID() {
		return connectionUUID;
	}
	
	Introduction provideIntroductionFromServer() {
		Introduction introduction = new Introduction();
		introduction.setSenderId(this.getServerId());
		introduction.setReceiverId(getConnectionUUID());
		introduction.setSenderName(Server.serverName);
		introduction.setSenderType(MemberType.SERVER);
		return introduction;
	}
	
	abstract UUID getServerId();
	
	void disconnect() throws IOException {
		getOutToClient().println(DISCONNECT);
		kill();
	}
	
	void kill() throws IOException {
		getOutToClient().close();
		getInFromClient().close();
		getSocket().close();
	}
}
