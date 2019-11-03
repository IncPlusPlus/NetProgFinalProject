package io.github.incplusplus.peerprocessing.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.incplusplus.peerprocessing.common.*;
import io.github.incplusplus.peerprocessing.slave.Slave;
import org.javatuples.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.UUID;

import static io.github.incplusplus.peerprocessing.common.Constants.SHARED_MAPPER;
import static io.github.incplusplus.peerprocessing.common.Demands.*;
import static io.github.incplusplus.peerprocessing.common.MiscUtils.*;
import static io.github.incplusplus.peerprocessing.common.Responses.IDENTITY;
import static io.github.incplusplus.peerprocessing.common.Responses.SOLUTION;
import static io.github.incplusplus.peerprocessing.common.StupidSimpleLogger.enable;
import static io.github.incplusplus.peerprocessing.common.StupidSimpleLogger.log;

public class Client implements ProperClient, Personable {
	private final String serverHostname;
	private final int serverPort;
	private Socket sock;
	private PrintWriter outToServer;
	private BufferedReader inFromServer;
	private Scanner in = new Scanner(System.in);
	private String name;
	private UUID uuid = UUID.randomUUID();
	
	public static void main(String[] args) throws IOException {
		enable();
		Pair<String, Integer> hostAndPortPair = promptForHostPortTuple();
		Client mainClient = new Client(hostAndPortPair.getValue0(), hostAndPortPair.getValue1());
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
		System.out.println("If you want to enter an expression, type it and hit enter.");
		System.out.println("After you have entered your expression, it may take a moment for the server to respond.");
		System.out.println("You'll see 'Evaluate: ' again after submitting. You may choose to wait (recommended) " +
				"or you may attempt to enter a second expression while the first processes.\nThis is not recommended " +
				"as you may be interrupted by the first result while you type the second expression.");
		dealWithServer();
		while(!sock.isClosed()) {
			printEvalLine();
			outToServer.println(msg(in.nextLine(), SOLVE));
		}
	}
	
	/**
	 * Introduces this {@linkplain Personable} object to a server.
	 */
	@Override
	public void introduce() throws JsonProcessingException {
		Introduction introduction = new Introduction();
		introduction.setName(name);
		introduction.setId(uuid);
		introduction.setType(ClientType.CLIENT);
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
		//TODO implement
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
					}
					else if (header.equals(PROVIDE_CLIENT_NAME)) {
						throw new IllegalStateException("RUN! EVERYBODY RUN!");
					}
					else if (header.equals(SOLUTION)) {
						printSolution(SHARED_MAPPER.readValue(decode(lineFromServer), MathQuery.class));
					}
				}
				catch (IOException e) {
					e.printStackTrace();
					try {
						disconnect();
					}
					catch (IOException ex) {
						log("There was an exception during the disconnect which began due to a previous exception!");
						ex.printStackTrace();
					}
					break;
				}
			}
		});
		serverInteractionThread.setDaemon(true);
		serverInteractionThread.start();
	}
	
	private void printSolution(MathQuery query) {
		if (query.isSolved()) {
			log("\nThe solution for the problem \"" + query.getOriginalExpression() + "\" is: \"" + query.getResult() + "\"");
		}
		else {
			log("\nThe solution for the problem \"" + query.getOriginalExpression() + "\" could not be found.");
			log("The reason for this is: " + query.getReasonUnsolved().toString());
			log("Stacktrace: \n" + Arrays.toString(query.getReasonUnsolved().getStackTrace()));
		}
		printEvalLine();
	}
	
	private void printEvalLine() {
		System.out.print("Evaluate: ");
	}
}
