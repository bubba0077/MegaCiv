package net.bubbaland.megaciv.messages;

import net.bubbaland.megaciv.game.GameEvent;
import net.bubbaland.megaciv.game.GameEvent.EventType;

public class KeepAliveMessage implements ClientMessage {

	@Override
	public EventType getEventType() {
		return GameEvent.EventType.KEEPALIVE;
	}

	public String toString() {
		return "Keep-Alive Message";
	}

}
