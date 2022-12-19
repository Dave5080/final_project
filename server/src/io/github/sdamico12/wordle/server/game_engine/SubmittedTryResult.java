package io.github.sdamico12.wordle.server.game_engine;

import java.util.Optional;

public class SubmittedTryResult {
	private final Optional<String> hint;
	private final boolean user_could_play;
	private final boolean word_was_valid;

	public SubmittedTryResult(Optional<String> hint, boolean user_could_play, boolean wod_was_valid){
		this.hint = hint;
		this.user_could_play = user_could_play;
		this.word_was_valid = wod_was_valid;
	}

	public Optional<String> getHint() {
		return hint;
	}

	public boolean accountCouldPlay() {
		return user_could_play;
	}

	public boolean wordWasValid() {
		return word_was_valid;
	}
}
