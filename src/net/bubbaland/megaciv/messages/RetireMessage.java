package net.bubbaland.megaciv.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.GameEvent;

public class RetireMessage extends ClientMessage {

	@JsonProperty("name")
	private final Civilization.Name name;

	public RetireMessage(@JsonProperty("name") Civilization.Name name) {
		super(GameEvent.EventType.GAME_START);
		this.name = name;
	}

	public Civilization.Name getCivName() {
		return this.name;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

}
