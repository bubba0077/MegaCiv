package net.bubbaland.megaciv.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.GameEvent;
import net.bubbaland.megaciv.game.GameEvent.EventType;

public class CivEditMessage implements ClientMessage {

	@JsonProperty("civ")
	private final Civilization civ;

	@JsonCreator
	public CivEditMessage(@JsonProperty("civ") Civilization civ) {
		this.civ = civ;
	}

	public Civilization getCivilization() {
		return this.civ;
	}

	@Override
	public String toString() {
		return "Civ " + civ.getName() + " edited: " + civ.toFullString();
	}

	@Override
	public EventType getEventType() {
		return GameEvent.EventType.CIV_EDIT;
	}
}
