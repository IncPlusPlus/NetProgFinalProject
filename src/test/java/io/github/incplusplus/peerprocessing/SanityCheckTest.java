package io.github.incplusplus.peerprocessing;

import static io.github.incplusplus.peerprocessing.NormalIT.VERBOSE_TEST_OUTPUT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(value = 2)
class SanityCheckTest {
  @Test
  void sanityCheck() {
    assertEquals(1, 1);
    if (VERBOSE_TEST_OUTPUT) System.out.println("Sanity check passed! You're sane! Congrats!");
  }
}
