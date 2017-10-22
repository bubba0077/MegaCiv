package net.bubbaland.megaciv.game;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class GameEvent {

	public enum EventType {
		USER_CONNECT, GAME_START, CENSUS, CITY_COUNT, STOPWATCH, AST, TECH_PURCHASE, CIV_EDIT
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

	public GameEvent(EventType type, User user, String message) {
		this(Instant.now(), type, user, message);
	}

	@JsonCreator
	private GameEvent(@JsonProperty("timestamp") Instant timestamp, @JsonProperty("type") EventType type,
			@JsonProperty("user") User user, @JsonProperty("message") String message) {
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

	public String toString() {
		// TODO
		return "[" + dateFormat.format(this.timestamp) + "] " + user.getUserName() + ": " + message;
	}

}