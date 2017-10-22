package net.bubbaland.megaciv.messages;

import java.util.HashMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.GameEvent;

public class CensusMessage extends ClientMessage {

	@JsonProperty("census")
	private HashMap<Civilization.Name, Integer> census;

	@JsonCreator
	public CensusMessage(@JsonProperty("census") HashMap<Civilization.Name, Integer> census) {
		super(GameEvent.EventType.CENSUS);
		this.census = census;
	}

	public HashMap<Civilization.Name, Integer> getCensus() {
		return this.census;
	}

	public String toString() {
		String s = "New Census:<BR>";
		s = s + census.keySet().stream().map(civName -> civName.toString() + ": " + census.get(civName) + "</br>")
				.collect(Collectors.joining());
		return s;
	}

}
