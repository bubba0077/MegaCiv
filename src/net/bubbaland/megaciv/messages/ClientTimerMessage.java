package net.bubbaland.megaciv.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ClientTimerMessage extends ClientMessage implements TimerMessage {

	@JsonProperty("action")
	private final StopwatchEvent	eventType;
	@JsonProperty("timerLength")
	private final int				timerLength;
	@JsonProperty("lastDeciseconds")
	private final int				lastDeciseconds;
	// Timer start in server time
	@JsonProperty("eventTime")
	private final long				eventTime;

	@JsonCreator
	public ClientTimerMessage(@JsonProperty("action") StopwatchEvent eventType,
			@JsonProperty("eventTime") long timerStart, @JsonProperty("timerLength") int timerLength,
			@JsonProperty("lastDeciseconds") int lastDeciseconds) {
		this.eventType = eventType;
		this.timerLength = timerLength;
		this.eventTime = timerStart;
		this.lastDeciseconds = lastDeciseconds;
	}

	@Override
	public StopwatchEvent getEvent() {
		return eventType;
	}

	@Override
	public int getTimerLength() {
		return this.timerLength;
	}

	@Override
	public long getEventTime() {
		return this.eventTime;
	}

	@Override
	public int getLastEventTic() {
		return this.lastDeciseconds;
	}
}
