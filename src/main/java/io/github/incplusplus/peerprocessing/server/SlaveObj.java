package io.github.incplusplus.peerprocessing.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.incplusplus.peerprocessing.common.Header;
import io.github.incplusplus.peerprocessing.common.Job;
import io.github.incplusplus.peerprocessing.common.Query;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.github.incplusplus.peerprocessing.common.Constants.SHARED_MAPPER;
import static io.github.incplusplus.peerprocessing.common.Demands.*;
import static io.github.incplusplus.peerprocessing.common.MiscUtils.*;
import static io.github.incplusplus.peerprocessing.common.Responses.*;
import static io.github.incplusplus.peerprocessing.common.StupidSimpleLogger.debug;
import static io.github.incplusplus.peerprocessing.common.StupidSimpleLogger.printStackTrace;
import static io.github.incplusplus.peerprocessing.common.VariousEnums.DISCONNECT;
import static io.github.incplusplus.peerprocessing.server.Server.*;

public class SlaveObj extends ConnectedEntity {
	private List<UUID> jobsResponsibleFor = new ArrayList<>();
	
	public SlaveObj(PrintWriter outToClient, BufferedReader inFromClient, Socket socket,
	                UUID connectionUUID) {super(outToClient, inFromClient, socket, connectionUUID);}
	
	/**
	 * When an object implementing interface <code>Runnable</code> is used
	 * to create a thread, starting the thread causes the object's
	 * <code>run</code> method to be called in that separately executing
	 * thread.
	 * <p>
	 * The general contract of the method <code>run</code> is that it may
	 * take any action whatsoever.
	 *
	 * @see Thread#run()
	 */
	@Override
	public void run() {
		String lineFromSlave;
		while (!getSocket().isClosed()) {
			try {
				lineFromSlave = getInFromClient().readLine();
				Header header = getHeader(lineFromSlave);
				if (header.equals(RESULT)) {
					Query completedQuery = SHARED_MAPPER.readValue(decode(lineFromSlave), Query.class);
					Query storedQuery = removeJob(completedQuery.getQueryId());
					//We keep the originally created query object and only take what we need from the
					//slave's data. This is to prevent possibly malicious slaves from compromising
					//our good and pure clients who can do nothing wrong.
					storedQuery.setQueryState(QueryState.COMPLETE);
					storedQuery.setReasonIncomplete(completedQuery.getReasonIncomplete());
					storedQuery.setResult(completedQuery.getResult());
					storedQuery.setCompleted(true);
					relayToAppropriateClient(storedQuery);
					jobsResponsibleFor.remove(storedQuery.getQueryId());
				}
				else if (header.equals(IDENTIFY)) {
					getOutToClient().println(
							msg(SHARED_MAPPER.writeValueAsString(provideIntroductionFromServer()), IDENTITY));
				}
				else if (header.equals(DISCONNECT)) {
					deRegister(this);
					//the client already is ending their connection.
					//we don't want to write back
					kill();
					break;
				}
			}
			catch (SocketException e) {
				debug("Slave " + getConnectionUUID() + " disconnected.");
				deRegister(this);
				try {
					getSocket().close();
				}
				catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			catch (IOException e) {
				printStackTrace(e);
			}
		}
	}
	
	/**
	 * Send a query to this slave for processing.
	 *
	 * @param query the query to send
	 * @throws JsonProcessingException if something goes horribly wrong
	 */
	void accept(Query query) throws JsonProcessingException {
		query.setQueryState(QueryState.WAITING_ON_SLAVE);
		jobsResponsibleFor.add(query.getQueryId());
		debug("Slave " + getConnectionUUID() + " now responsible for " + query.getQueryId());
		getOutToClient().println(msg(SHARED_MAPPER.writeValueAsString(query), QUERY));
	}
	
	/**
	 * @return the list of UUIDs representing jobs that this slave is currently
	 * responsible for. Useful for recovering a job if a slave suddenly disconnects.
	 */
	public List<UUID> getJobsResponsibleFor() {
		return jobsResponsibleFor;
	}
}
