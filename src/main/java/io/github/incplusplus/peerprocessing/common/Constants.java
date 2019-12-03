package io.github.incplusplus.peerprocessing.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

/** A class of constants that the server and/or the client understand */
public class Constants {
  /**
   * This is the character that separates the beginning header from the payload of the message. The
   * header is what tells the server or client what the interaction is regarding.
   */
  //  :) I love the bell character
  static final char HEADER_SEPARATOR = (char) 7;
  /**
   * This is the string that will be typed into the client by a user to tell the client to
   * disconnect itself from the server.
   */
  public static final String QUIT_STRING = "/q";

  public static final ObjectMapper SHARED_MAPPER =
      new ObjectMapper().activateDefaultTyping(BasicPolymorphicTypeValidator.builder().build());
}
