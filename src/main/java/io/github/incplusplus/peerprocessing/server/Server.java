package io.github.incplusplus.peerprocessing.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.incplusplus.peerprocessing.common.Query;
import io.github.incplusplus.peerprocessing.common.StupidSimpleLogger;
import io.github.incplusplus.peerprocessing.common.MemberType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.incplusplus.peerprocessing.common.MiscUtils.getIp;
import static io.github.incplusplus.peerprocessing.common.MiscUtils.randInt;
import static io.github.incplusplus.peerprocessing.common.StupidSimpleLogger.*;
import static io.github.incplusplus.peerprocessing.server.QueryState.SENDING_TO_CLIENT;
import static io.github.incplusplus.peerprocessing.server.QueryState.WAITING_ON_SLAVE;

//TODO Make this class less static. Allow server instances.
public class Server {
	private static ServerSocket socket;
	/**
	 * The time to sleep in milliseconds when there are no slaves around to process requests
	 */
	private static long NO_SLAVES_SLEEP_TIME = 1000 * 30;
	private final static int port = 1234;
	//mostly unused
	final static UUID serverId = UUID.randomUUID();
	final static String serverName = "Processing Server";
	private static volatile AtomicBoolean started = new AtomicBoolean(false);
	private static final Map<UUID, ClientObj> clients = new ConcurrentHashMap<>();
	private static final Map<UUID, SlaveObj> slaves = new ConcurrentHashMap<>();
	/**
	 * Should probably be replaced with {@link Executors#newCachedThreadPool()} at some point.
	 * Elements of this list exist while an incoming connection is established and are removed
	 * after it has been established what {@linkplain MemberType} the incoming connection is.
	 */
	private static final List<ConnectionHandler> connectionHandlers = Collections.synchronizedList(new ArrayList<>());
	private static final ConcurrentHashMap<UUID, Query> queries = new ConcurrentHashMap<UUID, Query>();
	/**
	 * A queue that holds the jobs that are waiting to be assigned and sent to a slave.
	 */
	private static final BlockingDeque<Query> jobsAwaitingProcessing = new LinkedBlockingDeque<Query>();
	
	public static void main(String[] args) throws IOException, InterruptedException {
		Scanner in = new Scanner(System.in);
		//Set up my custom logging implementation
		StupidSimpleLogger.enable();
		if (serverName != null)
			info("Server name: " + serverName);
		start(port);
		info("Server started on " + getIp() + ":" + port + ".");
		info("Hit enter to stop the server.");
		/*
		 * Wait for newline from user.
		 * This call will block the main thread
		 * until the user hits enter in the console.
		 * This is because the server runs on a daemon thread.
		 * This feels like a cleaner way than having a while(true){}
		 * on the main thread.
		 */
		in.nextLine();
		stop();
		debug("Server stopped.");
	}
	
	/**
	 * See {@link #start(int)}
	 */
	public static int start(int serverPort, boolean verbose) throws IOException {
		if (verbose)
			StupidSimpleLogger.enable();
		return start(serverPort);
	}
	
	/**
	 * Start a server.
	 *
	 * @param serverPort the port to start the server on.
	 *                   If set to 0, the server will listen on
	 *                   whatever port is available.
	 * @return the port the server started listening on
	 */
	public static int start(int serverPort) throws IOException {
		class ServerStartTask implements Runnable {
			private ServerSocket serverSocket;
			
			ServerStartTask(ServerSocket socket) {
				this.serverSocket = socket;
			}
			
			public void run() {
				try {
					started.compareAndSet(false, true);
					
					startJobIngestionThread();
					while (started.get()) {
						try {
							Socket incomingConnection = serverSocket.accept();
							ConnectionHandler ch = new ConnectionHandler(incomingConnection);
							register(ch);
							Thread handlerThread = new Thread(ch);
							handlerThread.setName(
									"ConnectionHandler thread for " + incomingConnection.getLocalAddress().getHostAddress());
							handlerThread.setDaemon(true);
							handlerThread.start();
						}
						catch (IllegalArgumentException e) {
							error(e.getMessage());
						}
						catch (SocketException e) {
							if (started.get())
								stop();
						}
						catch (IOException e) {
							printStackTrace(e);
							debug("FATAL ERROR. THE CLIENT HANDLER ENCOUNTERED AN ERROR DURING CLOSING TIME");
						}
					}
				}
				catch (IOException e) {
					printStackTrace(e);
				}
			}
		}
		socket = new ServerSocket(serverPort);
		Thread t = new Thread(new ServerStartTask(socket));
		t.setName("Server socket acceptance thread");
		t.start();
		return socket.getLocalPort();
	}
	
