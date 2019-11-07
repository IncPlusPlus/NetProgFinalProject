package io.github.incplusplus.peerprocessing;

import org.junit.jupiter.api.Test;

import static io.github.incplusplus.peerprocessing.ClientIT.VERBOSE_TEST_OUTPUT;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SanityCheckTest {
	@Test
	void sanityCheck() {
		assertEquals(1, 1);
		if (VERBOSE_TEST_OUTPUT)
			System.out.println("Sanity check passed! You're sane! Congrats!");
	}
}
