package io.github.sdamico12.wordle.server.game_engine;

import io.github.sdamico12.wordle.server.account.Account;

import java.awt.dnd.DropTarget;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Broadcaster implements Runnable{
    private List<DatagramChannel> open_connections = new ArrayList<>();
    private BlockingQueue<Entry<Account,List<SubmittedTryResult>>> queued_games = new LinkedBlockingQueue<>();

    private Selector selector;

    public Broadcaster() throws IOException {
        selector = Selector.open();
    }

    public synchronized void registerConnection(InetAddress addr, int port) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        socket.connect(addr, port);
        DatagramChannel channel = socket.getChannel();
        open_connections.add(channel);
        channel.register(selector, SelectionKey.OP_WRITE,false);
    }

    public void registerConnection(SocketChannel channel, int port) throws IOException{
        registerConnection(channel.socket().getInetAddress(), port);
    }

    public synchronized void unregisterConnection(InetAddress addr) throws IOException {
        for(DatagramChannel ch: open_connections)
            if(ch.socket().getInetAddress().equals(addr)){
                ch.close();
                open_connections.remove(ch);
            }
    }

    public void shareGame(Account account, List<SubmittedTryResult> game) throws InterruptedException {
        queued_games.put(new SimpleEntry<>(account,game));
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                broadcast();
            } catch (InterruptedException | IOException ignored) {}
        }
    }

    private void broadcast() throws InterruptedException, IOException {
        Entry<Account, List<SubmittedTryResult>> entry = queued_games.take();
        synchronized (this) {
            Set<DatagramChannel> remainingBroadcast = new HashSet<>(open_connections);
            while (remainingBroadcast.size() > 0){
                selector.select();
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()){
                    SelectionKey key = it.next();
                    DatagramChannel channel = (DatagramChannel) key.channel();
                    if(remainingBroadcast.contains(channel) && key.isWritable()){
                        writeGame(channel, entry.getKey(), entry.getValue());
                        remainingBroadcast.remove(channel);
                    }
                    it.remove();
                }
            }
        }
    }

    private void writeGame(DatagramChannel udp_channel, Account account, List<SubmittedTryResult> game) throws IOException {
        ByteBuffer userPacket = ByteBuffer.allocate(257);
        byte[] username = account.getUsername().getBytes();
        userPacket.put((byte) username.length);
        userPacket.put((byte) game.size());
        userPacket.put(username);
        udp_channel.write(userPacket);
        ByteBuffer recordPacket = ByteBuffer.allocate(12);
        for(SubmittedTryResult record : game){
            recordPacket.reset();
            recordPacket.put(record.toBytes());
            udp_channel.write(recordPacket);
        }

    }
}
