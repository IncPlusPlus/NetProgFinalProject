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
import static io.github.incplusplus.peerprocessing.common.StupidSimpleLogger.log;
import static io.github.incplusplus.peerprocessing.server.ConnectionState.CONNECTING;
import static io.github.incplusplus.peerprocessing.server.ConnectionState.INVALID;
import static io.github.incplusplus.peerprocessing.server.Server.deRegister;
import static io.github.incplusplus.peerprocessing.server.Server.register;
import static io.github.incplusplus.peerprocessing.server.ServerMethods.negotiate;

public class ConnectionHandler implements Runnable {
	private volatile PrintWriter outToClient;
	private volatile BufferedReader inFromClient;
	private Socket socket;
	private UUID connectionUUID;
	private volatile ConnectionState connectionState;
	
	public ConnectionHandler(Socket incomingConnection) throws IOException {
		log("New connection at " + incomingConnection.getLocalAddress() + ":" + incomingConnection.getLocalPort());
		this.connectionUUID = UUID.randomUUID();
		this.connectionState = CONNECTING;
		this.socket = incomingConnection;
		this.inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.outToClient = new PrintWriter(socket.getOutputStream(), true);
	}
	
	public void run() {
		try {
			Introduction clientIntroduction = SHARED_MAPPER.readValue(
					negotiate(IDENTIFY, IDENTITY, outToClient, inFromClient), Introduction.class);
			if (clientIntroduction.getType().equals(ClientType.CLIENT)) {
				ClientObj client = new ClientObj(outToClient, inFromClient, socket, connectionUUID);
				Thread clientThread = new Thread(client);
				clientThread.setDaemon(true);
				clientThread.start();
				log("Registering new client");
				register(client);
			}
			else if (clientIntroduction.getType().equals(ClientType.SLAVE)) {
				SlaveObj slave = new SlaveObj(outToClient, inFromClient, socket, connectionUUID);
				Thread clientThread = new Thread(slave);
				clientThread.setDaemon(true);
				clientThread.start();
				log("Registering new slave");
				register(slave);
			}
			this.connectionState = INVALID;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			//Remove this ConnectionHandler from the connectionHandlers list
			deRegister(this);
		}
	}
}
