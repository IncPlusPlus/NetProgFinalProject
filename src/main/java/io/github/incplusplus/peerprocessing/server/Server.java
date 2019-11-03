package io.github.incplusplus.peerprocessing.server;

import io.github.incplusplus.peerprocessing.common.Job;
import io.github.incplusplus.peerprocessing.common.StupidSimpleLogger;
import io.github.incplusplus.peerprocessing.common.ClientType;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import static io.github.incplusplus.peerprocessing.common.MiscUtils.getIp;
import static io.github.incplusplus.peerprocessing.common.StupidSimpleLogger.log;

public class Server {
	private static ServerSocket socket;
	private final static int port = 1234;
	private static String serverName = "Processing Server";
	private static final Map<UUID, ClientObj> clients = new ConcurrentHashMap<>();
	private static final Map<UUID, SlaveObj> slaves = new ConcurrentHashMap<>();
	/**
	 * Should probably be replaced with {@link Executors#newCachedThreadPool()} at some point.
	 * Elements of this list exist while an incoming connection is established and are removed
	 * after it has been established what {@linkplain ClientType} the incoming connection is.
	 */
	private static final List<ConnectionHandler> connectionHandlers = Collections.synchronizedList(new ArrayList<>());
	private static final ConcurrentHashMap<UUID, Job> jobs = new ConcurrentHashMap<>();
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
	 * This keeps track of the connected clients and slaves.
	 *
	 * @param connectedEntity an entity to keep track of
	 */
	static void register(ConnectedEntity connectedEntity) {
		if (connectedEntity instanceof SlaveObj) {
			SlaveObj shouldBeNull = null;
			//Put this slave in the slaves map
			shouldBeNull = slaves.put(connectedEntity.getConnectionUUID(), (SlaveObj) connectedEntity);
			//ConcurrentHashMap does not support null entries. Because of this, the put() method
			//only returns null if there was no previous mapping. We want to assure there was no
			//duplicate mapping before this put() operation.
			assert shouldBeNull == null;
		}
		else if (connectedEntity instanceof ClientObj) {
			ClientObj shouldBeNull = null;
			//Put this slave in the slaves map
			shouldBeNull = clients.put(connectedEntity.getConnectionUUID(), (ClientObj) connectedEntity);
			//ConcurrentHashMap does not support null entries. Because of this, the put() method
			//only returns null if there was no previous mapping. We want to assure there was no
			//duplicate mapping before this put() operation.
			assert shouldBeNull == null;
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
	 *
	 * @param connectionHandler the ConnectionHandler to remove
	 */
	static void deRegister(ConnectionHandler connectionHandler) {
		connectionHandlers.remove(connectionHandler);
	}
	
	/**
	 * Remove the specified {@linkplain ConnectedEntity} from the server's list of ConnectedEntities.
	 * This can be a {@link io.github.incplusplus.peerprocessing.client.Client}
	 * or a {@link io.github.incplusplus.peerprocessing.slave.Slave}
	 *
	 * @param connectedEntity the entity to remove
	 */
	static void deRegister(ConnectedEntity connectedEntity) {
		if (connectedEntity instanceof SlaveObj) {
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
	
	static void submitJob(Job job) {
		Job shouldBeNull = null;
		//Put this slave in the slaves map
		shouldBeNull = jobs.put(job.getJobId(), job);
		//ConcurrentHashMap does not support null entries. Because of this, the put() method
		//only returns null if there was no previous mapping. We want to assure there was no
		//duplicate mapping before this put() operation.
		assert shouldBeNull == null;
		sendToLeastBusySlave(job);
	}
	
	private static void sendToLeastBusySlave(Job job) {
		/*
		 * TODO: Determining the business of slaves is not yet implemented as the
		 *  heartbeat system has not yet been implemented. For now this method
		 *  just sends the job to a random slave.
		 */
	}
}
