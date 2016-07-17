package net.bubbaland.megaciv.messages;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Civilization;

public class CensusMessage extends ClientMessage {

	@JsonProperty("census")
	private HashMap<Civilization.Name, Integer> census;

	@JsonCreator
	public CensusMessage(@JsonProperty("census") HashMap<Civilization.Name, Integer> census) {
		this.census = census;
	}

	public HashMap<Civilization.Name, Integer> getCensus() {
		return this.census;
	}

}
