package io.github.sdamico12.wordle.server.connections.states.wordlestates;

import io.github.sdamico12.wordle.server.connections.states.ConnectionState;
import io.github.sdamico12.wordle.server.game_engine.GameEngine;

import java.io.IOException;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class WordleHandshakeState implements ConnectionState {
	@Override
	public ConnectionState execute(SocketChannel channel) throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(2);
		channel.read(buf);
		GameEngine.getEngine().getBroadcaster().registerConnection(channel, (buf.get(0)<<8)+buf.get(1));
		return new WordleLoginState();
	}

	@Override
	public int getInterestOps() {
		return SelectionKey.OP_READ;
	}
}
