package net.bubbaland.megaciv.messages;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.GameEvent;
import net.bubbaland.megaciv.game.Stopwatch;
import net.bubbaland.megaciv.game.GameEvent.EventType;

public class TimerMessage implements ClientMessage, ServerMessage {

	// Date format to use
	static public final DateTimeFormatter dateFormat =
			DateTimeFormatter.ofPattern("yyyy MMM dd hh:mm:ss.SSS").withZone(ZoneId.systemDefault());

	public enum StopwatchEvent {
		START, STOP, RESET, SET, SET_LAST_TIC
	};

	@JsonProperty("action")
	private final StopwatchEvent	eventType;
	@JsonProperty("timerLength")
	private final Duration			timerLength;
	@JsonProperty("lastEventTimeRemaining")
	private final Duration			lastEventTimeRemaining;
	@JsonProperty("timeRemaining")
	private final Duration			timeRemaining;
	// Timer start in server time
	@JsonProperty("eventTime")
	private final Instant			eventTime;

	@JsonCreator
	public TimerMessage(@JsonProperty("action") StopwatchEvent eventType,
			@JsonProperty("timerLength") Duration timerLength, @JsonProperty("eventTime") Instant lastEventTime,
			@JsonProperty("lastEventTimeRemaining") Duration lastEventTimeRemaining,
			@JsonProperty("timeRemaining") Duration timeRemaining) {
		this.eventType = eventType;
		this.timerLength = timerLength;
		this.eventTime = lastEventTime;
		this.timeRemaining = timeRemaining;
		this.lastEventTimeRemaining = lastEventTimeRemaining;
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

	public Duration getTimeRemaining() {
		return this.timeRemaining;
	}

	public Duration getLastEventTimeRemaining() {
		return this.lastEventTimeRemaining;
	}

	@Override
	public String toString() {
		return "Stopwatch " + this.eventType + " at " + TimerMessage.dateFormat.format(this.eventTime) + " with "
				+ Stopwatch.formatTimer(this.timeRemaining) + " remaining";
	}

	@Override
	public EventType getEventType() {
		return GameEvent.EventType.STOPWATCH;
	}
}
