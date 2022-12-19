package io.github.sdamico12.wordle.server.account;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.github.sdamico12.wordle.server.config.CEntries;
import io.github.sdamico12.wordle.server.config.ConfigManager;

import java.io.IOException;
import java.util.Optional;

public class Account {
	private final String username;
	private final byte[] hashedPassword;

	private int playedGames;
	private int wonGames;
	private int currentWinStreak;
	private int longestWinStreak;

	private ConfigManager configManager;

	public Account(String username, byte[] hashedPassword) throws IOException {
		this(username,hashedPassword,0,0,0,0);

	}
	public Account(String username, byte[] hashedPassword, int playedGames, int wonGames, int currentWinStreak, int longestWinStreak) throws IOException {
		this.username = username;
		this.hashedPassword = hashedPassword;
		this.wonGames = wonGames;
		this.playedGames = playedGames;
		this.currentWinStreak = currentWinStreak;
		this.longestWinStreak = longestWinStreak;
		configManager = ConfigManager.getManager();
	}

	public String getUsername(){
		return username;
	}

	public byte[] getHashedPassword() {
		return hashedPassword;
	}

	public double getWinRate() {
		if(playedGames == 0) return 0;
		return (double) wonGames / (double) playedGames;
	}

	public int getPlayedGames() {
		return playedGames;
	}

	public int getWonGames() {
		return wonGames;
	}

	public int getCurrentWinStreak() {
		return currentWinStreak;
	}

	public int getLongestWinStreak() {
		return longestWinStreak;
	}

	public void addGameLost(){
		this.playedGames++;
		this.currentWinStreak = 0;
	}

	public void addGameWon(){
		this.playedGames++;
		this.wonGames++;
		if(longestWinStreak == currentWinStreak) longestWinStreak++;
		currentWinStreak++;

	}

	@Override
	public int hashCode(){
		return username.hashCode() % configManager.getIntVal(CEntries.USER_PARTITION_SIZE);
	}
}
