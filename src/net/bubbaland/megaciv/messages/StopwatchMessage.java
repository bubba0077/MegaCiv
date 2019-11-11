package net.bubbaland.megaciv.messages;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.GameEvent;
import net.bubbaland.megaciv.game.GameEvent.EventType;
import net.bubbaland.megaciv.game.Stopwatch;

public class StopwatchMessage implements ClientMessage, ServerMessage {

	// Date format to use
	static public final DateTimeFormatter	dateFormat	=
			DateTimeFormatter.ofPattern("yyyy MMM dd hh:mm:ss.SSS").withZone(ZoneId.systemDefault());

	@JsonProperty("action")
	private final Stopwatch.StopwatchEvent	eventType;
	@JsonProperty("timerLength")
	private final Duration					timerLength;
	@JsonProperty("lastEventTimeRemaining")
	private final Duration					lastEventTimeRemaining;
	// Timer start in server time
	@JsonProperty("eventTime")
	private final Instant					eventTime;

	@JsonCreator
	public StopwatchMessage(@JsonProperty("action") final Stopwatch.StopwatchEvent eventType,
			@JsonProperty("timerLength") final Duration timerLength,
			@JsonProperty("eventTime") final Instant lastEventTime,
			@JsonProperty("lastEventTimeRemaining") final Duration lastEventTimeRemaining) {
		this.eventType = eventType;
		this.timerLength = timerLength;
		this.eventTime = lastEventTime;
		this.lastEventTimeRemaining = lastEventTimeRemaining;
	}

	public Stopwatch.StopwatchEvent getEvent() {
		return this.eventType;
	}

	public Duration getTimerLength() {
		return this.timerLength;
	}

	public Instant getEventTime() {
		return this.eventTime;
	}

	public Duration getLastEventTimeRemaining() {
		return this.lastEventTimeRemaining;
	}

	@Override
	public String toString() {
		return "Stopwatch " + this.eventType + " at " + StopwatchMessage.dateFormat.format(this.eventTime);
	}

	@Override
	public EventType getEventType() {
		return GameEvent.EventType.STOPWATCH;
	}
}
