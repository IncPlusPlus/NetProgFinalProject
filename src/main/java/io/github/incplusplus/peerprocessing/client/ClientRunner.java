package io.github.incplusplus.peerprocessing.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.javatuples.Pair;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.github.incplusplus.peerprocessing.common.MiscUtils.promptForHostPortTuple;
import static io.github.incplusplus.peerprocessing.logger.StupidSimpleLogger.*;

/**
 * This class is purely a driver class for a single {@link Client}.
 * Run the main method here to create an instance of a Client
 * and interact with it through the console.
 * <br>
 * The goal of this class is to make the {@link Client} class
 * as object-oriented as possible and avoid using its main method.
 * This also will help with making improving coverage easier.
 */
public final class ClientRunner {
	public static void main(String[] args) throws IOException {
		enable();
		Pair<String, Integer> hostAndPortPair = promptForHostPortTuple();
		Client mainClient = new Client(hostAndPortPair.getValue0(), hostAndPortPair.getValue1());
		Scanner in = new Scanner(System.in);
		mainClient.setVerbose(true);
		info("Connecting...");
		mainClient.init();
		mainClient.begin();
		while (!mainClient.isPolite()) {}
		info("\nIf you want to enter an expression, type it and hit enter.\n" +
				"After you have entered your expression, it may take a moment for the server to respond.\n" +
				"You'll see 'Evaluate: ' again after submitting. You may choose to wait (recommended) " +
				"or you may attempt to enter a second expression while the first processes. \n" +
				"This is not recommended " +
				"as you may be interrupted by the first result while you type the second expression.\n" +
				"To exit, type /q and hit enter.\n");
		String consoleLine;
		ExecutorService executor = Executors.newSingleThreadExecutor();
		printEvalLine();
		while (!mainClient.isClosed()) {
			try {
				consoleLine = in.nextLine();
				if (consoleLine.equalsIgnoreCase("/q")) {
					mainClient.close();
					break;
				}
				executor.submit(mainClient.evaluateExpression(consoleLine));
				//Sleep for a hot second in case the answer comes in super fast
				Thread.sleep(250);
			}
			catch (InterruptedException | JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		executor.shutdown();
	}
	
	static void printEvalLine() {
		infoNoLine("Evaluate: ");
	}
}
