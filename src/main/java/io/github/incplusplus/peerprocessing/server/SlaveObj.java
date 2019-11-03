package io.github.incplusplus.peerprocessing.server;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

public class SlaveObj extends ConnectedEntity {
	public SlaveObj(PrintWriter outToClient, BufferedReader inToClient, Socket socket,
	                UUID connectionUUID) {super(outToClient, inToClient, socket, connectionUUID);}
}
