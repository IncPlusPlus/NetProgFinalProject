package io.github.incplusplus.peerprocessing.server;

import io.github.incplusplus.peerprocessing.common.StupidSimpleLogger;
import io.github.incplusplus.peerprocessing.common.ClientType;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;

import static io.github.incplusplus.peerprocessing.common.MiscUtils.getIp;
import static io.github.incplusplus.peerprocessing.common.StupidSimpleLogger.log;

public class Server {
	private static ServerSocket socket;
	private final static int port = 1234;
	private static String serverName = "Processing Server";
	private static final List<ClientObj> clients = Collections.synchronizedList(new ArrayList<>());
	private static final List<SlaveObj> slaves = Collections.synchronizedList(new ArrayList<>());
	/**
	 * Should probably be replaced with {@link Executors#newCachedThreadPool()} at some point.
	 * Elements of this list exist while an incoming connection is established and are removed
	 * after it has been established what {@linkplain ClientType} the incoming connection is.
	 */
	private static final List<ConnectionHandler> connectionHandlers = Collections.synchronizedList(new ArrayList<>());
//	private static final List<ConnectedEntity> connectedEntities = Collections.synchronizedList(new ArrayList<>());
	
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
							ConnectionHandler ch = new ConnectionHandler(socket.accept());
							register(ch);
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
	
	//<editor-fold desc="register() methods">
	/**
	 * Register a {@linkplain ConnectionHandler} within the server.
	 *
	 * @param connectionHandler the ConnectionHandler to register.
	 */
	static void register(ConnectionHandler connectionHandler) {
		connectionHandlers.add(connectionHandler);
	}
	
	/**
	 * Register a {@linkplain ConnectedEntity} within the server.
	 * This keeps track of
	 * @param connectedEntity an entity to keep track of
	 */
	static void register(ConnectedEntity connectedEntity) {
		if(connectedEntity instanceof SlaveObj) {
			slaves.add((SlaveObj) connectedEntity);
		}
		else if (connectedEntity instanceof ClientObj) {
			clients.add((ClientObj) connectedEntity);
		}
		else {
			throw new IllegalArgumentException("Argument 'connectedEntity' must be a SlaveObj or ClientObj.");
		}
	}
	//</editor-fold>
	
	//<editor-fold desc="deRegister() methods">
	/**
	 * Remove the specified {@linkplain ConnectionHandler} from the server's list
	 * of ConnectionHandlers.
	 * @param connectionHandler the ConnectionHandler to remove
	 */
	static void deRegister(ConnectionHandler connectionHandler) {
		connectionHandlers.remove(connectionHandler);
	}
	
	/**
	 * Remove the specified {@linkplain ConnectedEntity} from the server's list of ConnectedEntities.
	 * This can be a {@link io.github.incplusplus.peerprocessing.client.Client}
	 * or a {@link io.github.incplusplus.peerprocessing.slave.Slave}
	 * @param connectedEntity the entity to remove
	 */
	static void deRegister(ConnectedEntity connectedEntity) {
		if(connectedEntity instanceof SlaveObj) {
			slaves.remove((SlaveObj) connectedEntity);
		}
		else if (connectedEntity instanceof ClientObj) {
			clients.remove((ClientObj) connectedEntity);
		}
		else {
			throw new IllegalArgumentException("Argument 'connectedEntity' must be a SlaveObj or ClientObj.");
		}
	}
	//</editor-fold>
}
