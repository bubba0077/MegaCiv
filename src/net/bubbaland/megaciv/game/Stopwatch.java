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
	private volatile int						lastEventTic;
	private volatile int						tics;
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
		this.tics = this.timerLength * 10;
		this.lastEventTime = -1;
		this.lastEventTic = -1;
	}

	public synchronized void startOffset(long eventTime) {
		this.lastEventTime = eventTime;
		this.lastEventTic = this.tics;
		this.tics = this.tics - (int) ( ( System.currentTimeMillis() - eventTime ) / 100 );
		this.timer.start();
		for (StopwatchListener listener : this.listeners) {
			listener.watchStarted();
		}
	}

	public synchronized void start() {
		this.lastEventTic = this.tics;
		this.timer.start();
		for (StopwatchListener listener : this.listeners) {
			listener.watchStarted();
		}
	}

	public synchronized void stopOffset(long eventTime) {
		this.timer.stop();
		this.lastEventTime = eventTime;
		this.lastEventTic = this.tics;
		this.tics = this.tics + (int) ( ( System.currentTimeMillis() - eventTime ) / 100 );
		for (StopwatchListener listener : this.listeners) {
			listener.watchStopped();
		}
	}

	public synchronized void stop() {
		this.timer.stop();
		this.lastEventTic = this.tics;
		for (StopwatchListener listener : this.listeners) {
			listener.watchStopped();
		}
	}

	public synchronized void reset() {
		this.reset(System.currentTimeMillis());
	}

	public synchronized void reset(long eventTime) {
		this.timer.stop();
		this.lastEventTime = eventTime;
		this.tics = this.timerLength * 10;
		this.lastEventTic = this.tics;
		for (StopwatchListener listener : this.listeners) {
			listener.watchReset();
		}
	}

	public int getLastEventTic() {
		return this.lastEventTic;
	}

	public synchronized void setTics(int tic) {
		this.tics = tic;
		this.lastEventTic = tic;
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
		this.tics--;
		if (this.tics <= 0) {
			this.timer.stop();
			this.tics = 0;
		}
		for (StopwatchListener listener : this.listeners) {
			listener.tic(this.tics);
		}
	}

	public int getTimerLength() {
		return this.timerLength;
	}

	public int getTicsRemaining() {
		return this.tics;
	}

	public long getLastEventTime() {
		return this.lastEventTime;
	}

	public boolean isRunning() {
		return this.timer.isRunning();
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
			case SET_LAST_TIC:
				this.setTics(message.getLastDeciseconds());
				break;
			default:
				break;
		}
		// System.out.println("Remote event " + eventType.toString() + " that happened at " + new
		// Date(this.lastEventTime)
		// + " processed");
	}

}