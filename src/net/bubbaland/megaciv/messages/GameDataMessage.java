package net.bubbaland.megaciv.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.bubbaland.megaciv.game.Game;

public class GameDataMessage extends ServerMessage {

	@JsonProperty("game")
	private final Game game;

	@JsonCreator
	public GameDataMessage(@JsonProperty("game") Game game) {
		super();
		this.game = game;
	}

	public Game getGame() {
		return this.game;
	}

}
