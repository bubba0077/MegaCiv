package net.bubbaland.megaciv.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.GameEvent;
import net.bubbaland.megaciv.game.GameEvent.EventType;

public class UndoPurchaseMessage implements ClientMessage {

	@JsonProperty("civName")
	private final Civilization.Name civName;

	@JsonCreator
	public UndoPurchaseMessage(@JsonProperty("civName") Civilization.Name civName) {
		this.civName = civName;
	}

	public Civilization.Name getCivName() {
		return this.civName;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EventType getEventType() {
		return GameEvent.EventType.TECH_PURCHASE;
	}
}
