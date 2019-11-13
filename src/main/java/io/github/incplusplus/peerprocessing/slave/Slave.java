package io.github.incplusplus.peerprocessing.slave;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.udojava.evalex.Expression;
import io.github.incplusplus.peerprocessing.common.*;
import org.javatuples.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.incplusplus.peerprocessing.common.Constants.SHARED_MAPPER;
import static io.github.incplusplus.peerprocessing.common.Demands.*;
import static io.github.incplusplus.peerprocessing.common.MiscUtils.*;
import static io.github.incplusplus.peerprocessing.common.Responses.IDENTITY;
import static io.github.incplusplus.peerprocessing.common.Responses.RESULT;
import static io.github.incplusplus.peerprocessing.logger.StupidSimpleLogger.*;
import static io.github.incplusplus.peerprocessing.common.VariousEnums.DISCONNECT;
import static java.util.Objects.isNull;

public class Slave implements ProperClient, Personable {
	private final String serverHostname;
	private final int serverPort;
	private Socket sock;
	private AtomicBoolean running = new AtomicBoolean();
	/**
	 * Whether or not this client has introduced itself
	 */
	private AtomicBoolean polite = new AtomicBoolean();
	private PrintWriter outToServer;
	private BufferedReader inFromServer;
	private String name;
	private volatile UUID uuid = UUID.randomUUID();
	
	public static void main(String[] args) throws IOException {
		enable();
		Pair<String, Integer> hostAndPortPair = promptForHostPortTuple();
		Slave mainSlave = new Slave(hostAndPortPair.getValue0(), hostAndPortPair.getValue1());
		mainSlave.init();
		mainSlave.begin();
	}
	
	public Slave(String serverHostname, int serverPort) {
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
		if (verbose)
			enable();
	}
	
	public boolean isClosed() {
		return !running.get();
	}
	
	public boolean isPolite() {
		return polite.get();
	}
	
	public UUID getConnectionId() {
		return uuid;
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
		introduction.setSenderName(name);
		introduction.setSenderId(uuid);
		introduction.setSenderType(MemberType.SLAVE);
		outToServer.println(msg(SHARED_MAPPER.writeValueAsString(introduction), Responses.IDENTITY));
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
	
	private void dealWithServer() {
		if (isNull(sock))
			throw new IllegalStateException("Socket not initialized properly. " +
					"Did you remember to check the boolean value of Slave.begin()?!");
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
						Introduction introduction = SHARED_MAPPER.readValue(decode(lineFromServer), Introduction.class);
						this.uuid = introduction.getReceiverId();
						debug(this + " connected");
						this.polite.compareAndSet(false, true);
					}
					else if (header.equals(QUERY)) {
						Query query = SHARED_MAPPER.readValue(decode(lineFromServer), Query.class);
						debug("Solving: " + query.getQueryId() + " - " + query.getQueryString());
						sendEvaluatedQuery(evaluate(query));
					}
					else if (header.equals(DISCONNECT)) {
						debug("Told by server to disconnect. Disconnecting..");
						disconnect();
						debug("Disconnected.");
					}
					else if (header.equals(PROVIDE_CLIENT_NAME)) {
						throw new IllegalStateException("RUN! EVERYBODY RUN!");
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
		serverInteractionThread.setName("Slave server interaction");
		serverInteractionThread.start();
	}
	
	/**
	 * Solves and returns a mathQuery. If an exception occurs,
	 * it will be contained in the query enclosed by the specified mathQuery.
	 *
	 * @param mathQuery the mathQuery to complete
	 * @return the mathQuery containing the
	 * completed query or a query containing a stacktrace if incomplete
	 */
	private MathQuery solve(MathQuery mathQuery) {
		Expression expression = new Expression(mathQuery.getQueryString());
		try {
			mathQuery.setResult(expression.eval().toString());
			mathQuery.setCompleted(true);
		}
		catch (Exception e) {
			printStackTrace(e);
			//We've completed it to the best of our ability
			//client should check the throwable!=null
			mathQuery.setCompleted(true);
			mathQuery.setReasonIncomplete(e);
		}
		return mathQuery;
	}
	
	private Query evaluate(Query query) {
		if (query instanceof MathQuery) {
			return solve((MathQuery) query);
		}
		else {
			throw new UnsupportedOperationException();
		}
	}
	
	private void sendEvaluatedQuery(Query query) throws JsonProcessingException {
		outToServer.println(msg(SHARED_MAPPER.writeValueAsString(query), RESULT));
	}
	
	@Override
	public String toString() {
		return "Slave" + (uuid != null ? " " + uuid : "");
	}
}
