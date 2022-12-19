package io.github.sdamico12.wordle.server.connections.states.wordlestates;

import io.github.sdamico12.wordle.server.account.Account;
import io.github.sdamico12.wordle.server.connections.states.ConnectionState;
import io.github.sdamico12.wordle.server.connections.states.exceptions.AbortionState;
import io.github.sdamico12.wordle.server.connections.states.exceptions.NotifyState;
import io.github.sdamico12.wordle.server.connections.states.exceptions.StateProtocolException;
import io.github.sdamico12.wordle.server.game_engine.GameEngine;
import io.github.sdamico12.wordle.server.game_engine.SubmittedTryResult;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class WordleLobbyState implements ConnectionState {

	private final Account loggedAccount;
	private final ByteBuffer buf;

	private final GameEngine engine;

	public WordleLobbyState(Account loggedAccount) throws IOException {
		this.loggedAccount = loggedAccount;
		buf = ByteBuffer.allocate(11);
		engine = GameEngine.getEngine();
	}

	@Override
	public ConnectionState execute(SocketChannel channel) throws StateProtocolException, IOException {
		if(buf.position() < buf.limit()){
			channel.read(buf);
			return this;
		}
		byte[] input = buf.array();
		buf.clear();
		byte req = input[0];
		String guessed_word = new String(Arrays.copyOfRange(input,1,11));
		buf.clear();
		switch (req){
			case 0:
				SubmittedTryResult result = engine.submitTry(loggedAccount, guessed_word);
				if(!result.accountCouldPlay())
					return new NotifyState(NotifyState.FATAL, "You have already played for this word\n", this);
				if(!result.wordWasValid())
					return new NotifyState(NotifyState.FATAL,"Your word is not present in vocabulary\n", this);
				if(result.getHint().isEmpty())
					return new NotifyState(NotifyState.SUCCESS, "Congratulation! You guessed\n", this);
				return new NotifyState(NotifyState.ERROR, result.getHint().get(), this);
			case 1:
				//share
				// if user can't share return Error Notify with this as next
				// if user can share then share and return Success Notify with this as next
				break;
			case 2:
				//logout
				return new NotifyState(NotifyState.SUCCESS, "Logged out\nPlease log in", new WordleLoginState());
			default: throw new StateProtocolException(new AbortionState());
		}
		return this;
	}

	@Override
	public int getInterestOps() {
		return SelectionKey.OP_READ;
	}

}
