package io.github.incplusplus.peerprocessing.slave;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.incplusplus.peerprocessing.common.*;
import org.javatuples.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.UUID;

import static io.github.incplusplus.peerprocessing.common.Constants.SHARED_MAPPER;
import static io.github.incplusplus.peerprocessing.common.Demands.IDENTIFY;
import static io.github.incplusplus.peerprocessing.common.Demands.PROVIDE_CLIENT_NAME;
import static io.github.incplusplus.peerprocessing.common.MiscUtils.*;
import static io.github.incplusplus.peerprocessing.common.StupidSimpleLogger.enable;
import static io.github.incplusplus.peerprocessing.common.StupidSimpleLogger.log;

public class Slave implements ProperClient, Personable {
	private final String serverHostname;
	private final int serverPort;
	private Socket sock;
	private PrintWriter outToServer;
	private BufferedReader inFromServer;
	private Scanner in;
	private String name;
	private UUID uuid = UUID.randomUUID();
	
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
	
	public void init() throws IOException {
		this.sock = new Socket(serverHostname, serverPort);
		this.outToServer = new PrintWriter(sock.getOutputStream());
		this.inFromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
	}
	
	/**
	 * Begin reading or writing as expected.
	 */
	@Override
	public void begin() throws IOException {
		dealWithServer();
	}
	
	/**
	 * Introduces this {@linkplain Personable} object to a server.
	 */
	@Override
	public void introduce() throws JsonProcessingException {
		Introduction introduction = new Introduction();
		introduction.setName(name);
		introduction.setId(uuid);
		introduction.setType(ClientType.SLAVE);
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
}
