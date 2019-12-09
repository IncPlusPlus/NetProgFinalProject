package io.github.incplusplus.peerprocessing.server;

import static io.github.incplusplus.peerprocessing.common.MiscUtils.getIp;
import static io.github.incplusplus.peerprocessing.logger.StupidSimpleLogger.*;
import static io.github.incplusplus.peerprocessing.server.Server.serverName;

import io.github.incplusplus.peerprocessing.logger.StupidSimpleLogger;
import java.io.IOException;
import java.util.Scanner;

/** A class to start an instance of a {@linkplain Server}. */
class ServerRunner {
  /**
   * @param args optional. Enter some integer for a custom start port
   * @throws IOException if any exceptions occur
   */
  public static void main(String[] args) throws IOException {
    Scanner in = new Scanner(System.in);
    // Set up my custom logging implementation
    StupidSimpleLogger.enable();
    Server myServer = new Server();
    int initPort = 0;
    try {
      if (args.length > 0) initPort = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      printStackTrace(e);
      error("Invalid port provided in args. Binding to whatever's available.");
    }
    int port = myServer.start(initPort, true);
    //noinspection ConstantConditions because we may eventually use serverName
    if (serverName != null) info("Server name: " + serverName);
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
