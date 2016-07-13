package net.bubbaland.megaciv.client.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Civilization;

public class AssignPlayerMessage extends ClientMessage {

	@JsonProperty("civ")
	private final Civilization.Name	civ;
	@JsonProperty("player")
	private final String			player;

	@JsonCreator
	public AssignPlayerMessage(@JsonProperty("civ") Civilization.Name civ, @JsonProperty("player") String player) {
		super();
		this.civ = civ;
		this.player = player;
	}

	public Civilization.Name getCivilizationName() {
		return this.civ;
	}

	public String getPlayer() {
		return this.player;

	}
}
