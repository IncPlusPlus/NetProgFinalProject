package io.github.incplusplus.peerprocessing.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.incplusplus.peerprocessing.common.Header;
import io.github.incplusplus.peerprocessing.common.Job;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;

import static io.github.incplusplus.peerprocessing.common.Constants.SHARED_MAPPER;
import static io.github.incplusplus.peerprocessing.common.Demands.*;
import static io.github.incplusplus.peerprocessing.common.MiscUtils.*;
import static io.github.incplusplus.peerprocessing.common.Responses.SOLUTION;
import static io.github.incplusplus.peerprocessing.common.StupidSimpleLogger.printStackTrace;
import static io.github.incplusplus.peerprocessing.server.Server.*;

public class SlaveObj extends ConnectedEntity {
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
				if (header.equals(SOLUTION)) {
					Job completedJob = SHARED_MAPPER.readValue(decode(lineFromSlave), Job.class);
					Job storedJob = removeJob(completedJob.getJobId());
					//We keep the originally created job object and only take what we need from the
					//slave's data. This is to prevent possibly malicious slaves from compromising
					//our good and pure clients who can do nothing wrong.
					storedJob.setJobState(JobState.COMPLETE);
					storedJob.getMathQuery().setReasonUnsolved(completedJob.getMathQuery().getReasonUnsolved());
					storedJob.getMathQuery().setResult(completedJob.getMathQuery().getResult());
					storedJob.getMathQuery().setSolved(completedJob.getMathQuery().isSolved());
					relayToAppropriateClient(storedJob);
				}
			}
			catch (SocketException e) {
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
	 * Send a job to this slave for processing.
	 *
	 * @param job the job to send
	 * @throws JsonProcessingException if something goes horribly wrong
	 */
	void accept(Job job) throws JsonProcessingException {
		job.setJobState(JobState.WAITING_ON_SLAVE);
		getOutToClient().println(msg(SHARED_MAPPER.writeValueAsString(job), SOLVE));
	}
}
