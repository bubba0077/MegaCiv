package net.bubbaland.megaciv.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Game;
import net.bubbaland.megaciv.game.GameEvent;
import net.bubbaland.megaciv.game.GameEvent.EventType;

public class LoadGameMessage implements ClientMessage {

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

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EventType getEventType() {
		return GameEvent.EventType.GAME_START;
	}

}
