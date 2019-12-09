package io.github.incplusplus.peerprocessing.common;

/** This enum contains values that are processed by the server and/or client */
public enum VariousEnums implements Header {
  /**
   * This tells the server to kill off the client connections. Also works vice versa where this is
   * sent by the server telling the client to disconnect.
   */
  DISCONNECT
}
