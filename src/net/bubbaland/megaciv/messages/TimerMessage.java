package net.bubbaland.megaciv.messages;

import java.time.Duration;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.GameEvent;
import net.bubbaland.megaciv.game.Stopwatch;
import net.bubbaland.megaciv.game.GameEvent.EventType;

public class TimerMessage implements ClientMessage, ServerMessage {

	public enum StopwatchEvent {
		START, STOP, RESET, SET, SET_LAST_TIC
	};

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
	public TimerMessage(@JsonProperty("action") StopwatchEvent eventType, @JsonProperty("eventTime") Instant timerStart,
			@JsonProperty("timerLength") Duration timerLength, @JsonProperty("timeRemaining") Duration timeRemaining) {
		this.eventType = eventType;
		this.timerLength = timerLength;
		this.eventTime = timerStart;
		this.timeRemaining = timeRemaining;
	}

	public StopwatchEvent getEvent() {
		return eventType;
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

	@Override
	public String toString() {
		return "Stopwatch " + this.eventType + " at " + GameEvent.dateFormat.format(this.eventTime) + " with "
				+ Stopwatch.formatTimer(this.timeRemaining) + " remaining";
	}

	@Override
	public EventType getEventType() {
		return GameEvent.EventType.STOPWATCH;
	}
}
