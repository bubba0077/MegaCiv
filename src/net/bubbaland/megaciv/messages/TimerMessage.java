package net.bubbaland.megaciv.messages;

import java.time.Duration;
import java.time.Instant;

public interface TimerMessage {

	public enum StopwatchEvent {
		START, STOP, RESET, SET, SET_LAST_TIC
	};

	public StopwatchEvent getEvent();

	public Duration getTimerLength();

	public Instant getEventTime();

	public Duration getLastEventTic();
}
