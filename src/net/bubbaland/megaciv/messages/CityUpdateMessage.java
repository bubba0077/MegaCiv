package net.bubbaland.megaciv.messages;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.GameEvent;

public class CityUpdateMessage extends ClientMessage {

	@JsonProperty("cityCount")
	private HashMap<Civilization.Name, Integer> cityCount;

	@JsonCreator
	public CityUpdateMessage(@JsonProperty("cityCount") HashMap<Civilization.Name, Integer> cityCount) {
		super(GameEvent.EventType.CITY_COUNT);
		this.cityCount = cityCount;
	}

	public HashMap<Civilization.Name, Integer> getCityCount() {
		return this.cityCount;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}
}
