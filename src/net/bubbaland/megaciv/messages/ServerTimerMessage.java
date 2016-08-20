package net.bubbaland.megaciv.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ServerTimerMessage extends ServerMessage implements TimerMessage {

	@JsonProperty("action")
	private final StopwatchEvent	eventType;
	@JsonProperty("eventTime")
	private final long				eventTime;
	@JsonProperty("lastDeciseconds")
	private final int				lastDeciseconds;
	@JsonProperty("timerLength")
	private final int				timerLength;

	@JsonCreator
	public ServerTimerMessage(@JsonProperty("action") StopwatchEvent eventType,
			@JsonProperty("eventTime") long timerStart, @JsonProperty("timerLength") int timerLength,
			@JsonProperty("lastDeciseconds") int lastDeciseconds) {
		this.eventType = eventType;
		this.timerLength = timerLength;
		this.eventTime = timerStart;
		this.lastDeciseconds = lastDeciseconds;
	}

	public StopwatchEvent getEvent() {
		return this.eventType;
	}

	public int getTimerLength() {
		return this.timerLength;
	}

	public long getEventTime() {
		return this.eventTime;
	}

	public int getLastEventTic() {
		return this.lastDeciseconds;
	}
}
