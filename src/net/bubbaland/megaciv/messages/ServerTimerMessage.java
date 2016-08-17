package net.bubbaland.megaciv.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ServerTimerMessage extends ServerMessage implements TimerMessage {

	@JsonProperty("connectionTime")
	private final long		connectionTime;
	@JsonProperty("action")
	private final Action	action;
	@JsonProperty("timerLength")
	private final int		timerLength;

	@JsonCreator
	public ServerTimerMessage(@JsonProperty("action") Action action,
			@JsonProperty("connectionTime") long connectionTime, @JsonProperty("timerLength") int timerLength) {
		this.action = action;
		this.connectionTime = connectionTime;
		this.timerLength = timerLength;
	}

	public Action getAction() {
		return this.action;
	}

	public long getConnectionTime() {
		return this.connectionTime;
	}

	public int getTimerLength() {
		return this.timerLength;
	}
}
