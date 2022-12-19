package io.github.sdamico12.wordle.server.connections.states.wordlestates;

import io.github.sdamico12.wordle.server.connections.states.ConnectionState;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class WordleHandshakeState implements ConnectionState {
	@Override
	public ConnectionState execute(SocketChannel channel) throws IOException {
		byte[] b = new byte[1];
		b[0] = 1;
		channel.write(ByteBuffer.wrap(b));
		return new WordleLoginState();
	}

	@Override
	public int getInterestOps() {
		return SelectionKey.OP_WRITE;
	}
}
