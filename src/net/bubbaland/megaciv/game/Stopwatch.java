package net.bubbaland.megaciv.game;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import net.bubbaland.megaciv.messages.TimerMessage;
import net.bubbaland.megaciv.messages.TimerMessage.StopwatchEvent;

public class Stopwatch {
	private Duration							timerLength;
	private volatile Instant					lastEventTime;
	private volatile Duration					lastEventTic;
	private volatile Duration					tics;
	private Timer								timer;
	private boolean								isRunning;

	private final ArrayList<StopwatchListener>	listeners;

	public Stopwatch() {
		this(300);
	}

	public Stopwatch(int seconds) {
		this(Duration.ofSeconds(seconds));
	}

	public Stopwatch(Duration timerLength) {
		this.listeners = new ArrayList<StopwatchListener>();
		this.timer = null;
		this.timerLength = timerLength;
		this.tics = Duration.from(this.timerLength);
		this.lastEventTime = Instant.now();
		this.lastEventTic = this.tics;
		this.isRunning = false;
	}

	public synchronized void startOffset(Instant eventTime) {
		this.timer = new Timer();
		this.lastEventTime = eventTime;
		this.lastEventTic = this.tics;
		this.tics = this.tics.minus(Duration.between(eventTime, Instant.now()));
		// this.tics - (int) ( ( System.currentTimeMillis() - eventTime ) / 100 );
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

	public synchronized void stopOffset(Instant eventTime) {
		if (this.timer != null) {
			this.timer.cancel();
		}
		this.timer = null;
		this.lastEventTime = eventTime;
		this.lastEventTic = this.tics;
		this.tics = this.tics.plus(Duration.between(eventTime, Instant.now()));
		// this.tics = this.tics + (int) ( ( System.currentTimeMillis() - eventTime ) / 100 );
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
		this.reset(Instant.now());
	}

	public synchronized void reset(Instant eventTime) {
		if (this.timer != null) {
			this.timer.cancel();
		}
		this.timer = null;
		this.lastEventTime = eventTime;
		this.tics = Duration.from(this.timerLength);
		this.lastEventTic = this.tics;
		for (StopwatchListener listener : this.listeners) {
			listener.watchReset();
		}
		this.isRunning = false;
	}

	public Duration getLastEventTic() {
		return this.lastEventTic;
	}

	public synchronized void setTics(Duration tic) {
		this.tics = tic;
		this.lastEventTic = tic;
	}

	public synchronized void setTimer(Duration timerLength) {
		this.setTimer(Instant.now(), timerLength);
	}

	public synchronized void setTimer(Instant eventTime, Duration timerLength) {
		if (this.timer != null) {
			this.timer.cancel();
		}
		this.timer = null;
		this.timerLength = timerLength;
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

	public Duration getTimerLength() {
		return this.timerLength;
	}

	public Duration getTicsRemaining() {
		return this.tics;
	}

	public Instant getLastEventTime() {
		return this.lastEventTime;
	}

	public boolean isRunning() {
		return this.isRunning;
	}

	public synchronized void remoteEvent(TimerMessage message, Duration offset) {
		StopwatchEvent eventType = message.getEvent();
		Instant eventTime = message.getEventTime().minus(offset);
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
			default:
				break;
		}
		// System.out.println("Remote event " + eventType.toString() + " processed");
		// System.out.println(" Server Time: " + new Date(message.getEventTime()));
		// System.out.println(" Local Time: " + new Date(eventTime));
		// System.out.println(" Now: " + new Date(System.currentTimeMillis()));
		// System.out.println(" Last Event Tic: " + message.getLastEventTic());
	}

	public static String formatTimer(Duration timeRemaining) {
		int min = (int) timeRemaining.toMinutes();
		int sec = (int) timeRemaining.minus(Duration.ofMinutes(min)).getSeconds();
		int tenths = (int) timeRemaining.minus(Duration.ofMinutes(min)).minus(Duration.ofSeconds(sec)).toMillis() / 100;
		return String.format("%02d", min) + ":" + String.format("%02d", sec) + "." + tenths;
	}

	private class TicTok extends TimerTask {
		@Override
		public void run() {
			Stopwatch.this.tics = Stopwatch.this.tics.minusMillis(100);
			if (Stopwatch.this.tics.isZero() || Stopwatch.this.tics.isNegative()) {
				Stopwatch.this.timer.cancel();
				Stopwatch.this.tics = Duration.ZERO;
			}
			for (StopwatchListener listener : Stopwatch.this.listeners) {
				listener.tic(Stopwatch.this.tics);
			}
		}
	}

}