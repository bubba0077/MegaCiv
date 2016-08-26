package net.bubbaland.megaciv.messages;

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
	@JsonProperty("Region")
	private final Civilization.Region					region;

	@JsonCreator
	public NewGameMessage(@JsonProperty("Region") Civilization.Region region,
			@JsonProperty("civNames") HashMap<Civilization.Name, String> newCivNames,
			@JsonProperty("Difficulty") final Difficulty difficulty) {
		super();
		this.region = region;
		this.civNames = newCivNames;
		this.difficulty = difficulty;
	}

	public Civilization.Region getRegion() {
		return this.region;
	}

	public HashMap<Civilization.Name, String> getCivNames() {
		return this.civNames;
	}

	public Difficulty getDifficulty() {
		return this.difficulty;
	}
}
