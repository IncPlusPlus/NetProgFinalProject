package io.github.incplusplus.peerprocessing.client;

import io.github.incplusplus.peerprocessing.common.AlgebraicQuery;

import java.util.Arrays;

import static io.github.incplusplus.peerprocessing.logger.StupidSimpleLogger.debug;

class ConsoleUtils {
	static void printSolution(AlgebraicQuery query) {
		assert query.isCompleted();
		if (query.getReasonIncomplete() == null) {
			debug("The solution for the problem \"" + query.getQueryString() + "\" is: \"" + query.getResult() + "\"");
		}
		else {
			debug("The solution for the problem \"" + query.getQueryString() + "\" could not be found.");
			debug("The reason for this is: " + query.getReasonIncomplete().toString());
			debug("Stacktrace: \n" + Arrays.toString(query.getReasonIncomplete().getStackTrace()));
		}
	}
}
