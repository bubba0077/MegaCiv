package net.bubbaland.megaciv.game;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import net.bubbaland.megaciv.messages.TimerMessage;
import net.bubbaland.megaciv.messages.TimerMessage.StopwatchEvent;

public class Stopwatch {
	private int									timerLength;
	private volatile long						lastEventTime;
	private volatile int						lastEventTic;
	private volatile int						tics;
	private Timer								timer;
	private boolean								isRunning;

	private final ArrayList<StopwatchListener>	listeners;

	public Stopwatch() {
		this(300);
	}

	public Stopwatch(int seconds) {
		this.listeners = new ArrayList<StopwatchListener>();
		this.timer = null;
		this.timerLength = seconds;
		this.tics = this.timerLength * 10;
		this.lastEventTime = -1;
		this.lastEventTic = this.tics;
		this.isRunning = false;
	}

	public synchronized void startOffset(long eventTime) {
		this.timer = new Timer();
		this.lastEventTime = eventTime;
		this.lastEventTic = this.tics;
		this.tics = this.tics - (int) ( ( System.currentTimeMillis() - eventTime ) / 100 );
		this.timer.scheduleAtFixedRate(new TicTok(), 0, 100);
		for (StopwatchListener listener : this.listeners) {
			listener.watchStarted();
		}
		this.isRunning = true;
	}

	public synchronized void start() {
		this.lastEventTic = this.tics;
		this.timer = new Timer();
		this.timer.scheduleAtFixedRate(new TicTok(), 0, 100);
		for (StopwatchListener listener : this.listeners) {
			listener.watchStarted();
		}
		this.isRunning = true;
	}

	public synchronized void stopOffset(long eventTime) {
		if (this.timer != null) {
			this.timer.cancel();
		}
		this.timer = null;
		this.lastEventTime = eventTime;
		this.lastEventTic = this.tics;
		this.tics = this.tics + (int) ( ( System.currentTimeMillis() - eventTime ) / 100 );
		for (StopwatchListener listener : this.listeners) {
			listener.watchStopped();
		}
		this.isRunning = false;
	}

	public synchronized void stop() {
		if (this.timer != null) {
			this.timer.cancel();
		}
		this.timer = null;
		this.lastEventTic = this.tics;
		for (StopwatchListener listener : this.listeners) {
			listener.watchStopped();
		}
		this.isRunning = false;
	}

	public synchronized void reset() {
		this.reset(System.currentTimeMillis());
	}

	public synchronized void reset(long eventTime) {
		if (this.timer != null) {
			this.timer.cancel();
		}
		this.timer = null;
		this.lastEventTime = eventTime;
		this.tics = this.timerLength * 10;
		this.lastEventTic = this.tics;
		for (StopwatchListener listener : this.listeners) {
			listener.watchReset();
		}
		this.isRunning = false;
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
		if (this.timer != null) {
			this.timer.cancel();
		}
		this.timer = null;
		this.timerLength = seconds;
		this.reset(eventTime);
	}

	public synchronized void addStopwatchListener(StopwatchListener listener) {
		this.listeners.add(listener);
		if (isRunning) {
			listener.watchStarted();
		} else {
			listener.watchStopped();
		}
	}

	public synchronized void removeStopwatchListener(StopwatchListener listener) {
		this.listeners.remove(listener);
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
		return this.isRunning;
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
				this.setTics(message.getLastEventTic());
				break;
			default:
				break;
		}
		// System.out.println("Remote event " + eventType.toString() + " processed");
		// System.out.println(" Server Time: " + new Date(message.getEventTime()));
		// System.out.println(" Local Time: " + new Date(eventTime));
		// System.out.println(" Now: " + new Date(System.currentTimeMillis()));
		// System.out.println(" Last Event Tic: " + message.getLastEventTic());
	}

	private class TicTok extends TimerTask {
		@Override
		public void run() {
			Stopwatch.this.tics--;
			if (Stopwatch.this.tics <= 0) {
				Stopwatch.this.timer.cancel();
				Stopwatch.this.tics = 0;
			}
			for (StopwatchListener listener : Stopwatch.this.listeners) {
				listener.tic(Stopwatch.this.tics);
			}
		}
	}

}