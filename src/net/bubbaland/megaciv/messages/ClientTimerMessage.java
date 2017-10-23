package net.bubbaland.megaciv.messages;

import java.time.Duration;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.GameEvent;

public class ClientTimerMessage extends ClientMessage implements TimerMessage {

	@JsonProperty("action")
	private final StopwatchEvent	eventType;
	@JsonProperty("timerLength")
	private final Duration			timerLength;
	@JsonProperty("timeRemaining")
	private final Duration			timeRemaining;
	// Timer start in server time
	@JsonProperty("eventTime")
	private final Instant			eventTime;

	@JsonCreator
	public ClientTimerMessage(@JsonProperty("action") StopwatchEvent eventType,
			@JsonProperty("eventTime") Instant timerStart, @JsonProperty("timerLength") Duration timerLength,
			@JsonProperty("timeRemaining") Duration timeRemaining) {
		super(GameEvent.EventType.STOPWATCH);
		this.eventType = eventType;
		this.timerLength = timerLength;
		this.eventTime = timerStart;
		this.timeRemaining = timeRemaining;
	}

	@Override
	public StopwatchEvent getEvent() {
		return eventType;
	}

	@Override
	public Duration getTimerLength() {
		return this.timerLength;
	}

	@Override
	public Instant getEventTime() {
		return this.eventTime;
	}

	@Override
	public Duration getLastEventTic() {
		return this.timeRemaining;
	}

	@Override
	public String toString() {
		// TODO
		return null;
	}
}
