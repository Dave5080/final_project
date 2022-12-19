package io.github.sdamico12.wordle.server.connections.states.exceptions;

import io.github.sdamico12.wordle.server.connections.states.ConnectionState;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class AbortionState implements ConnectionState {
	@Override
	public ConnectionState execute(SocketChannel channel) throws IOException {
		channel.write(ByteBuffer.wrap(new byte[]{-1}));
		return this;
	}

	@Override
	public int getInterestOps() {
		return SelectionKey.OP_WRITE;
	}
}
