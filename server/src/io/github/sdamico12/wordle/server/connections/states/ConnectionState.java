package io.github.sdamico12.wordle.server.connections.states;

import io.github.sdamico12.wordle.server.connections.states.exceptions.StateProtocolException;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface ConnectionState {


	ConnectionState execute(SocketChannel channel) throws StateProtocolException, IOException;

	int getInterestOps();



	/*int getStateId();
	default ConnectionState executeUntilLoop(SocketChannel channel) throws StateProtocolException, IOException {
		ConnectionState state = this;
		while ((state = state.execute(channel)).getStateId() != this.getStateId());
		return state;
	}
	default void executeRecursive(SocketChannel channel) throws StateProtocolException, IOException {
		ConnectionState state = this;
		while ((state = state.execute(channel)) != null);
	}*/

}
