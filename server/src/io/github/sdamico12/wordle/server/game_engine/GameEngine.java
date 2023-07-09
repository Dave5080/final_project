package io.github.sdamico12.wordle.server.game_engine;

import io.github.sdamico12.wordle.server.account.Account;
import io.github.sdamico12.wordle.server.account.AccountManager;
import io.github.sdamico12.wordle.server.config.CEntries;
import io.github.sdamico12.wordle.server.config.ConfigManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class GameEngine {
	private static GameEngine engine = null;
	public static GameEngine getEngine() throws IOException {
		return engine == null ? (engine = new GameEngine()) : engine;
	}

	private final ConcurrentMap<Account, List<Optional<SubmittedTryResult>>> attemptsMap;
	private String currentWord = null;
	private final List<String> vocabulary;

	private Broadcaster broadcaster;

	private GameEngine() throws IOException {
		configManager = ConfigManager.getManager();
		accountManager = AccountManager.getManager();
		task = new WordUpdateTask();
		service = Executors.newSingleThreadScheduledExecutor();
		attemptsMap = new ConcurrentHashMap<>();
		vocabulary = new ArrayList<>();
		broadcaster = new Broadcaster();
		Scanner vocScan = new Scanner(new FileInputStream("vocabulary.txt"));
		while (vocScan.hasNextLine())
			vocabulary.add(vocScan.nextLine());
	}

	public void start(){
		service.scheduleAtFixedRate(task, 0, configManager.getIntVal(CEntries.NEW_WORD_INTERVAL), TimeUnit.SECONDS);
	}


	public synchronized SubmittedTryResult submitTry(Account loggedAccount, String guessedWord){
		boolean userCouldPlay = countValidAttempts(loggedAccount) < 12,
				wordIsValid = vocabulary.contains(guessedWord);
		Optional<String> hint = userCouldPlay && wordIsValid ? Optional.of(genHint(guessedWord)) : Optional.empty();
		SubmittedTryResult currentAttempt = new SubmittedTryResult(hint,userCouldPlay, wordIsValid);
		if (currentAttempt.getHint().isPresent())
			getAccountAttempts(loggedAccount).add(Optional.of(currentAttempt));
		else getAccountAttempts(loggedAccount).add(Optional.empty());
		return currentAttempt;
	}

	public List<Optional<SubmittedTryResult>> getAccountAttempts(Account account){
		return attemptsMap.merge(account,new LinkedList<>(),(x,y) -> x);
	}

	public int countValidAttempts(Account account){
		List<Optional<SubmittedTryResult>> attempts = getAccountAttempts(account);
		int c = 0;
		for(Optional<SubmittedTryResult> o : attempts)
			if(o.isPresent()) c++;
		return c;

	}

	public String genHint(String guessedWord){
		StringBuilder hintBuilder = new StringBuilder();
		for(int i = 0; i < 10; i++){
			char c = guessedWord.charAt(i);
			if(c == currentWord.charAt(i)) hintBuilder.append('V');
			else if(currentWord.indexOf(c) != -1) hintBuilder.append('Q');
			else  hintBuilder.append('X');
		}
		return hintBuilder.toString();
	}

	public synchronized void updateWord(String newWord){
		currentWord = newWord;
		attemptsMap.clear();
	}

	public synchronized void shareAttempts(Account sharingAccount){
		List<SubmittedTryResult> attempts = getAccountAttempts(sharingAccount).stream().filter(Optional::isPresent).map(Optional::get).toList();
	}

	public Broadcaster getBroadcaster(){
		return broadcaster;
	}

	private final ConfigManager configManager;
	private final AccountManager accountManager;
	private final ScheduledExecutorService service;
	private final WordUpdateTask task;
}
