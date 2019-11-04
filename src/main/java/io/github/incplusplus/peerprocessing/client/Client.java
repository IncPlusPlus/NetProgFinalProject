package io.github.incplusplus.peerprocessing.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.incplusplus.peerprocessing.common.*;
import org.javatuples.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static io.github.incplusplus.peerprocessing.common.Constants.SHARED_MAPPER;
import static io.github.incplusplus.peerprocessing.common.Demands.*;
import static io.github.incplusplus.peerprocessing.common.MiscUtils.*;
import static io.github.incplusplus.peerprocessing.common.Responses.IDENTITY;
import static io.github.incplusplus.peerprocessing.common.Responses.SOLUTION;
import static io.github.incplusplus.peerprocessing.common.StupidSimpleLogger.*;
import static io.github.incplusplus.peerprocessing.common.VariousEnums.DISCONNECT;

public class Client implements ProperClient, Personable {
	private final String serverHostname;
	/**
	 * If true, this is being used by a human with the console. If false, it is being used as an API passthrough
	 */
	private boolean usedWithConsole;
	private final int serverPort;
	private Socket sock;
	private PrintWriter outToServer;
	private BufferedReader inFromServer;
	private Scanner in = new Scanner(System.in);
	private String name;
	private UUID uuid;
	
	public static void main(String[] args) throws IOException {
		enable();
		Pair<String, Integer> hostAndPortPair = promptForHostPortTuple();
		Client mainClient = new Client(hostAndPortPair.getValue0(), hostAndPortPair.getValue1());
		mainClient.usedWithConsole = true;
		mainClient.init();
		mainClient.begin();
	}
	
	public Client(String serverHostname, int serverPort) {
		this.serverHostname = serverHostname;
		this.serverPort = serverPort;
	}
	
	public void init() throws IOException {
		this.sock = new Socket(serverHostname, serverPort);
		this.outToServer = new PrintWriter(sock.getOutputStream(), true);
		this.inFromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
	}
	
	/**
	 * Begin reading or writing as expected.
	 */
	@Override
	public void begin() {
		info("\nIf you want to enter an expression, type it and hit enter.\n" +
				"After you have entered your expression, it may take a moment for the server to respond.\n" +
				"You'll see 'Evaluate: ' again after submitting. You may choose to wait (recommended) " +
				"or you may attempt to enter a second expression while the first processes. \n" +
				"This is not recommended " +
				"as you may be interrupted by the first result while you type the second expression.\n" +
				"To exit, type /q and hit enter.\n");
		dealWithServer();
		String consoleLine;
		while (!sock.isClosed()) {
			if (usedWithConsole)
				printEvalLine();
			try {
				consoleLine = getConsoleLine();
				if (consoleLine == null) {
					break;
				}
				outToServer.println(msg(consoleLine, SOLVE));
			}
			catch (ExecutionException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String getConsoleLine() throws ExecutionException, InterruptedException {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		Future<String> future = executorService.submit(new ConsoleInputReadTask(sock));
		executorService.shutdown();
		return future.get();
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
		introduction.setSenderType(MemberType.CLIENT);
		outToServer.println(msg(SHARED_MAPPER.writeValueAsString(introduction), IDENTITY));
		debug("Connected");
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
		outToServer.close();
		inFromServer.close();
		sock.close();
	}
	
	private void dealWithServer() {
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
						this.uuid = UUID.fromString(decode(lineFromServer));
					}
					else if (header.equals(PROVIDE_CLIENT_NAME)) {
						throw new IllegalStateException("RUN! EVERYBODY RUN!");
					}
					else if (header.equals(DISCONNECT)) {
						debug("Told by server to disconnect. Disconnecting..");
						close();
						debug("Disconnected.");
					}
					else if (header.equals(SOLUTION)) {
						if (usedWithConsole)
							printSolution(SHARED_MAPPER.readValue(decode(lineFromServer), MathJob.class));
					}
				}
				catch (SocketException e) {
					printStackTrace(e);
					error("The server suddenly disconnected");
					try {
						disconnect();
					}
					catch (IOException ex) {
						ex.printStackTrace();
					}
				}
				catch (IOException e) {
					printStackTrace(e);
					try {
						disconnect();
					}
					catch (IOException ex) {
						debug("There was an exception during the disconnect which began due to a previous exception!");
						ex.printStackTrace();
					}
					break;
				}
			}
		});
		serverInteractionThread.setDaemon(true);
		serverInteractionThread.start();
	}
	
	private void printSolution(MathJob query) {
		if (query.isSolved()) {
			System.out.println();
			debug("The solution for the problem \"" + query.getOriginalExpression() + "\" is: \"" + query.getResult() + "\"");
		}
		else {
			System.out.println();
			debug("The solution for the problem \"" + query.getOriginalExpression() + "\" could not be found.");
			debug("The reason for this is: " + query.getReasonUnsolved().toString());
			debug("Stacktrace: \n" + Arrays.toString(query.getReasonUnsolved().getStackTrace()));
		}
		printEvalLine();
	}
	
	private void printEvalLine() {
		infoNoLine("Evaluate: ");
	}
}
