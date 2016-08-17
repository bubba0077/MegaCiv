package net.bubbaland.megaciv.client.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Timer;

public class Stopwatch implements ActionListener {
	private int									timerLength;
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
	}

	public synchronized void start() {
		this.timer.start();
		for (StopwatchListener listener : this.listeners) {
			listener.watchStarted();
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

interface StopwatchListener {

	public abstract void tic(int deciseconds);

	public abstract void watchStarted();

	public abstract void watchStopped();

	public abstract void watchReset();

}