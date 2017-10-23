package net.bubbaland.megaciv.messages;

import java.time.Duration;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ServerTimerMessage extends ServerMessage implements TimerMessage {

	@JsonProperty("action")
	private final StopwatchEvent	eventType;
	@JsonProperty("eventTime")
	private final Instant			eventTime;
	@JsonProperty("timeRemaining")
	private final Duration			timeRemaining;
	@JsonProperty("timerLength")
	private final Duration			timerLength;

	@JsonCreator
	public ServerTimerMessage(@JsonProperty("action") StopwatchEvent eventType,
			@JsonProperty("eventTime") Instant eventTime, @JsonProperty("timerLength") Duration timerLength,
			@JsonProperty("timeRemaining") Duration timeRemaining) {
		this.eventType = eventType;
		this.eventTime = eventTime;
		this.timerLength = timerLength;
		this.timeRemaining = timeRemaining;
	}

	public StopwatchEvent getEvent() {
		return this.eventType;
	}

	public Duration getTimerLength() {
		return this.timerLength;
	}

	public Instant getEventTime() {
		return this.eventTime;
	}

	public Duration getLastEventTic() {
		return this.timeRemaining;
	}
}
