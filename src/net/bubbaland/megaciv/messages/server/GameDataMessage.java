package net.bubbaland.megaciv.messages.server;

import net.bubbaland.megaciv.game.Game;

public class GameDataMessage extends ServerMessage {

	final private Game game;

	public GameDataMessage(Game game) {
		super("GameData");
		this.game = game;
	}

	public Game getGame() {
		return this.game;
	}

}
