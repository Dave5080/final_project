package io.github.sdamico12.wordle.server.connections.states.exceptions;

import io.github.sdamico12.wordle.server.connections.states.ConnectionState;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class NotifyState implements ConnectionState {

	public static final int SUCCESS = 1;
	public final static int ERROR = 0;
	public static final int FATAL = -1;
	private final int code;
	private final String message;
	private final ConnectionState recoverState;

	public NotifyState(int code, String message, ConnectionState recoverState){
		this.code = code;
		this.message = message;
		this.recoverState = recoverState;
	}


	@Override
	public ConnectionState execute(SocketChannel channel) throws IOException {
		byte[] code = new byte[1];
		code[0] = (byte)this.code;
		channel.write(ByteBuffer.wrap(code));
		channel.write(ByteBuffer.wrap(message.getBytes()));
		return recoverState;
	}

	@Override
	public int getInterestOps() {
		return SelectionKey.OP_WRITE;
	}

}
