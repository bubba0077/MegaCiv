package net.bubbaland.megaciv.client.messages;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Technology;

public class AdditionalCreditMessage extends ClientMessage {

	@JsonProperty("civName")
	private final Civilization.Name					civName;
	@JsonProperty("credits")
	private final HashMap<Technology.Type, Integer>	credits;

	@JsonCreator
	public AdditionalCreditMessage(@JsonProperty("civName") Civilization.Name civName,
			@JsonProperty("credits") HashMap<Technology.Type, Integer> credits) {
		this.civName = civName;
		this.credits = credits;
	}

	/**
	 * @return the civName
	 */
	public Civilization.Name getCivName() {
		return this.civName;
	}

	/**
	 * @return the credits
	 */
	public HashMap<Technology.Type, Integer> getCredits() {
		return this.credits;
	}


}
