package io.github.incplusplus.peerprocessing.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.javatuples.Pair;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static io.github.incplusplus.peerprocessing.client.ConsoleUtils.printSolution;
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
	private static List<FutureTask<BigDecimal>> queries = Collections.synchronizedList(new ArrayList<>());
	private volatile static Client mainClient;
	
	public static void main(String[] args) throws IOException {
		enable();
		Pair<String, Integer> hostAndPortPair = promptForHostPortTuple();
		mainClient = new Client(hostAndPortPair.getValue0(), hostAndPortPair.getValue1());
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
				consoleLine = getConsoleLine(mainClient);
				if (consoleLine == null) {
					break;
				}
				executor.submit(mainClient.evaluateExpression(consoleLine));
				//Sleep for a hot second in case the answer comes in super fast
				Thread.sleep(250);
			}
			catch (ExecutionException | InterruptedException | JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		executor.shutdown();
	}
	
	static void printEvalLine() {
		infoNoLine("Evaluate: ");
	}
	
	private static String getConsoleLine(Client dependentClient) throws ExecutionException, InterruptedException {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		Future<String> future = executorService.submit(new ConsoleInputReadTask(dependentClient));
		executorService.shutdown();
		return future.get();
	}
}
