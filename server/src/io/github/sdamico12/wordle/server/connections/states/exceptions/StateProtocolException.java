package io.github.sdamico12.wordle.server.connections.states.exceptions;

import io.github.sdamico12.wordle.server.connections.states.ConnectionState;

public class StateProtocolException extends Exception{
	private final ConnectionState exceptionalState;

	public StateProtocolException(ConnectionState exceptionalState){
		this.exceptionalState = exceptionalState;
	}
	public ConnectionState getExceptionalState(){
		return exceptionalState;
	}

}
