package io.github.sdamico12.wordle.server;

import static io.github.sdamico12.wordle.server.config.CEntries.*;
import io.github.sdamico12.wordle.server.config.ConfigManager;
import io.github.sdamico12.wordle.server.connections.ConnectionManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class ServerMain {

	private static ServerSocketChannel wordleListener ,statsListener;
	private static Thread mainThread;

	public static void main(String... args) throws IOException {

		mainThread = Thread.currentThread();

		wordleListener = ServerSocketChannel.open();
		statsListener = ServerSocketChannel.open();

		ConfigManager configManager = ConfigManager.getManager();
		ConnectionManager connectionManager = ConnectionManager.getManager();

		wordleListener.bind(new InetSocketAddress(configManager.getIntVal(WORDLE_PORT)));
		statsListener.bind(new InetSocketAddress(configManager.getIntVal(STATS_PORT)));

		Selector selector = Selector.open();

		wordleListener.register(selector, SelectionKey.OP_ACCEPT, true);
		statsListener.register(selector, SelectionKey.OP_ACCEPT, false);

		getNewInterrupter().start();

		while (!Thread.currentThread().isInterrupted()){
			selector.select();
			Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			while (it.hasNext()){
				SelectionKey k = it.next();
				if((Boolean) k.attachment()) connectionManager.submitWordleConnection((SocketChannel) k.channel());
				else connectionManager.submitStatsConnection((SocketChannel) k.channel());
				System.out.println("Gawain27 was here");
				it.remove();
			}
		}

		connectionManager.shutdown();
	}

	public static Thread getNewInterrupter(){
		return new Thread(() -> {
			try {
				System.in.read();
				mainThread.interrupt();
				wordleListener.close();
				statsListener.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}
}
