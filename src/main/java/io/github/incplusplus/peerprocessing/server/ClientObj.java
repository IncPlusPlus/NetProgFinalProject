package io.github.incplusplus.peerprocessing.server;

import io.github.incplusplus.peerprocessing.common.Header;
import io.github.incplusplus.peerprocessing.common.Job;
import io.github.incplusplus.peerprocessing.common.MathQuery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

import static io.github.incplusplus.peerprocessing.common.Demands.SOLVE;
import static io.github.incplusplus.peerprocessing.common.MiscUtils.decode;
import static io.github.incplusplus.peerprocessing.common.MiscUtils.getHeader;

public class ClientObj extends ConnectedEntity {
	public ClientObj(PrintWriter outToClient, BufferedReader inFromClient, Socket socket,
	                 UUID connectionUUID) {super(outToClient, inFromClient, socket, connectionUUID);}
	
	/**
	 * When an object implementing interface <code>Runnable</code> is used
	 * to create a thread, starting the thread causes the object's
	 * <code>run</code> method to be called in that separately executing
	 * thread.
	 * <p>
	 * The general contract of the method <code>run</code> is that it may
	 * take any action whatsoever.
	 *
	 * @see Thread#run()
	 */
	@Override
	public void run() {
		String lineFromClient;
		while (!getSocket().isClosed()) {
			try {
				lineFromClient = getInFromClient().readLine();
				Header header = getHeader(lineFromClient);
				if (header.equals(SOLVE)) {
					offload(decode(lineFromClient));
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			finally {
			
			}
		}
	}
	
	private void offload(String mathQuery) {
		Server.submitJob(
				new Job(new MathQuery(mathQuery), getConnectionUUID())
		);
	}
	
}
