package io.github.sdamico12.wordle.server.connections;

import io.github.sdamico12.wordle.server.connections.states.ConnectionState;
import io.github.sdamico12.wordle.server.connections.states.wordlestates.WordleHandshakeState;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("ALL")
public class ConnectionBlockHandler implements Runnable{

	private final AtomicInteger currentConnections;
	private final Selector selector;

	private Thread currentThread = null;

	public ConnectionBlockHandler() throws IOException {
		currentConnections = new AtomicInteger(0);
		selector = Selector.open();
	}

	public void submitWordleConnection(SocketChannel connection) throws ClosedChannelException {
		currentConnections.incrementAndGet();
		ConnectionState state = new WordleHandshakeState();
		connection.register(selector, state.getInterestOps(), new ConnectionHandler(connection, state));
	}

	public void submitStatsConnection(SocketChannel connection) throws ClosedChannelException{
		currentConnections.incrementAndGet();
		ConnectionState initialState = new WordleHandshakeState();
		connection.register(selector, initialState.getInterestOps(),new ConnectionHandler(connection, initialState));
	}

	public int getCurrentConnections(){
		return currentConnections.get();
	}

	@Override
	public void run() {
		currentThread = Thread.currentThread();
		while (!Thread.currentThread().isInterrupted()){
			try {
				selector.select();
				Iterator<SelectionKey> it = selector.selectedKeys().iterator();
				while (it.hasNext()){
					SelectionKey k = it.next();
					ConnectionHandler handler = (ConnectionHandler) k.attachment();
					k.interestOps(handler.handle());
					it.remove();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void shutdown(){
		currentThread.interrupt();
	}
}
