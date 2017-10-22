package net.bubbaland.megaciv.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.GameEvent;

public class CivEditMessage extends ClientMessage {

	@JsonProperty("civ")
	private final Civilization civ;

	public CivEditMessage(@JsonProperty("civ") Civilization civ) {
		super(GameEvent.EventType.CIV_EDIT);
		this.civ = civ;
	}

	public Civilization getCivilization() {
		return this.civ;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}
}
