package net.bubbaland.megaciv.messages;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.User;

public class UserListMessage implements ServerMessage {

	@JsonProperty("userList")
	private final ArrayList<User> userList;

	@JsonCreator
	public UserListMessage(@JsonProperty("userList") ArrayList<User> userList) {
		this.userList = userList;
	}

	public ArrayList<User> getUserList() {
		return this.userList;
	}

}
