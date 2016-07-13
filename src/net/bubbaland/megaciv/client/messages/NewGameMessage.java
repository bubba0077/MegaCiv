package net.bubbaland.megaciv.client.messages;

import java.util.ArrayList;
import net.bubbaland.megaciv.game.Civilization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NewGameMessage extends ClientMessage {

	@JsonCreator
	public NewGameMessage() {
		super();
	}
}
