package net.bubbaland.megaciv.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.GameEvent;
import net.bubbaland.megaciv.game.GameEvent.EventType;

public class RetireMessage implements ClientMessage {

	@JsonProperty("name")
	private final Civilization.Name name;

	public RetireMessage(@JsonProperty("name") Civilization.Name name) {
		this.name = name;
	}

	public Civilization.Name getCivName() {
		return this.name;
	}

	@Override
	public String toString() {
		return name.toString() + " retired";
	}

	@Override
	public EventType getEventType() {
		return GameEvent.EventType.GAME_START;
	}

}
