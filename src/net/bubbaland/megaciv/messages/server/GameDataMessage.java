package net.bubbaland.megaciv.messages.server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import net.bubbaland.megaciv.game.Game;

// @JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
// @JsonTypeName("GameDataMessage")
public class GameDataMessage extends ServerMessage {

	@JsonProperty("game")
	final private Game game;

	@JsonCreator
	public GameDataMessage(@JsonProperty("game") Game game) {
		super();
		this.game = game;
	}

	public Game getGame() {
		return this.game;
	}

}
