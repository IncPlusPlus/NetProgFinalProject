package io.github.incplusplus.peerprocessing.server;

import io.github.incplusplus.peerprocessing.common.StupidSimpleLogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static io.github.incplusplus.peerprocessing.common.MiscUtils.getIp;
import static io.github.incplusplus.peerprocessing.common.StupidSimpleLogger.log;

public class Server {
	private static ServerSocket socket;
	private final static int port = 1234;
	private static String serverName = "Processing Server";
	private static final List<ClientObj> clients = Collections.synchronizedList(new ArrayList<>());
	private static final List<SlaveObj> slaves = Collections.synchronizedList(new ArrayList<>());
	private static final List<ClientHandler> connections = Collections.synchronizedList(new ArrayList<>());
	
	public static void main(String[] args) throws IOException {
		Scanner in = new Scanner(System.in);
		//Set up my custom logging implementation
		StupidSimpleLogger.enable();
		if (serverName != null)
			System.out.println("Server name: " + serverName);
		socket = new ServerSocket(port);
		start(port);
		System.out.println("Server started on " + getIp() + ":" + port + ".");
		System.out.println("Hit enter to stop the server.");
		/*
		 * Wait for newline from user.
		 * This call will block the main thread
		 * until the user hits enter in the console.
		 * This is because the server runs on a daemon thread.
		 * This feels like a cleaner way than having a while(true){}
		 * on the main thread.
		 */
		in.nextLine();
		System.out.println("Server stopped.");
	}
	
	public static void start(int serverPort) {
		class ServerStartTask implements Runnable {
			int port;
			
			ServerStartTask(int p) {port = p;}
			
			public void run() {
				try {
					System.out.println("Ready and waiting!");
					while (true) {
						try {
							ClientHandler ch = new ClientHandler(socket.accept());
							connections.add(ch);
							Thread handlerThread = new Thread(ch);
							handlerThread.setDaemon(true);
							handlerThread.start();
						}
						catch (IOException e) {
							e.printStackTrace();
							log("FATAL ERROR. THE CLIENT HANDLER ENCOUNTERED AN ERROR DURING CLOSING TIME");
						}
					}
				}
				finally {
					log("Server shutting down!");
				}
			}
		}
		Thread t = new Thread(new ServerStartTask(port));
		t.setDaemon(true);
		t.start();
	}
}
