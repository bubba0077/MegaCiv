package net.bubbaland.megaciv.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Civilization;

public class CivEditMessage extends ClientMessage {

	@JsonProperty("civ")
	private final Civilization civ;

	public CivEditMessage(@JsonProperty("civ") Civilization civ) {
		this.civ = civ;
	}

	public Civilization getCivilization() {
		return this.civ;
	}

}
