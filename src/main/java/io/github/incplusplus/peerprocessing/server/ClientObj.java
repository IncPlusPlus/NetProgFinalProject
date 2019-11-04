package io.github.incplusplus.peerprocessing.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.incplusplus.peerprocessing.common.Header;
import io.github.incplusplus.peerprocessing.common.Job;
import io.github.incplusplus.peerprocessing.common.MathQuery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;

import static io.github.incplusplus.peerprocessing.common.Constants.SHARED_MAPPER;
import static io.github.incplusplus.peerprocessing.common.Demands.SOLVE;
import static io.github.incplusplus.peerprocessing.common.MiscUtils.*;
import static io.github.incplusplus.peerprocessing.common.Responses.SOLUTION;
import static io.github.incplusplus.peerprocessing.common.StupidSimpleLogger.printStackTrace;
import static io.github.incplusplus.peerprocessing.server.Server.deRegister;

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
			catch (SocketException e) {
				deRegister(this);
				try {
					getSocket().close();
				}
				catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			catch (IOException e) {
				printStackTrace(e);
			}
			finally {
			
			}
		}
	}
	
	void acceptCompleted(MathQuery mathQuery) throws JsonProcessingException {
		getOutToClient().println(msg(SHARED_MAPPER.writeValueAsString(mathQuery), SOLUTION));
	}
	
	private void offload(String mathQuery) {
		Server.submitJob(
				new Job(new MathQuery(mathQuery), getConnectionUUID())
		);
	}
}
