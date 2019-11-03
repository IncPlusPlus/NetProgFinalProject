package io.github.incplusplus.peerprocessing.server;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

public class ClientObj extends ConnectedEntity {
	public ClientObj(PrintWriter outToClient, BufferedReader inToClient, Socket socket,
	                 UUID connectionUUID) {super(outToClient, inToClient, socket, connectionUUID);}
}
