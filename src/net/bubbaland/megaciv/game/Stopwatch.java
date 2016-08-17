package net.bubbaland.megaciv.game;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Timer;

public class Stopwatch implements ActionListener {
	private int									timerLength;
	private volatile long						startTime;
	private volatile int						deciseconds;
	private final Timer							timer;

	private final ArrayList<StopwatchListener>	listeners;

	public Stopwatch() {
		this(300);
	}

	public Stopwatch(int seconds) {
		this.listeners = new ArrayList<StopwatchListener>();
		this.timer = new Timer(100, this);
		this.timer.setActionCommand("tic");
		this.timerLength = seconds;
		this.deciseconds = this.timerLength * 10;
		this.startTime = 0;
	}

	public synchronized void startOffset(long milliseconds) {
		this.startTime = System.currentTimeMillis();
		this.deciseconds = this.deciseconds - (int) ( ( this.startTime - milliseconds ) / 100 );
		this.timer.start();
		for (StopwatchListener listener : this.listeners) {
			listener.watchStarted();
		}
	}

	public synchronized void start() {
		this.timer.start();
		for (StopwatchListener listener : this.listeners) {
			listener.watchStarted();
		}
	}

	public synchronized void stopOffset(long milliseconds) {
		this.timer.stop();
		long now = System.currentTimeMillis();
		this.deciseconds = this.deciseconds + (int) ( ( now - milliseconds ) / 100 );
		for (StopwatchListener listener : this.listeners) {
			listener.watchStopped();
		}
	}

	public synchronized void stop() {
		this.timer.stop();
		for (StopwatchListener listener : this.listeners) {
			listener.watchStopped();
		}
	}

	public synchronized void reset() {
		this.timer.stop();
		this.deciseconds = this.timerLength * 10;
		for (StopwatchListener listener : this.listeners) {
			listener.watchReset();
		}
	}

	public synchronized void setTimer(int seconds) {
		this.timer.stop();
		this.timerLength = seconds;
		this.reset();
	}

	public synchronized void addStopwatchListener(StopwatchListener listener) {
		this.listeners.add(listener);
		if (timer.isRunning()) {
			listener.watchStarted();
		} else {
			listener.watchStopped();
		}
	}

	public synchronized void removeStopwatchListener(StopwatchListener listener) {
		this.listeners.remove(listener);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		this.deciseconds--;
		if (this.deciseconds <= 0) {
			this.timer.stop();
			this.deciseconds = 0;
		}
		for (StopwatchListener listener : this.listeners) {
			listener.tic(this.deciseconds);
		}
	}

	public int getTimerLength() {
		return this.timerLength;
	}

	public int getTicsRemaining() {
		return this.deciseconds;
	}

}