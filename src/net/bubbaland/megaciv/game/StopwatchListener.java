package net.bubbaland.megaciv.game;

public interface StopwatchListener {

	public abstract void tic(int deciseconds);

	public abstract void watchStarted();

	public abstract void watchStopped();

	public abstract void watchReset();

}