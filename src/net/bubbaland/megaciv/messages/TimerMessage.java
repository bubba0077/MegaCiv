package net.bubbaland.megaciv.messages;

public interface TimerMessage {

	public enum StopwatchEvent {
		START, STOP, RESET, SET
	};

	public StopwatchEvent getEvent();

	public int getTimerLength();

	public long getEventTime();
}
