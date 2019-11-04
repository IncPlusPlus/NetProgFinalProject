package io.github.incplusplus.peerprocessing.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.incplusplus.peerprocessing.client.Client;
import io.github.incplusplus.peerprocessing.common.Job;
import io.github.incplusplus.peerprocessing.common.StupidSimpleLogger;
import io.github.incplusplus.peerprocessing.common.ClientType;

import java.io.IOException;
import java.net.ServerSocket;
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
import static io.github.incplusplus.peerprocessing.server.JobState.SENDING_TO_CLIENT;
import static io.github.incplusplus.peerprocessing.server.JobState.WAITING_ON_SLAVE;

public class Server {
	private static ServerSocket socket;
	/**
	 * The time to sleep in milliseconds when there are no slaves around to process requests
	 */
	private static long NO_SLAVES_SLEEP_TIME = 1000 * 30;
	private final static int port = 1234;
	private static String serverName = "Processing Server";
	private static volatile AtomicBoolean started = new AtomicBoolean(false);
	private static final Map<UUID, ClientObj> clients = new ConcurrentHashMap<>();
	private static final Map<UUID, SlaveObj> slaves = new ConcurrentHashMap<>();
	/**
	 * Should probably be replaced with {@link Executors#newCachedThreadPool()} at some point.
	 * Elements of this list exist while an incoming connection is established and are removed
	 * after it has been established what {@linkplain ClientType} the incoming connection is.
	 */
	private static final List<ConnectionHandler> connectionHandlers = Collections.synchronizedList(new ArrayList<>());
	private static final ConcurrentHashMap<UUID, Job> jobs = new ConcurrentHashMap<>();
	/**
	 * A queue that holds the jobs that are waiting to be assigned and sent to a slave.
	 */
	private static final BlockingDeque<Job> jobsAwaitingProcessing = new LinkedBlockingDeque<>();
	
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
		info("Server stopped.");
		while (true) {
			System.out.println(true);
			Thread.sleep(2000);
		}
	}
	
	public static void start(int serverPort) {
		class ServerStartTask implements Runnable {
			private int port;
			
			ServerStartTask(int p) {
				port = p;
			}
			
			public void run() {
				try {
					started.compareAndSet(false, true);
					socket = new ServerSocket(port);
					info("Ready and waiting!");
					startJobIngestionThread();
					while (started.get()) {
						try {
							ConnectionHandler ch = new ConnectionHandler(socket.accept());
							register(ch);
							Thread handlerThread = new Thread(ch);
							handlerThread.setDaemon(true);
							handlerThread.start();
						}
						catch (SocketException e) {
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
				finally {
					debug("Server shutting down!");
				}
			}
		}
		Thread t = new Thread(new ServerStartTask(serverPort));
		t.setDaemon(true);
		t.start();
	}
	
	public static void stop() throws IOException {
		started.compareAndSet(true,false);
		info("Server shutting down.");
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
		info("Server shut down.");
		socket.close();
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
			slaves.remove(connectedEntity.getConnectionUUID());
		}
		else if (connectedEntity instanceof ClientObj) {
			clients.remove(connectedEntity.getConnectionUUID());
		}
		else {
			throw new IllegalArgumentException("Argument 'connectedEntity' must be a SlaveObj or ClientObj.");
		}
	}
	
	//</editor-fold>
	
	/**
	 * Add a job to the server. The server will then add this job
	 * to the job queue where it will be removed and processed.
	 * The job will also be added to a map of jobs to determine
	 * the source and destination of jobs received by slaves.
	 *
	 * @param job the job to be processed
	 */
	static void submitJob(Job job) {
		Job shouldBeNull = null;
		//Put this slave in the slaves map
		shouldBeNull = jobs.put(job.getJobId(), job);
		//ConcurrentHashMap does not support null entries. Because of this, the put() method
		//only returns null if there was no previous mapping. We want to assure there was no
		//duplicate mapping before this put() operation.
		assert shouldBeNull == null;
		jobsAwaitingProcessing.add(job);
	}
	
	/**
	 * Remove a job from the jobs list. At this point,
	 * the job has just been processed by a slave.
	 * It's internal status will still be {@link JobState#WAITING_ON_SLAVE}
	 *
	 * @param jobId the id of the job to remove
	 */
	static Job removeJob(UUID jobId) {
		Job removedJob = jobs.remove(jobId);
		assert removedJob.getJobState().equals(WAITING_ON_SLAVE);
		removedJob.setJobState(SENDING_TO_CLIENT);
		return removedJob;
	}
	
	static void relayToAppropriateClient(Job job) throws JsonProcessingException {
		ClientObj requestSource = clients.get(job.getRequestingClientUUID());
		requestSource.acceptCompleted(job.getMathQuery());
	}
	
	private static void sendToLeastBusySlave(Job job) throws InterruptedException, JsonProcessingException {
		while (slaves.size() < 1) {
			debug("Tried to send job with id " + job.getJobId() + " to a slave.\n" +
					"However, no slaves were available. Queueing thread sleeping " +
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
		designatedSlave.accept(job);
	}
	
	private static void startJobIngestionThread() {
		Thread ingestionThread = new Thread(() -> {
			while (!socket.isClosed()) {
				try {
					Job currentJob = jobsAwaitingProcessing.take();
					sendToLeastBusySlave(currentJob);
				}
				catch (InterruptedException | JsonProcessingException e) {
					printStackTrace(e);
				}
			}
		});
		ingestionThread.setDaemon(true);
		ingestionThread.start();
	}
}
