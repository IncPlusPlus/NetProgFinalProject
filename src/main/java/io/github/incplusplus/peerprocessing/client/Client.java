package io.github.incplusplus.peerprocessing.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.incplusplus.peerprocessing.common.*;
import io.github.incplusplus.peerprocessing.linear.BigDecimalMatrix;
import io.github.incplusplus.peerprocessing.query.AlgebraicQuery;
import io.github.incplusplus.peerprocessing.query.Query;
import io.github.incplusplus.peerprocessing.query.matrix.MatrixQuery;
import io.github.incplusplus.peerprocessing.query.matrix.Operation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.Socket;
import java.net.SocketException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.incplusplus.peerprocessing.client.ClientRunner.printEvalLine;
import static io.github.incplusplus.peerprocessing.client.ConsoleUtils.printSolution;
import static io.github.incplusplus.peerprocessing.common.Constants.SHARED_MAPPER;
import static io.github.incplusplus.peerprocessing.common.Demands.IDENTIFY;
import static io.github.incplusplus.peerprocessing.common.Demands.QUERY;
import static io.github.incplusplus.peerprocessing.common.MiscUtils.*;
import static io.github.incplusplus.peerprocessing.common.Responses.IDENTITY;
import static io.github.incplusplus.peerprocessing.common.Responses.RESULT;
import static io.github.incplusplus.peerprocessing.common.VariousEnums.DISCONNECT;
import static io.github.incplusplus.peerprocessing.logger.StupidSimpleLogger.*;
import static java.util.Objects.isNull;

public class Client implements ProperClient, Personable {
	private final String serverHostname;
	private final int serverPort;
	private Socket sock;
	private PrintWriter outToServer;
	private BufferedReader inFromServer;
	private volatile UUID uuid;
	private boolean printResults = false;
	private final AtomicBoolean running = new AtomicBoolean();
	/**
	 * Whether or not this client has introduced itself
	 */
	private final AtomicBoolean polite = new AtomicBoolean();
	private final ConcurrentHashMap<UUID, Query> futureQueries = new ConcurrentHashMap<>();
	
	public Client(String serverHostname, int serverPort) {
		this.serverHostname = serverHostname;
		this.serverPort = serverPort;
	}
	
	public boolean init() {
		try {
			this.sock = new Socket(serverHostname, serverPort);
			this.outToServer = new PrintWriter(sock.getOutputStream(), true);
			this.inFromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			return true;
		}
		catch (IOException e) {
			printStackTrace(e);
			return false;
		}
	}
	
	public void setVerbose(boolean verbose) {
		if (verbose) {
			enable();
			printResults = true;
		}
	}
	
	public UUID getConnectionId() {
		return uuid;
	}
	
	public boolean isClosed() {
		return !running.get();
	}
	
	public boolean isPolite() {
		return polite.get();
	}
	
	/**
	 * Begin reading or writing as expected.
	 */
	@Override
	public void begin() {
		boolean firstStart = running.compareAndSet(false, true);
		assert firstStart;
		dealWithServer();
	}
	
	/**
	 * Introduces this {@linkplain Personable} object to a server.
	 */
	@Override
	public void introduce() throws JsonProcessingException {
		debug("Introducing self to server. Connecting...");
		Introduction introduction = new Introduction();
		introduction.setSenderId(uuid);
		introduction.setSenderType(MemberType.CLIENT);
		outToServer.println(msg(SHARED_MAPPER.writeValueAsString(introduction), IDENTITY));
	}
	
