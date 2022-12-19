package io.github.sdamico12.wordle.server.connections;

import io.github.sdamico12.wordle.server.connections.states.ConnectionState;
import io.github.sdamico12.wordle.server.connections.states.exceptions.AbortionState;
import io.github.sdamico12.wordle.server.connections.states.exceptions.StateProtocolException;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class ConnectionHandler {

	private final SocketChannel channel;

	private ConnectionState state;

	public ConnectionHandler(SocketChannel channel, ConnectionState initialState){
		this.channel = channel;
		this.state = initialState;
	}

	public int handle(){
		try {
			this.state = this.state.execute(channel);
		} catch (StateProtocolException e) {
			this.state = e.getExceptionalState();
		} catch (IOException e) {
			this.state = new AbortionState();
		}
		return state.getInterestOps();
	}

}
