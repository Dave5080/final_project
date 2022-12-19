package io.github.sdamico12.wordle.server.connections;

import io.github.sdamico12.wordle.server.config.CEntries;
import io.github.sdamico12.wordle.server.config.ConfigManager;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionManager {
	private static ConnectionManager instance = null;
	private final int tp_size;
	private final ConnectionBlockHandler[] handlers;
	private final ExecutorService tp;
	public static ConnectionManager getManager() throws IOException {
		return instance == null ? (instance = new ConnectionManager()) : instance;
	}

	private ConnectionManager() throws IOException {
		tp_size = ConfigManager.getManager().getIntVal(CEntries.THREAD_POOL_SIZE);
		tp = Executors.newFixedThreadPool(tp_size);
		handlers = new ConnectionBlockHandler[tp_size];
		for(int i = 0; i < tp_size; i++){
			handlers[i] = new ConnectionBlockHandler();
			tp.execute(handlers[i]);
		}
	}

	private ConnectionBlockHandler getMinConnectionBlock(){
		ConnectionBlockHandler min = handlers[0];
		for(int i = 1; i < tp_size; i++)
			if(handlers[i].getCurrentConnections() < min.getCurrentConnections()) min = handlers[i];
		return min;
	}

	public void submitWordleConnection(SocketChannel channel) throws ClosedChannelException {
		getMinConnectionBlock().submitWordleConnection(channel);
	}

	public void submitStatsConnection(SocketChannel channel) throws ClosedChannelException {
		getMinConnectionBlock().submitStatsConnection(channel);
	}

	public void shutdown(){
		for(ConnectionBlockHandler h : handlers)
			h.shutdown();
		tp.shutdown();

	}


}
