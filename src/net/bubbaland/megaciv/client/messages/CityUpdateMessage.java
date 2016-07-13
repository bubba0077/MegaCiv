package net.bubbaland.megaciv.client.messages;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Civilization;

public class CityUpdateMessage extends ClientMessage {

	@JsonProperty("cityCount")
	private HashMap<Civilization.Name, Integer> cityCount;

	@JsonCreator
	public CityUpdateMessage(@JsonProperty("cityCount") HashMap<Civilization.Name, Integer> cityCount) {
		this.cityCount = cityCount;
	}

	public HashMap<Civilization.Name, Integer> getCityCount() {
		return this.cityCount;
	}
}
