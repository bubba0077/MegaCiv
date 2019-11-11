package net.bubbaland.megaciv.messages;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.GameEvent;
import net.bubbaland.megaciv.game.GameEvent.EventType;

public class CityUpdateMessage implements ClientMessage {

	@JsonProperty("cityCount")
	private final HashMap<Civilization.Name, Integer> cityCount;

	@JsonCreator
	public CityUpdateMessage(@JsonProperty("cityCount") final HashMap<Civilization.Name, Integer> cityCount) {
		this.cityCount = cityCount;
	}

	public HashMap<Civilization.Name, Integer> getCityCount() {
		return this.cityCount;
	}

	@Override
	public String toString() {
		return "City Count: " + this.cityCount.toString();
	}

	@Override
	public EventType getEventType() {
		return GameEvent.EventType.CITY_COUNT;
	}
}
