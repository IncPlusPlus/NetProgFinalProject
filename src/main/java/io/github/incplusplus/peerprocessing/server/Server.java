package io.github.incplusplus.peerprocessing.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.github.incplusplus.peerprocessing.common.*;
import io.github.incplusplus.peerprocessing.logger.StupidSimpleLogger;
import io.github.incplusplus.peerprocessing.query.AlgebraicQuery;
import io.github.incplusplus.peerprocessing.query.BatchQuery;
import io.github.incplusplus.peerprocessing.query.PoisonPill;
import io.github.incplusplus.peerprocessing.query.Query;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.incplusplus.peerprocessing.common.Constants.SHARED_MAPPER;
import static io.github.incplusplus.peerprocessing.common.Demands.IDENTIFY;
import static io.github.incplusplus.peerprocessing.common.Demands.QUERY;
import static io.github.incplusplus.peerprocessing.common.MiscUtils.*;
import static io.github.incplusplus.peerprocessing.common.Responses.IDENTITY;
import static io.github.incplusplus.peerprocessing.common.Responses.RESULT;
import static io.github.incplusplus.peerprocessing.common.VariousEnums.DISCONNECT;
import static io.github.incplusplus.peerprocessing.logger.StupidSimpleLogger.*;
import static io.github.incplusplus.peerprocessing.server.QueryState.SENDING_TO_CLIENT;
import static io.github.incplusplus.peerprocessing.server.QueryState.WAITING_ON_SLAVE;
import static io.github.incplusplus.peerprocessing.server.ServerMethods.negotiate;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class Server {
	private ServerSocket socket;
	//mostly unused
	private final UUID serverId = UUID.randomUUID();
	//server naming is a low priority at the moment
	final static String serverName = "Processing Server";
	private final AtomicBoolean started = new AtomicBoolean(false);
	private final AtomicBoolean shutdownInProgress = new AtomicBoolean(false);
	private final Map<UUID, ClientObj> clients = new ConcurrentHashMap<>();
	final Map<UUID, SlaveObj> slaves = new ConcurrentHashMap<>();
	/**
	 * Should probably be replaced with {@link Executors#newCachedThreadPool()} at some point.
	 * Elements of this list exist while an incoming connection is established and are removed
	 * after it has been established what {@linkplain MemberType} the incoming connection is.
	 */
	private final List<ConnectionHandler> connectionHandlers = Collections.synchronizedList(new ArrayList<>());
	/** A HashMap that holds all queries that are being processed or relayed. */
	private final ConcurrentHashMap<UUID, Query> queries = new ConcurrentHashMap<>();
	/**
	 *  A HashMap that contains jobs that are to be processed in batches.
	 *  ClientObj and SlaveObj should not interact with this Map. Only the server
	 *  coordinates when an action is taken with the items in this Map.
	 */
	private final ConcurrentHashMap<UUID, BatchQuery> batchQueries = new ConcurrentHashMap<>();
	/**
	 * A queue that holds the jobs that are waiting to be assigned and sent to a slave.
	 */
	private final BlockingDeque<Query> jobsAwaitingProcessing = new LinkedBlockingDeque<>();
	
	/**
	 * Start a server.
	 * @param serverPort the port to start the server on.
	 *                    If set to 0, the server will listen on
	 *                    whatever port is available.
	 * @param verbose whether the server should log to the console about every little thing
	 * @return the port the server started listening on
	 * @throws IOException if an IO error occurs
	 */
	public int start(int serverPort, boolean verbose) throws IOException {
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
	 * @throws IOException if an IO error occurs
	 */
	private int start(int serverPort) throws IOException {
		class ServerStartTask implements Runnable {
			private final ServerSocket serverSocket;
			
			private ServerStartTask(ServerSocket socket) {
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
	
	public void stop() throws IOException {
		started.compareAndSet(true, false);
		shutdownInProgress.compareAndSet(false, true);
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
		poisonJobIngestionThread();
		shutdownInProgress.compareAndSet(true, false);
	}
	
	public boolean shutdownInProgress() {
		return shutdownInProgress.get();
	}
	
	public boolean started() {
		return started.get();
	}
	
	//<editor-fold desc="register() methods">
	
	/**
	 * Register a {@linkplain ConnectionHandler} within the server.
	 *
	 * @param connectionHandler the ConnectionHandler to register.
	 */
	private void register(ConnectionHandler connectionHandler) {
		connectionHandlers.add(connectionHandler);
	}
	
	/**
	 * Register a {@linkplain ConnectedEntity} within the server.
	 * This keeps track of the connected clients and slaves.
	 *
	 * @param connectedEntity an entity to keep track of
	 */
	private void register(ConnectedEntity connectedEntity) {
		if (connectedEntity instanceof SlaveObj) {
			SlaveObj shouldBeNull;
			//Put this slave in the slaves map
			shouldBeNull = slaves.put(connectedEntity.getConnectionUUID(), (SlaveObj) connectedEntity);
			//ConcurrentHashMap does not support null entries. Because of this, the put() method
			//only returns null if there was no previous mapping. We want to assure there was no
			//duplicate mapping before this put() operation.
			assert shouldBeNull == null;
		}
		else if (connectedEntity instanceof ClientObj) {
			ClientObj shouldBeNull;
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
	private void deRegister(ConnectionHandler connectionHandler) {
		connectionHandlers.remove(connectionHandler);
	}
	
	/**
	 * Remove the specified {@linkplain ConnectedEntity} from the server's list of ConnectedEntities.
	 * This can be a {@link io.github.incplusplus.peerprocessing.client.Client}
	 * or a {@link io.github.incplusplus.peerprocessing.slave.Slave}
	 *
	 * @param connectedEntity the entity to remove
	 */
	private void deRegister(ConnectedEntity connectedEntity) {
		if (connectedEntity instanceof SlaveObj) {
			synchronized (slaves) {
				SlaveObj shouldBeNonNull = slaves.remove(connectedEntity.getConnectionUUID());
				assert nonNull(shouldBeNonNull);
				//noinspection StatementWithEmptyBody
				while(nonNull(slaves.get(connectedEntity.getConnectionUUID()))) {}
				//for each of the queries the slave was processing
				for (UUID uuid : ((SlaveObj) connectedEntity).getJobsResponsibleFor()) {
					//put them in the queue to get processed
					debug("Slave " + connectedEntity.getConnectionUUID()+ " abandoned job " + uuid);
					boolean abandonedJobAdded = jobsAwaitingProcessing.add(queries.get(uuid));
					assert abandonedJobAdded;
				}
			}
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
	boolean isConnected(UUID uuid) {
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
	private void submitJob(Query query) {
		Query shouldBeNull;
		if(query instanceof BatchQuery){
			shouldBeNull = batchQueries.put(query.getQueryId(), (BatchQuery) query);
		}
		else{
			shouldBeNull = queries.put(query.getQueryId(), query);
		}
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
	private Query removeJob(UUID queryId) {
		Query removedJob = queries.remove(queryId);
		if(isNull(removedJob)){
			removedJob=batchQueries.remove(queryId);
			if(isNull(removedJob))
				debug("Tried to remove a job that existed in neither 'queries' nor 'batchQueries'.");
		}
		return removedJob;
	}
	
	/**
	 * Tell the server a certain job has been completed. The server checks
	 * whether this Query is a member of any active {@linkplain BatchQuery}
	 * instance.
	 * @param query the query that has been completed by a slave
	 */
	private void submitCompletedJob(Query query) throws JsonProcessingException {
		UUID batchQueryId = query.getParentBatchId();
		if (nonNull(batchQueryId)) {
          synchronized (batchQueries) {
            BatchQuery responsibleBatchQuery = batchQueries.get(batchQueryId);
            if(nonNull(responsibleBatchQuery)){
                boolean sanityCheck = responsibleBatchQuery.offer(query);
                assert sanityCheck;
                debug("Completed job " + query.getQueryId() + " in batch " + responsibleBatchQuery.getQueryId());
                if(responsibleBatchQuery.isCompleted()) {
                    debug("Completed batch " + responsibleBatchQuery.getQueryId());
                    Query removedJob = removeJob(responsibleBatchQuery.getQueryId());
                    responsibleBatchQuery.setCompleted(true);
                    removedJob.setQueryState(SENDING_TO_CLIENT);
                    relayToAppropriateClient(responsibleBatchQuery);
                }
            }
          }
        }
		else {
			query.setQueryState(SENDING_TO_CLIENT);
			relayToAppropriateClient(query);
		}
	}
	
	private void relayToAppropriateClient(Query query) throws JsonProcessingException {
		ClientObj requestSource = clients.get(query.getRequestingClientUUID());
		if (requestSource == null) {
			error("Tried to tell client " + query.getRequestingClientUUID() + " the answer to " +
					query.getQueryId() + " but the client disappeared!");
		}
		else {
			requestSource.acceptCompleted(query);
		}
	}
	
	private void sendToLeastBusySlave(Query job) throws InterruptedException, JsonProcessingException {
		SlaveObj leastBusySlave;
		while (slaves.size() < 1) {
			debug("Tried to send job with id " + job.getQueryId() + " to a slave. " +
					"However, no slaves were available. Job queue thread sleeping.");
			Thread.sleep(250);
		}
		/*
		 * TODO: Determining the business of slaves is not yet implemented as the
		 *  heartbeat system has not yet been implemented. For now this method
		 *  just sends the job to the slave dealing with the fewest jobs.
		 */
		//just because it's a ConcurrentHashMap doesn't mean everything is safe!
		synchronized (slaves) {
			leastBusySlave = slaves.entrySet().parallelStream()
					.map(Map.Entry::getValue)
					//find the slave that is responsible for the fewest jobs
					.min(Comparator.comparingInt(slaveObj -> slaveObj.getJobsResponsibleFor().size()))
					//get the actual SlaveObj or else
					//commit suicide
					.orElseThrow();
		}
		job.setSolvingSlaveUUID(leastBusySlave.getConnectionUUID());
		debug("Sending query " + job.getQueryId() + " to slave " + leastBusySlave.getConnectionUUID());
		leastBusySlave.accept(job);
	}
	
	private void poisonJobIngestionThread() {
		jobsAwaitingProcessing.add(new PoisonPill());
	}
	
	private void startJobIngestionThread() {
		Thread ingestionThread = new Thread(() -> {
			debug("Starting job ingestion thread. (Server.started() = "+started()+")");
			synchronized (jobsAwaitingProcessing) {
				if(!jobsAwaitingProcessing.isEmpty()) {
					debug("Job queue was not empty on startup. Popping all elements...");
					while(!jobsAwaitingProcessing.isEmpty()) {
						try{
							debug("Popped " + jobsAwaitingProcessing.pop());
						}
						catch (NoSuchElementException e) {
							debug("Finished popping from the queue.");
						}
					}
				}
			}
			while (started()) {
				try {
					Query currentJob = jobsAwaitingProcessing.take();
					if (currentJob instanceof PoisonPill) {
						debug("Job ingestion thread ate a poison pill and is shutting down.");
						break;
					}
					if(currentJob instanceof BatchQuery) {
						currentJob.setQueryState(WAITING_ON_SLAVE);
						for(Query individualJob : ((BatchQuery) currentJob).getQueries()) {
							queries.put(individualJob.getQueryId(),individualJob);
							jobsAwaitingProcessing.add(individualJob);
						}
					}
					else {
						sendToLeastBusySlave(currentJob);
					}
				}
				catch (InterruptedException | JsonProcessingException e) {
					printStackTrace(e);
					try {
						//If job ingestion thread dies, kill off server
						this.stop();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		});
		ingestionThread.setName("Job Ingestion Thread");
		ingestionThread.setDaemon(true);
		ingestionThread.start();
	}
	
	class ClientObj extends ConnectedEntity {
		ClientObj(PrintWriter outToClient, BufferedReader inFromClient, Socket socket,
		          UUID connectionUUID) {super(outToClient, inFromClient, socket, connectionUUID);}
		
		@Override
		UUID getServerId() {
			return serverId;
		}
		
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
					if (header.equals(QUERY)) {
						solve(SHARED_MAPPER.readValue(Objects.requireNonNull(decode(lineFromClient)), Query.class));
					}
					else if (header.equals(IDENTIFY)) {
						getOutToClient().println(
								msg(SHARED_MAPPER.writeValueAsString(provideIntroductionFromServer()), IDENTITY));
					}
					else if (header.equals(DISCONNECT)) {
						deRegister(this);
						//the client already is ending their connection.
						//we don't want to write back
						kill();
						break;
					}
				}
				catch (SocketException e) {
					debug("Client " + getConnectionUUID() + " disconnected.");
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
			}
		}
		
		void acceptCompleted(Query query) throws JsonProcessingException {
			getOutToClient().println(msg(SHARED_MAPPER.writeValueAsString(query), RESULT));
		}
		
		private void solve(Query query) {
			debug("Got query " + query.getQueryId() + " from client " + getConnectionUUID());
			query.setRequestingClientUUID(getConnectionUUID());
			submitJob(query);
		}
	}
	
	class SlaveObj extends ConnectedEntity {
		private final List<UUID> jobsResponsibleFor = Collections.synchronizedList(new ArrayList<>());
		
		SlaveObj(PrintWriter outToClient, BufferedReader inFromClient, Socket socket,
		         UUID connectionUUID) {super(outToClient, inFromClient, socket, connectionUUID);}
		
		@Override
		UUID getServerId() {
			return serverId;
		}
		
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
			String lineFromSlave;
			while (!getSocket().isClosed()) {
				try {
					lineFromSlave = getInFromClient().readLine();
					Header header = getHeader(lineFromSlave);
					if (header.equals(RESULT)) {
						Query completedQuery = SHARED_MAPPER.readValue(Objects.requireNonNull(decode(lineFromSlave)), Query.class);
						Query storedQuery = removeJob(completedQuery.getQueryId());
						//We keep the originally created query object and only take what we need from the
						//slave's data. This is to prevent possibly malicious slaves from compromising
						//our good and pure clients who can do nothing wrong.
						storedQuery.setQueryState(QueryState.COMPLETE);
						storedQuery.setReasonIncomplete(completedQuery.getReasonIncomplete());
						storedQuery.setResult(completedQuery.getResult());
						storedQuery.setCompleted(true);
						submitCompletedJob(storedQuery);
						boolean definitelyShouldStillPossessJob = jobsResponsibleFor.remove(storedQuery.getQueryId());
						assert definitelyShouldStillPossessJob;
					}
					else if (header.equals(IDENTIFY)) {
						getOutToClient().println(
								msg(SHARED_MAPPER.writeValueAsString(provideIntroductionFromServer()), IDENTITY));
					}
					else if (header.equals(DISCONNECT)) {
						deRegister(this);
						//the client already is ending their connection.
						//we don't want to write back
						kill();
						break;
					}
				}
				catch (NullPointerException npe) {
					slaveDisconnected(true);
				}
				catch (SocketException e) {
					slaveDisconnected(false);
				}
				catch (IOException e) {
					if (e instanceof JsonMappingException) {
						if (e.getCause() instanceof JsonEOFException) {
							slaveDisconnected(true);
						}
					}
					else {
						printStackTrace(e);
					}
				}
			}
		}

		private void slaveDisconnected(boolean violently) {
			debug("Slave " + getConnectionUUID() + (violently ? " violently" : "") + " disconnected.");
			deRegister(this);
			try {
				getSocket().close();
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		/**
		 * Send a query to this slave for processing.
		 *
		 * @param query the query to send
		 * @throws JsonProcessingException if something goes horribly wrong
		 */
		void accept(Query query) throws JsonProcessingException {
			query.setQueryState(WAITING_ON_SLAVE);
			jobsResponsibleFor.add(query.getQueryId());
			debug("Slave " + getConnectionUUID() + " now responsible for " + query.getQueryId());
			getOutToClient().println(msg(SHARED_MAPPER.writeValueAsString(query), QUERY));
		}
		
		/**
		 * @return the list of UUIDs representing jobs that this slave is currently
		 * responsible for. Useful for recovering a job if a slave suddenly disconnects.
		 */
		List<UUID> getJobsResponsibleFor() {
			return jobsResponsibleFor;
		}
	}
	
	class ConnectionHandler implements Runnable {
		private volatile PrintWriter outToClient;
		private volatile BufferedReader inFromClient;
		private Socket socket;
		private UUID connectionUUID;
		
		ConnectionHandler(Socket incomingConnection) throws IOException {
			if (incomingConnection == null || incomingConnection.isClosed())
				throw new IllegalArgumentException("An incoming connection was established" +
						"but was then immediately dropped.");
			debug("New connection at " + incomingConnection.getLocalAddress().getHostAddress() + ":" + incomingConnection.getLocalPort());
			this.connectionUUID = UUID.randomUUID();
			this.socket = incomingConnection;
			this.inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.outToClient = new PrintWriter(socket.getOutputStream(), true);
		}
		
		public void run() {
			try {
				Introduction clientIntroduction = SHARED_MAPPER.readValue(
						negotiate(IDENTIFY, IDENTITY, outToClient, inFromClient), Introduction.class);
				if (clientIntroduction.getSenderType().equals(MemberType.CLIENT)) {
					ClientObj client = new ClientObj(outToClient, inFromClient, socket, connectionUUID);
					Thread clientThread = new Thread(client);
					clientThread.setDaemon(true);
					clientThread.setName("Client at " + socket.getLocalAddress().getHostAddress());
					clientThread.start();
					debug("Registering new client");
					register(client);
				}
				else if (clientIntroduction.getSenderType().equals(MemberType.SLAVE)) {
					SlaveObj slave = new SlaveObj(outToClient, inFromClient, socket, connectionUUID);
					Thread clientThread = new Thread(slave);
					clientThread.setDaemon(true);
					clientThread.setName("Slave at " + socket.getLocalAddress().getHostAddress());
					clientThread.start();
					debug("Registering new slave");
					register(slave);
				}
			}
			catch (IOException e) {
				printStackTrace(e);
			}
			finally {
				//Remove this ConnectionHandler from the connectionHandlers list
				deRegister(this);
			}
		}
	}
}
