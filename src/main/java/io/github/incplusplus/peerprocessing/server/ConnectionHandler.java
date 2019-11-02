package io.github.incplusplus.peerprocessing.server;

import io.github.incplusplus.peerprocessing.common.ClientType;
import io.github.incplusplus.peerprocessing.common.Introduction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

import static io.github.incplusplus.peerprocessing.common.Constants.SHARED_MAPPER;
import static io.github.incplusplus.peerprocessing.common.Demands.IDENTIFY;
import static io.github.incplusplus.peerprocessing.common.Responses.IDENTITY;
import static io.github.incplusplus.peerprocessing.server.ConnectionState.CONNECTING;
import static io.github.incplusplus.peerprocessing.server.ServerMethods.negotiate;

public class ConnectionHandler implements Runnable {
	private final PrintWriter outToClient;
	private final BufferedReader inToClient;
	private Socket socket;
	private volatile ConnectionState connectionState;
	private UUID connectionUUID;
	
	public ConnectionHandler(Socket incomingConnection) throws IOException {
		this.connectionUUID = UUID.randomUUID();
		this.connectionState = CONNECTING;
		this.socket = incomingConnection;
		this.inToClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.outToClient = new PrintWriter(socket.getOutputStream(), true);
	}
	
	public void run() {
		try {
			Introduction clientIntroduction = SHARED_MAPPER.readValue(
					negotiate(IDENTIFY, IDENTITY, outToClient, inToClient), Introduction.class);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
