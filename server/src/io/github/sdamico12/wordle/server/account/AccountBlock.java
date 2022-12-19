package io.github.sdamico12.wordle.server.account;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.github.sdamico12.wordle.server.config.CEntries;
import io.github.sdamico12.wordle.server.config.ConfigManager;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class AccountBlock {
	private final int blockID;
	private final List<Account> accounts;
	private final File file;

	@SuppressWarnings("SpellCheckingInspection")
	public AccountBlock(int id) throws IOException {
		this.blockID = id;
		ConfigManager configManager = ConfigManager.getManager();
		accounts = loadFromFile();
		file = new File(String.format("%s%sblock%d.json", configManager.getVal(CEntries.USERS_DATA_PATH), File.separator, blockID));
		if(!file.getParentFile().exists()) if(!file.getParentFile().mkdirs()) throw new IOException("Cannot create parent directory");
		if(!file.exists()) if(!file.createNewFile()) throw new IOException("Cannot create file " + blockID);
	}

	public synchronized Optional<Account> get(String username){
		if(accounts == null) throw new IllegalStateException("AccountBlock not loaded yet");
		return accounts.stream().filter(x -> x.getUsername().equals(username)).findFirst();
	}

	public synchronized boolean add(Account account) throws IOException {
		if(account.hashCode() != blockID) throw new IllegalArgumentException("Account ended up in wrong block");
		if(accounts == null) throw new IllegalStateException("AccountBlock not loaded yet");
		if(accounts.contains(account)) return false;
		accounts.add(account);
		updateFile();
		return true;
	}

	public synchronized boolean remove(Account account) throws IOException {
		if(accounts == null) throw new IllegalStateException("AccountBlock not loaded yet");
		if(!accounts.contains(account)) return false;
		accounts.remove(account);
		updateFile();
		return true;
	}

	private void updateFile() throws IOException {
		JsonWriter writer = new JsonWriter(new FileWriter(file,false));
		writer.beginArray();
		for(Account a : accounts){
			writer.beginObject();
			writer.name("username").value(a.getUsername());
			writer.name("hash_pass").value(new String(a.getHashedPassword()));
			writer.name("played_games").value(a.getPlayedGames());
			writer.name("won_games").value(a.getWonGames());
			writer.name("current_streak").value(a.getCurrentWinStreak());
			writer.name("longest_streak").value(a.getLongestWinStreak());
			writer.endObject();
		}
		writer.endArray();
		writer.flush();
		writer.close();
	}

	private List<Account> loadFromFile() throws IOException {
		JsonReader reader = new JsonReader(new FileReader(file));
		List<Account> list = new LinkedList<>();
		reader.beginArray();
		while (reader.hasNext()) {
			reader.beginObject();
			reader.nextName();
			String username = reader.nextString();
			reader.nextName();
			byte[] hashed = reader.nextString().getBytes();
			reader.nextName();
			int playedGames = reader.nextInt();
			reader.nextName();
			int wonGames = reader.nextInt();
			reader.nextName();
			int currentWinStreak = reader.nextInt();
			reader.nextName();
			int longestWinStreak = reader.nextInt();
			reader.endObject();

			list.add(new Account(username, hashed, playedGames, wonGames, currentWinStreak, longestWinStreak));
		}
		reader.endArray();

		reader.close();
		return list;
	}
}