	public static void stop() throws IOException {
		started.compareAndSet(true, false);
		debug("Server shutting down.");
		debug("Disconnecting clients.");
		synchronized (clients) {
			for (Map.Entry<UUID, ClientObj> i : clients.entrySet()) {
				i.getValue().disconnect();
				debug("Dropped connection for client " + i.getValue().getConnectionUUID());
			}
		}
		debug("Disconnecting slaves.");
		synchronized (slaves) {
			for (Map.Entry<UUID, SlaveObj> i : slaves.entrySet()) {
				i.getValue().disconnect();
				debug("Dropped connection for slave " + i.getValue().getConnectionUUID());
				for (UUID jobId : i.getValue().getJobsResponsibleFor()) {
					debug("From slave " + i.getValue().getConnectionUUID() + ", dropped job " + jobId.toString());
				}
			}
		}
		socket.close();
	}
	
	public static boolean started() {
		return started.get();
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
			//TODO add responsibility reassignment if the slave held jobs
			slaves.remove(connectedEntity.getConnectionUUID());
		}
		else if (connectedEntity instanceof ClientObj) {
			clients.remove(connectedEntity.getConnectionUUID());
		}
		else {
			throw new IllegalArgumentException("Argument 'connectedEntity' must be a SlaveObj or ClientObj.");
		}
	}
	
	/**
	 * Determine whether or not a client or slave with a certain UUID is connected or not.
	 *
	 * @param uuid the UUID of the slave/client
	 * @return whether or not the server is still connected to the slave or client of the specified UUID
	 */
	static boolean isConnected(UUID uuid) {
		return clients.containsKey(uuid) || slaves.containsKey(uuid);
	}
	
	//</editor-fold>
	
	/**
	 * Add a query to the server. The server will then add this query
	 * to the query queue where it will be removed and processed.
	 * The query will also be added to a map of jobs to determine
	 * the source and destination of jobs received by slaves.
	 *
	 * @param query the query to be processed
	 */
	static void submitJob(Query query) {
		Query shouldBeNull = null;
		//Put this slave in the slaves map
		shouldBeNull = queries.put(query.getQueryId(), query);
		//ConcurrentHashMap does not support null entries. Because of this, the put() method
		//only returns null if there was no previous mapping. We want to assure there was no
		//duplicate mapping before this put() operation.
		assert shouldBeNull == null;
		jobsAwaitingProcessing.add(query);
	}
	
	/**
	 * Remove a query from the jobs list. At this point,
	 * the query has just been processed by a slave.
	 * It's internal status will still be {@link QueryState#WAITING_ON_SLAVE}
	 *
	 * @param queryId the id of the query to remove
	 */
	static Query removeJob(UUID queryId) {
		Query removedJob = queries.remove(queryId);
		boolean sanity = removedJob.getQueryState().equals(WAITING_ON_SLAVE);
		assert sanity;
		removedJob.setQueryState(SENDING_TO_CLIENT);
		return removedJob;
	}
	
	static void relayToAppropriateClient(Query query) throws JsonProcessingException {
		ClientObj requestSource = clients.get(query.getRequestingClientUUID());
		if (requestSource == null) {
			error("Tried to tell client " + query.getRequestingClientUUID() + " that " +
					query.getQueryString() + " = " +
					query.getResult() + " but the client disappeared!");
		}
		else {
			requestSource.acceptCompleted(query);
		}
	}
	
	private static void sendToLeastBusySlave(Query job) throws InterruptedException, JsonProcessingException {
		while (slaves.size() < 1) {
			debug("Tried to send job with id " + job.getQueryId() + " to a slave. " +
					"However, no slaves were available. Job queue thread sleeping " +
					"for " + NO_SLAVES_SLEEP_TIME / 1000 + " seconds...");
			Thread.sleep(NO_SLAVES_SLEEP_TIME);
		}
		/*
		 * TODO: Determining the business of slaves is not yet implemented as the
		 *  heartbeat system has not yet been implemented. For now this method
		 *  just sends the job to a random slave.
		 */
		SlaveObj designatedSlave = slaves.get((UUID) slaves.keySet().toArray()[randInt(0, slaves.size() - 1)]);
		job.setSolvingSlaveUUID(designatedSlave.getConnectionUUID());
		debug("Sending query " + job.getQueryId() + " to slave " + designatedSlave.getConnectionUUID());
		designatedSlave.accept(job);
	}
	
	private static void startJobIngestionThread() {
		Thread ingestionThread = new Thread(() -> {
			while (Server.started()) {
				try {
					Query currentJob = jobsAwaitingProcessing.take();
					sendToLeastBusySlave(currentJob);
				}
				catch (InterruptedException | JsonProcessingException e) {
					printStackTrace(e);
				}
			}
		});
		ingestionThread.setName("Job Ingestion Thread");
		ingestionThread.setDaemon(true);
		ingestionThread.start();
	}
}
