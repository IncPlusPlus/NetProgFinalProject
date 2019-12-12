package io.github.incplusplus.peerprocessing.server;

import static io.github.incplusplus.peerprocessing.common.MiscUtils.*;
import static io.github.incplusplus.peerprocessing.logger.StupidSimpleLogger.debug;

import io.github.incplusplus.peerprocessing.common.Demands;
import io.github.incplusplus.peerprocessing.common.Responses;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/** A collection of methods that I moved to here to keep {@link Server} concise. */
class ServerMethods {
  /**
   * Continuously prompts a client to supply a certain value. Specifically, this demands a certain
   * type of payload from a client denoted by some specific header. This method is used with the
   * following interaction in mind 1. Server sends payload containing only a header 2. Client sends
   * back response with corresponding header AND PAYLOAD
   *
   * @param serverDemand the header to send to the client which would indicate the expected response
   * @param properResponse the correct response header
   * @param outToClient a {@link PrintWriter} to the client
   * @param inFromClient a {@link BufferedReader} from the client
   * @return the string content of the reply from the client
   * @throws IOException if an IO error occurs during negotiation
   */
  static String negotiate(
      Demands serverDemand,
      Responses properResponse,
      PrintWriter outToClient,
      BufferedReader inFromClient)
      throws IOException {
    // tell the client what we want
    outToClient.println(serverDemand);
    // ingest client response
    String clientResponse = inFromClient.readLine();
    if (!expected(serverDemand, properResponse, clientResponse)) {
      return negotiate(serverDemand, properResponse, outToClient, inFromClient);
    } else return decode(clientResponse);
  }

  /**
   * Utility method to safely determine if we received back the expected header.
   *
   * @return whether the right header was sent back
   */
  private static boolean expected(
      Demands serverDemand, Responses properResponse, String clientResponse) {
    String clientHeader = clientResponse;
    if (clientResponse == null) {
      logFailedExpectations(serverDemand, properResponse, null);
      return false;
    } else {
      try {
        clientHeader = String.valueOf(getHeader(clientResponse));
        return true;
      } catch (IllegalArgumentException e) {
        logFailedExpectations(serverDemand, properResponse, clientHeader);
        return false;
      }
    }
  }

  private static void logFailedExpectations(
      Demands serverDemand, Responses properResponse, String actual) {
    debug(
        "Expected '"
            + properResponse
            + "' for demand '"
            + serverDemand
            + "' but got '"
            + actual
            + "' instead.");
  }
}
