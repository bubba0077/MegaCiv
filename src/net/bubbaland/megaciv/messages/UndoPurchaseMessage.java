package net.bubbaland.megaciv.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Civilization;

public class UndoPurchaseMessage extends ClientMessage {

	@JsonProperty("civName")
	private final Civilization.Name civName;

	@JsonCreator
	public UndoPurchaseMessage(@JsonProperty("civName") Civilization.Name civName) {
		this.civName = civName;
	}

	public Civilization.Name getCivName() {
		return this.civName;
	}
}
