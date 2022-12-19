package io.github.sdamico12.wordle.server.config;

import java.io.IOException;
import java.util.function.Predicate;

public enum CEntries {


	WORDLE_PORT("wordle_port", "9999", x -> x.configManager.getIntVal(x) > 1024 &&
													  x.configManager.getIntVal(x) < 65000),
	STATS_PORT("stats_port", "9998", x -> x.configManager.getIntVal(x) > 1024 &&
													x.configManager.getIntVal(x) < 65000 &&
													x.configManager.getIntVal(x) != WORDLE_PORT.configManager.getIntVal(WORDLE_PORT)),
	THREAD_POOL_SIZE("thread_pool_size", "64", x -> x.configManager.getIntVal(x) > 0),
	USERS_DATA_PATH("users_data_path", "users", x -> x.configManager.getVal(x).length() > 0),
	USER_PARTITION_SIZE("user_partition_size","30",x -> x.configManager.getIntVal(x) > 0),
	NEW_WORD_INTERVAL("new_word_interval", String.format("%d", 24*60*60), x -> x.configManager.getIntVal(x) > 0);

	private final String name;
	private final String def;
	private Predicate<CEntries> p;

	private ConfigManager configManager;

	CEntries(String name, String def, Predicate<CEntries> p){
		this.p = p;
		this.name = name;
		this.def = def;
	}

	public String getName() {
		return name;
	}

	public String getDef() {
		return def;
	}

	public boolean checkConfigCondition(ConfigManager configManager) {
		this.configManager = configManager;
		return p.test(this);
	}
}
