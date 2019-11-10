package io.github.incplusplus.peerprocessing.server;

import io.github.incplusplus.peerprocessing.common.StupidSimpleLogger;

import java.io.IOException;
import java.util.Scanner;

import static io.github.incplusplus.peerprocessing.common.MiscUtils.getIp;
import static io.github.incplusplus.peerprocessing.common.StupidSimpleLogger.debug;
import static io.github.incplusplus.peerprocessing.common.StupidSimpleLogger.info;
import static io.github.incplusplus.peerprocessing.server.Server.serverName;

public class ServerRunner {
	public static void main(String[] args) throws IOException {
		Scanner in = new Scanner(System.in);
		//Set up my custom logging implementation
		StupidSimpleLogger.enable();
		Server myServer = new Server();
		int port = myServer.start(0,true);
		if (serverName != null)
			info("Server name: " + serverName);
//		start(port);
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
		myServer.stop();
		debug("Server stopped.");
	}
}
