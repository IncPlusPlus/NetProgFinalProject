package io.github.incplusplus.peerprocessing.client;

import org.javatuples.Pair;

import java.io.IOException;

import static io.github.incplusplus.peerprocessing.common.MiscUtils.promptForHostPortTuple;
import static io.github.incplusplus.peerprocessing.common.StupidSimpleLogger.enable;

/**
 * This class is purely a driver class for a single {@link Client}.
 * Run the main method here to create an instance of a Client
 * and interact with it through the console.
 * <br>
 * The goal of this class is to make the {@link Client} class
 * as object-oriented as possible and avoid using its main method.
 * This also will help with making improving coverage easier.
 */
public class ClientRunner {
	public static void main(String[] args) throws IOException {
		enable();
		Pair<String, Integer> hostAndPortPair = promptForHostPortTuple();
		Client mainClient = new Client(hostAndPortPair.getValue0(), hostAndPortPair.getValue1());
		mainClient.usedWithConsole = true;
		mainClient.init();
		mainClient.begin();
	}
}
