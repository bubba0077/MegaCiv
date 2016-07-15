package net.bubbaland.megaciv.client.messages;

import java.util.HashMap;

import net.bubbaland.megaciv.game.Civilization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NewGameMessage extends ClientMessage {

	@JsonProperty("civNames")
	private final HashMap<Civilization.Name, String> civNames;

	@JsonCreator
	public NewGameMessage(@JsonProperty("civNames") HashMap<Civilization.Name, String> newCivNames) {
		super();
		this.civNames = newCivNames;
	}

	public HashMap<Civilization.Name, String> getCivNames() {
		return this.civNames;
	}
}
