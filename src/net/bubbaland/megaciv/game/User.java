package net.bubbaland.megaciv.game;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

public class User implements Serializable {

	private static final long		serialVersionUID	= 1932880137949465272L;

	@JsonProperty("userName")
	private volatile String			userName;
	// Last time this client sent a command
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonProperty("lastActive")
	private volatile LocalDateTime	lastActive;

	public User() {
		this("", LocalDateTime.now(ZoneOffset.UTC));
	}

	@JsonCreator
	public User(@JsonProperty("userName") final String userName,
			@JsonProperty("lastActive") final LocalDateTime lastActive) {
		this.userName = userName;
		this.lastActive = lastActive;
	}

	public LocalDateTime getLastActive() {
		return this.lastActive;
	}

	public Duration timeSinceLastActive() {
		return Duration.between(this.lastActive, LocalDateTime.now(ZoneOffset.UTC));
	}

	public void updateActivity() {
		this.lastActive = LocalDateTime.now(ZoneOffset.UTC);
	}


	/**
	 * @return the user
	 */
	public String getUserName() {
		return this.userName;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	public void setUserName(final String user) {
		this.userName = user;
	}

	public int compareTo(final User otherUser) {
		return this.getUserName().compareTo(otherUser.getUserName());
	}

	@Override
	public String toString() {
		return this.userName;
	}

	public String fullString() {
		return this.userName + " Last Active: " + this.lastActive;
	}


}
