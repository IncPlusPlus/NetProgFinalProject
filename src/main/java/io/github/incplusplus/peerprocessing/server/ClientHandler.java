package io.github.incplusplus.peerprocessing.server;

import io.github.incplusplus.peerprocessing.common.ClientType;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

import static io.github.incplusplus.peerprocessing.server.ConnectionState.CONNECTING;

public class ClientHandler implements Runnable {
	private Socket socket;
	private volatile ConnectionState connectionState;
	private UUID connectionUUID;
	public ClientHandler(Socket incomingConnection) {
		this.connectionUUID = UUID.randomUUID();
		this.connectionState = CONNECTING;
		this.socket = incomingConnection;
	}
	
	public void run() {
	
	}
}
