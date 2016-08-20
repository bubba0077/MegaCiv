package net.bubbaland.megaciv.messages;

public interface TimerMessage {

	public enum StopwatchEvent {
		START, STOP, RESET, SET, SET_LAST_TIC
	};

	public StopwatchEvent getEvent();

	public int getTimerLength();

	public long getEventTime();

	public int getLastEventTic();
}
