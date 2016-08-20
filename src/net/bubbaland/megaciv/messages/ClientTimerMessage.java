package net.bubbaland.megaciv.messages;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ClientTimerMessage extends ClientMessage implements TimerMessage {

	@JsonProperty("action")
	private final StopwatchEvent	eventType;
	@JsonProperty("timerLength")
	private final int				timerLength;
	// Timer start in server time
	@JsonProperty("eventTime")
	private final long				eventTime;

	@JsonCreator
	public ClientTimerMessage(@JsonProperty("action") StopwatchEvent action, @JsonProperty("eventTime") long timerStart,
			@JsonProperty("timerLength") int timerLength) {
		this.eventType = action;
		this.timerLength = timerLength;
		this.eventTime = timerStart;
	}

	public StopwatchEvent getEvent() {
		return this.eventType;
	}

	public int getTimerLength() {
		return this.timerLength;
	}

	@Override
	public long getEventTime() {
		return this.eventTime;
	}

	public String toString() {
		return "Client SNTP message\n" + " Action:" + eventType.toString() + " \n" + " Event Time: "
				+ new Date(eventTime) + "\n" + " Timer Length: " + timerLength;
	}
}
