package io.github.incplusplus.peerprocessing.common;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Classes that implement this interface are personable and will introduce themselves when asked by
 * a server.
 */
public interface Personable {
  /**
   * Introduces this {@linkplain Personable} object to a server.
   *
   * @throws JsonProcessingException if there was an exception encountered while performing the JSON
   *     processing required of this method.
   */
  void introduce() throws JsonProcessingException;
}