	/**
	 * Closes this stream and releases any system resources associated
	 * with it. If the stream is already closed then invoking this
	 * method has no effect.
	 *
	 * <p> As noted in {@link AutoCloseable#close()}, cases where the
	 * close may fail require careful attention. It is strongly advised
	 * to relinquish the underlying resources and to internally
	 * <em>mark</em> the {@code Closeable} as closed, prior to throwing
	 * the {@code IOException}.
	 *
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	public void close() throws IOException {
		boolean notAlreadyClosed = running.compareAndSet(true, false);
		assert notAlreadyClosed;
		outToServer.println(DISCONNECT);
		kill();
	}
	
	private void kill() throws IOException {
		outToServer.close();
		inFromServer.close();
		sock.close();
	}
	
	public FutureTask<BigDecimal> evaluateExpression(String mathExpression) {
		return new FutureTask<>(new ExpressionEvaluator(mathExpression));
	}

	public FutureTask<BigDecimalMatrix> multiply(BigDecimalMatrix matrix1, BigDecimalMatrix matrix2) {
		return new FutureTask<>(new MatrixMultiplicationEvaluator(matrix1,matrix2));
	}
	
	private void dealWithServer() {
		if (isNull(sock))
			throw new IllegalStateException("Socket not initialized properly. " +
					"Did you remember to check the boolean value of Client.begin()?!");
		Thread serverInteractionThread = new Thread(() -> {
			String lineFromServer;
			while (!sock.isClosed()) {
				try {
					lineFromServer = inFromServer.readLine();
					Header header = getHeader(lineFromServer);
					if (header.equals(IDENTIFY)) {
						introduce();
						//demand the server identify and also tell us our UUID
						outToServer.println(IDENTIFY);
					}
					else if (header.equals(IDENTITY)) {
						Introduction introduction = SHARED_MAPPER.readValue(
								Objects.requireNonNull(decode(lineFromServer)), Introduction.class);
						this.uuid = introduction.getReceiverId();
						debug(this + " connected");
						this.polite.compareAndSet(false, true);
					}
					else if (header.equals(DISCONNECT)) {
						debug("Told by server to disconnect. Disconnecting..");
						disconnect();
						debug("Disconnected.");
					}
					else if (header.equals(RESULT)) {
						Query result = SHARED_MAPPER.readValue(Objects.requireNonNull(decode(lineFromServer)), Query.class);
						if (futureQueries.containsKey(result.getQueryId())) {
							Query futureQuery = futureQueries.get(result.getQueryId());
							futureQuery.setQueryState(result.getQueryState());
							futureQuery.setSolvingSlaveUUID(result.getSolvingSlaveUUID());
							futureQuery.setResult(result.getResult());
							futureQuery.setCompleted(result.isCompleted());
							//The query has been solved and is ready to be grabbed by
							//whatever FutureTask put it into futureQueries in the first place
							if (printResults) {
								printResult(futureQuery);
								//todo make printing conditional on whether this was started from ClientRunner
								printEvalLine();
							}
						}
						else {
							error("Client " + getConnectionId() + " got query " + result.getQueryId() + " but wasn't expecting it.");
						}
					}
				}
				catch (NullPointerException e) {
					if (running.get()) {
						printStackTrace(e);
					}
				}
				catch (SocketException e) {
					if (running.get()) {
						error("The server suddenly disconnected");
						try {
							kill();
						}
						catch (IOException ex) {
							printStackTrace(ex);
						}
					}
				}
				catch (IOException e) {
					printStackTrace(e);
					try {
						disconnect();
					}
					catch (IOException ex) {
						debug("There was an exception during the disconnect which began due to a previous exception!");
						printStackTrace(ex);
					}
					break;
				}
			}
		});
		serverInteractionThread.setName("Client server interaction");
		serverInteractionThread.start();
	}
	
	private void printResult(Query query) {
		if (query instanceof AlgebraicQuery) {
			printSolution((AlgebraicQuery) query);
		}
		else if(query instanceof MatrixQuery) {
		  info("No printable answer for type " + MatrixQuery.class);
        }
		else {
			throw new UnsupportedOperationException();
		}
	}

	private class MatrixMultiplicationEvaluator implements Callable<BigDecimalMatrix> {
		private final BigDecimalMatrix matrix1;
		private final BigDecimalMatrix matrix2;

		public MatrixMultiplicationEvaluator(BigDecimalMatrix matrix1, BigDecimalMatrix matrix2) {
			this.matrix1 = matrix1;
			this.matrix2 = matrix2;
		}

		@Override
		public BigDecimalMatrix call() throws Exception {
			//If the client hasn't introduced itself yet,
			//don't throw a wrench into the mix.
			while (!polite.get()) {
				Thread.sleep(50);
			}
			MatrixQuery query = new MatrixQuery(Operation.MULTIPLY,matrix1,matrix2);
			UUID correspondingQueryId = query.getQueryId();
			query.setRequestingClientUUID(uuid);
			futureQueries.put(query.getQueryId(), query);
			outToServer.println(msg(SHARED_MAPPER.writeValueAsString(query), QUERY));
			if (futureQueries.get(correspondingQueryId) == null) {
				throw new IllegalStateException("The corresponding query disappeared from the map!");
			}
			while (!futureQueries.get(correspondingQueryId).isCompleted()) {
				Thread.sleep(50);
			}
			Query resultQuery = futureQueries.get(correspondingQueryId);
			MatrixQuery resultMatrixQuery = (MatrixQuery) resultQuery;
			return (BigDecimalMatrix) resultMatrixQuery.getResult();
		}
	}
	
	class ExpressionEvaluator implements Callable<BigDecimal> {
		private final String expression;
		
		ExpressionEvaluator(String mathExpression) {
			this.expression = mathExpression;
		}
		
		@Override
		public BigDecimal call() throws Exception {
			//If the client hasn't introduced itself yet,
			//don't throw a wrench into the mix.
			while (!polite.get()) {
				Thread.sleep(50);
			}
			AlgebraicQuery query = new AlgebraicQuery(this.expression, uuid);
			UUID correspondingQueryId = query.getQueryId();
			query.setRequestingClientUUID(uuid);
			futureQueries.put(query.getQueryId(), query);
			outToServer.println(msg(SHARED_MAPPER.writeValueAsString(query), QUERY));
			if (futureQueries.get(correspondingQueryId) == null) {
				throw new IllegalStateException("The corresponding query disappeared from the map!");
			}
			while (!futureQueries.get(correspondingQueryId).isCompleted()) {
				Thread.sleep(50);
			}
			return new BigDecimal((String) futureQueries.get(correspondingQueryId).getResult());
		}
	}
	
	@Override
	public String toString() {
		return "Client" + (uuid != null ? " " + uuid : "");
	}
}
