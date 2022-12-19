package io.github.sdamico12.wordle.server.account;

import io.github.sdamico12.wordle.server.config.CEntries;
import io.github.sdamico12.wordle.server.config.ConfigManager;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;


public class AccountManager {

	private static AccountManager instance = null;

	public static AccountManager getManager() throws IOException {
		return instance == null ? (instance = new AccountManager()) : instance;
	}
	private final ConfigManager configManager;
	private Map<Integer,AccountBlock> blockMap = new LinkedHashMap<>();
	private AccountManager() throws IOException {
		this.configManager = ConfigManager.getManager();
		for(int i = 0; i < configManager.getIntVal(CEntries.USER_PARTITION_SIZE); i++)
			blockMap.put(i, new AccountBlock(i));
	}

	public static byte[] hash_password(byte[] password){
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		return md.digest(password);
	}
	public static byte[] hash_password(String password){
		return hash_password(password.getBytes());
	}

	public Optional<Account> login(String username, String password) {
		Optional<Account> a = blockMap.get(username.hashCode()%configManager.getIntVal(CEntries.USER_PARTITION_SIZE)).get(username);
		if(a.isEmpty() || a.get().getHashedPassword() != hash_password(password)) return Optional.empty();
		return a;
	}

	public Optional<Account> register(String username, String password) throws IOException {
		Account a = new Account(username, hash_password(password));
		if(blockMap.get(a.hashCode()).add(a)) return Optional.of(a);
		return Optional.empty();
	}

	public boolean unregister(String username, String password) throws IOException {
		Optional<Account> a = login(username,password);
		if(a.isPresent()) return blockMap.get(a.get().hashCode()).remove(a.get());
		return false;
	}




}
