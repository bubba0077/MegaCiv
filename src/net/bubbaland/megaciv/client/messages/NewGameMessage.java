package net.bubbaland.megaciv.client.messages;

import java.util.HashMap;

import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Game.Difficulty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NewGameMessage extends ClientMessage {

	@JsonProperty("civNames")
	private final HashMap<Civilization.Name, String>	civNames;
	@JsonProperty("Difficulty")
	private final Difficulty							difficulty;

	@JsonCreator
	public NewGameMessage(@JsonProperty("civNames") HashMap<Civilization.Name, String> newCivNames,
			@JsonProperty("Difficulty") final Difficulty difficulty) {
		super();
		this.civNames = newCivNames;
		this.difficulty = difficulty;
	}

	public HashMap<Civilization.Name, String> getCivNames() {
		return this.civNames;
	}

	public Difficulty getDifficulty() {
		return this.difficulty;
	}
}
