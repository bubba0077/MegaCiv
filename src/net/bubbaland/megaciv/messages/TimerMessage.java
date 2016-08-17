package net.bubbaland.megaciv.messages;

public interface TimerMessage {

	public enum Action {
		START, STOP, RESET, SET
	};

	public Action getAction();

	public int getTimerLength();
}
