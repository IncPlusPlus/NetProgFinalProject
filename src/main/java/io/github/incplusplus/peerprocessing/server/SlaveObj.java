package io.github.incplusplus.peerprocessing.server;

import io.github.incplusplus.peerprocessing.common.Job;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

public class SlaveObj extends ConnectedEntity {
	public SlaveObj(PrintWriter outToClient, BufferedReader inFromClient, Socket socket,
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
	
	}
	
	public void accept(Job job) {
	
	}
}
