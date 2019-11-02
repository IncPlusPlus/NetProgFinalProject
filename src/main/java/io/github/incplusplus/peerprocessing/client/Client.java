package io.github.incplusplus.peerprocessing.client;

import io.github.incplusplus.peerprocessing.common.Personable;
import io.github.incplusplus.peerprocessing.slave.Slave;
import org.javatuples.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static io.github.incplusplus.peerprocessing.common.MiscUtils.promptForHostPortTuple;
import static io.github.incplusplus.peerprocessing.common.StupidSimpleLogger.enable;

public class Client implements Personable {
	private final String serverHostname;
	private final int serverPort;
	private Socket sock;
	private PrintWriter outToServer;
	private BufferedReader inFromServer;
	
	public static void main(String[] args) throws IOException {
		enable();
		Pair<String, Integer> hostAndPortPair = promptForHostPortTuple();
		Client mainClient = new Client(hostAndPortPair.getValue0(), hostAndPortPair.getValue1());
		mainClient.init();
	}
	
	public Client(String serverHostname, int serverPort) {
		this.serverHostname = serverHostname;
		this.serverPort = serverPort;
	}
	
	void init() throws IOException {
		this.sock = new Socket(serverHostname, serverPort);
		this.outToServer = new PrintWriter(sock.getOutputStream());
		this.inFromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
	}
	
	/**
	 * Introduces this {@linkplain Personable} object to a server.
	 */
	@Override
	public void introduce() {
	
	}
}
