package net.bubbaland.megaciv.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ServerTimerMessage extends ServerMessage implements TimerMessage {

	@JsonProperty("eventTime")
	private final long				eventTime;
	@JsonProperty("action")
	private final StopwatchEvent	action;
	@JsonProperty("timerLength")
	private final int				timerLength;

	@JsonCreator
	public ServerTimerMessage(@JsonProperty("action") StopwatchEvent action,
			@JsonProperty("eventTime") long eventTime, @JsonProperty("timerLength") int timerLength) {
		this.action = action;
		this.eventTime = eventTime;
		this.timerLength = timerLength;
	}

	public StopwatchEvent getEvent() {
		return this.action;
	}

	public long getConnectionTime() {
		return this.eventTime;
	}

	public int getTimerLength() {
		return this.timerLength;
	}

	@Override
	public long getEventTime() {
		return this.eventTime;
	}
}
