package net.bubbaland.megaciv.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Game;

public class GameEndMessage extends GameDataMessage {

	@JsonCreator
	public GameEndMessage(@JsonProperty("game") final Game game) {
		super(game);
	}

}
