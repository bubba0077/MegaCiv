package net.bubbaland.megaciv.game;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.Timer;

import net.bubbaland.megaciv.messages.TimerMessage;
import net.bubbaland.megaciv.messages.TimerMessage.StopwatchEvent;

public class Stopwatch implements ActionListener {
	private int									timerLength;
	private volatile long						lastEventTime;
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
		this.lastEventTime = -1;
	}

	public synchronized void startOffset(long eventTime) {
		this.lastEventTime = eventTime;
		this.deciseconds = this.deciseconds - (int) ( ( System.currentTimeMillis() - eventTime ) / 100 );
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

	public synchronized void stopOffset(long eventTime) {
		this.timer.stop();
		this.lastEventTime = eventTime;
		this.deciseconds = this.deciseconds + (int) ( ( System.currentTimeMillis() - eventTime ) / 100 );
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
		this.reset(System.currentTimeMillis());
	}

	public synchronized void reset(long eventTime) {
		this.timer.stop();
		this.deciseconds = this.timerLength * 10;
		for (StopwatchListener listener : this.listeners) {
			listener.watchReset();
		}
	}

	public synchronized void setTimer(int seconds) {
		this.setTimer(System.currentTimeMillis(), seconds);
	}

	public synchronized void setTimer(long eventTime, int seconds) {
		this.timer.stop();
		this.timerLength = seconds;
		this.reset(eventTime);
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

	public long getLastEventTime() {
		return this.lastEventTime;
	}

	public synchronized void remoteEvent(TimerMessage message, long offset) {
		StopwatchEvent eventType = message.getEvent();
		long eventTime = message.getEventTime() - offset;
		if (this.timerLength != message.getTimerLength()) {
			this.timerLength = message.getTimerLength();
		}
		switch (eventType) {
			case START:
				this.startOffset(eventTime);
				break;
			case STOP:
				this.stopOffset(eventTime);
				break;
			case SET:
				this.setTimer(eventTime, message.getTimerLength());
				break;
			case RESET:
				this.reset(eventTime);
				break;
		}
		// System.out.println("Remote event " + eventType.toString() + " that happened at " + new
		// Date(this.lastEventTime)
		// + " processed");
	}

}