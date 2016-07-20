package net.bubbaland.megaciv.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Civilization;

public class RetireMessage extends ClientMessage {

	@JsonProperty("name")
	private final Civilization.Name name;

	public RetireMessage(@JsonProperty("name") Civilization.Name name) {
		this.name = name;
	}

	public Civilization.Name getCivName() {
		return this.name;
	}

}
