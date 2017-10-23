package net.bubbaland.megaciv.messages;

import java.util.HashMap;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.GameEvent;
import net.bubbaland.megaciv.game.GameEvent.EventType;

public class CensusMessage implements ClientMessage {

	@JsonProperty("census")
	private HashMap<Civilization.Name, Integer> census;

	@JsonCreator
	public CensusMessage(@JsonProperty("census") HashMap<Civilization.Name, Integer> census) {
		this.census = census;
	}

	public HashMap<Civilization.Name, Integer> getCensus() {
		return this.census;
	}

	public String toString() {
		String s = "New Census: " + census.toString();
		// s = s + census.keySet().stream().map(civName -> civName.toString() + ": " + census.get(civName) + "</br>")
		// .collect(Collectors.joining());
		return s;
	}

	@Override
	public EventType getEventType() {
		return GameEvent.EventType.CENSUS;
	}

}
