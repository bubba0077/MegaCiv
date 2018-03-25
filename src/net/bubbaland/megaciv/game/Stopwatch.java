package net.bubbaland.megaciv.game;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import net.bubbaland.megaciv.messages.StopwatchMessage;

public class Stopwatch {
	private Duration							timerLength;
	private volatile Instant					startTime;
	private volatile Duration					lastTimeRemaining;
	private Timer								timer;
	private Duration							offset;

	// Resolution of the underlying timer in milliseconds
	private static final int					TIMER_RESOLUTION	= 50;

	private final ArrayList<StopwatchListener>	listeners;

	public Stopwatch(Duration timerLength) {
		this.listeners = new ArrayList<StopwatchListener>();
		this.timer = null;
		this.timerLength = timerLength;
		this.startTime = Instant.now();
		this.lastTimeRemaining = this.timerLength;
		this.offset = Duration.ZERO;
	}

	/**
	 * Set the stopwatch as started at the given time
	 * 
	 * @param eventTime
	 *            Time stopwatch started
	 */
	private synchronized void start(Instant eventTime) {
		this.startTime = eventTime;
		this.timer = new Timer();
		this.timer.scheduleAtFixedRate(new TicTok(), 0, TIMER_RESOLUTION);
		this.listeners.parallelStream().forEach(l -> l.watchStarted());
	}

	/**
	 * Set the stopwatch as stopped
	 * 
	 * @param eventTime
	 *            Time when the stopwatch was stopped on server
	 */
	private synchronized void stop() {
		if (this.timer != null) {
			this.timer.cancel();
		}
		this.timer = null;
		this.listeners.parallelStream().forEach(l -> l.watchStopped());
	}

	/**
	 * Reset the stopwatch to the specific length
	 * 
	 * @param timerLength
	 *            Length of time for the stopwatch
	 */
	public synchronized void setTimer(Duration timerLength) {
		this.timerLength = timerLength;
		this.lastTimeRemaining = this.timerLength;
		this.stop();
	}

	/**
	 * Add a listener to act on stopwatch events.
	 * 
	 * @param listener
	 *            The listener to add
	 */
	public synchronized void addStopwatchListener(StopwatchListener listener) {
		this.listeners.add(listener);
		if (this.isRunning()) {
			listener.watchStarted();
		} else {
			listener.watchStopped();
		}
	}

	/**
	 * Remove a listener of stopwatch events.
	 * 
	 * @param listener
	 *            The listener to remove
	 */
	public synchronized void removeStopwatchListener(StopwatchListener listener) {
		this.listeners.remove(listener);
	}

	/**
	 * Get the length of the timer.
	 * 
	 * @return The timer length
	 */
	public Duration getTimerLength() {
		return this.timerLength;
	}

	/**
	 * Get the time remaining on the timer at the time specified.
	 * 
	 * @param eventTime
	 *            The time when we want to know the time left on the timer.
	 * @return The time left on the timer at the specified time.
	 */
	public Duration getTimeRemaining(Instant eventTime) {
		if (this.isRunning()) {
			// Time remaining is the difference between the start time and the event time
			return this.timerLength.minus(Duration.between(this.startTime, eventTime));
		} else {
			// Time remaining is what was remaining when the timer stopped or was reset
			return this.lastTimeRemaining;
		}
	}

	/**
	 * Get when the timer started
	 * 
	 * @return
	 */
	public Instant getStartTime() {
		return this.startTime;
	}

	/**
	 * Find out if the stopwatch is currently running.
	 * 
	 * @return Whether the stopwatch is currently running
	 */
	public boolean isRunning() {
		return this.timer != null;
	}

	/**
	 * Create a timer message to synchronize stopwatch events
	 * 
	 * @param eventType
	 *            Type of stopwatch event to generate a message for
	 * @param eventTime
	 *            Time the event occured
	 * @return A timer message of the desired stopwatch event with all of the necessary values set
	 */
	public StopwatchMessage generateTimerMessage(Stopwatch.StopwatchEvent eventType, Instant eventTime) {
		switch (eventType) {
			case START:
				// Fake the start time if the clock was restarted with some time already elapsed
				Duration alreadyElapsed = this.timerLength.minus(this.lastTimeRemaining);
				eventTime = eventTime.minus(alreadyElapsed);
				this.lastTimeRemaining = this.timerLength;
				// return new TimerMessage(eventType, this.timerLength, eventTime, this.timerLength);
				break;
			case STOP:
				this.lastTimeRemaining = this.getTimeRemaining(eventTime);
				// eventTime = null;
				// return new TimerMessage(eventType, this.timerLength, null, this.getTimeRemaining(eventTime));
				break;
			case SET:
				this.lastTimeRemaining = this.timerLength;
				// eventTime = null;
				// return new TimerMessage(eventType, this.timerLength, null, this.timerLength);
				break;
			default:
				return null;
		}
		eventTime = eventTime.plus(this.offset); // Convert the event time into server time
		// System.out.println("Stopwatch event message generated for " + eventTime);
		return new StopwatchMessage(eventType, this.timerLength, eventTime, this.lastTimeRemaining);
	}

	/**
	 * Synchronize this stopwatch to the information provided in a timer message.
	 * 
	 * @param message
	 *            Timer message with which to synchronize
	 */
	public synchronized void remoteEvent(StopwatchMessage message) {
		Stopwatch.StopwatchEvent eventType = message.getEvent();
		Instant eventTime = message.getEventTime().minus(this.offset); // Convert the event time into local time
		this.timerLength = message.getTimerLength();
		switch (eventType) {
			case START:
				this.start(eventTime);
				break;
			case STOP:
				this.lastTimeRemaining = message.getLastEventTimeRemaining();
				this.stop();
				break;
			case SET:
				this.setTimer(message.getTimerLength());
				break;
			default:
				break;
		}
		// System.out.println("Remote event " + eventType.toString() + " processed");
		// System.out.println(" Server Time: " + message.getEventTime());
		// System.out.println(" Local Time: " + eventTime);
		// System.out.println(" Now: " + Instant.now());
		// System.out.println(" Last Event Tic: " + message.getLastEventTic());
	}

	public void setServerOffset(Duration offset) {
		this.offset = offset;
		// System.out.println("Offset set: " + formatTimer(offset));
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
			Instant now = Instant.now();
			Duration timeRemaining = Stopwatch.this.getTimeRemaining(now);
			Duration roundedTimeRemaining = timeRemaining.plus(Duration.ofMillis((long) ( 0.5 * TIMER_RESOLUTION
					- ( ( timeRemaining.toMillis() + 0.5 * TIMER_RESOLUTION ) % TIMER_RESOLUTION ) )));

			if (timeRemaining.isZero() || timeRemaining.isNegative()) {
				Stopwatch.this.stop();
				Stopwatch.this.lastTimeRemaining = Duration.ZERO;
			}
			Stopwatch.this.listeners.parallelStream().forEach(l -> l.tic(roundedTimeRemaining));
		}
	}

	public enum StopwatchEvent {
		START, STOP, SET
	}

}