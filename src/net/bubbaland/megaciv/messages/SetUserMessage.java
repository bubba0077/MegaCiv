package net.bubbaland.megaciv.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.User;

public class SetUserMessage extends ClientMessage {

	@JsonProperty("user")
	private User user;

	@JsonCreator
	public SetUserMessage(@JsonProperty("user") User user) {
		this.user = user;
	}

	public User getUser() {
		return this.user;
	}

}
