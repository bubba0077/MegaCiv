package net.bubbaland.megaciv.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Game;

public class LoadGameMessage extends ClientMessage {

	@JsonProperty("game")
	private final Game game;

	@JsonCreator
	public LoadGameMessage(@JsonProperty("game") Game game) {
		this.game = game;
	}

	/**
	 * @return the game
	 */
	public Game getGame() {
		return this.game;
	}


}
