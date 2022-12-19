package io.github.sdamico12.wordle.server.connections.states.wordlestates;

import io.github.sdamico12.wordle.server.account.Account;
import io.github.sdamico12.wordle.server.account.AccountManager;
import io.github.sdamico12.wordle.server.connections.states.ConnectionState;
import io.github.sdamico12.wordle.server.connections.states.exceptions.AbortionState;
import io.github.sdamico12.wordle.server.connections.states.exceptions.NotifyState;
import io.github.sdamico12.wordle.server.connections.states.exceptions.StateProtocolException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Optional;

public class WordleLoginState implements ConnectionState {

	private static AccountManager accountManager = null;
	private ByteBuffer buf;
	public WordleLoginState() throws IOException {
		if(accountManager == null) accountManager = AccountManager.getManager();
		buf = ByteBuffer.allocate(1024);
	}

	/*
	Una di interazione con l'account manager è un pacchetto di 516 byte formattato come segue:
	- byte 0 -> tipo di richiesta (register login unregister)
	- byte 1 -> lunghezza username
	- byte 2 -> lunghezza password
	- byte 3-258 -> username
	- byte 259-515 -> password
	 */

	@Override
	public ConnectionState execute(SocketChannel channel) throws StateProtocolException, IOException {
		if(buf.position() != buf.limit()){
			channel.read(buf);
			return this;
		}
		byte[] input = buf.array();
		buf.clear();
		byte reqType = input[0], usernameLength = input[1], passwordLength = input[2];
		String username = new String(Arrays.copyOfRange(input,3,Math.min(usernameLength-3, 258)));
		String password = new String(Arrays.copyOfRange(input,259,Math.min(passwordLength-259,515)));
		Optional<Account> o_account;
		switch (reqType){
			case 0:
				o_account = accountManager.register(username, password);
				if(o_account.isEmpty())
					return new NotifyState(0,"Error while registering", this);
				else return new NotifyState(1,"Registered successfully", this);
			case 1:
				o_account = accountManager.login(username, password);
				if(o_account.isEmpty())
					return new NotifyState(0,"Login failed", this);
				else return new NotifyState(1, "Login successful", new WordleLobbyState(o_account.get()));
			case 2:
				if(accountManager.unregister(username,password))
					return new NotifyState(1,"unregistered", this);
				else return new NotifyState(0,"unregistration failed", this);
			default: throw new StateProtocolException(new AbortionState());
		}
	}

	@Override
	public int getInterestOps() {
		return SelectionKey.OP_READ;
	}
}
