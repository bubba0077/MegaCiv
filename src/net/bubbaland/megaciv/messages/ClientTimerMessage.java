package net.bubbaland.megaciv.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ClientTimerMessage extends ClientMessage implements TimerMessage {

	@JsonProperty("action")
	private final Action	action;
	@JsonProperty("timerLength")
	private final int		timerLength;

	@JsonCreator
	public ClientTimerMessage(@JsonProperty("action") Action action, @JsonProperty("timerLength") int timerLength) {
		this.action = action;
		this.timerLength = timerLength;
	}

	public Action getAction() {
		return this.action;
	}

	public int getTimerLength() {
		return this.timerLength;
	}
}
