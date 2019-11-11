package net.bubbaland.megaciv.game;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class GameEvent {

	public enum EventType {
		USER_CONNECT, GAME_START, CENSUS, CITY_COUNT, STOPWATCH, AST, TECH_PURCHASE, CIV_EDIT, KEEPALIVE
	};

	@JsonProperty("type")
	private final EventType					type;
	@JsonProperty("user")
	private final User						user;
	@JsonProperty("timestamp")
	private final Instant					timestamp;
	@JsonProperty("message")
	private final String					message;

	// Date format to use
	static public final DateTimeFormatter	dateFormat	=
			DateTimeFormatter.ofPattern("yyyy MMM dd hh:mm:ss").withZone(ZoneId.systemDefault());

	public GameEvent(final EventType type, final User user, final String message) {
		this(Instant.now(), type, user, message);
	}

	@JsonCreator
	private GameEvent(@JsonProperty("timestamp") final Instant timestamp, @JsonProperty("type") final EventType type,
			@JsonProperty("user") final User user, @JsonProperty("message") final String message) {
		this.timestamp = timestamp;
		this.type = type;
		this.user = user;
		this.message = message;
	}

	/**
	 * @return the type
	 */
	public EventType getType() {
		return this.type;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return this.user;
	}

	/**
	 * @return the timestamp
	 */
	public Instant getTimestamp() {
		return this.timestamp;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return this.message;
	}

	@Override
	public String toString() {
		// TODO
		return "[" + dateFormat.format(this.timestamp) + " " + this.user.getUserName() + "]: " + this.message;
	}

}