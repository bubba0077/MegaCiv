package net.bubbaland.megaciv.game;

import java.time.Duration;

public interface StopwatchListener {

	public abstract void tic(Duration tics);

	public abstract void watchStarted();

	public abstract void watchStopped();

	public abstract void watchReset();

}