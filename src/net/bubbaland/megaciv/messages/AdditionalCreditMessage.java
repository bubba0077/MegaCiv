package net.bubbaland.megaciv.messages;

import java.util.ArrayList;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.GameEvent;
import net.bubbaland.megaciv.game.GameEvent.EventType;
import net.bubbaland.megaciv.game.Technology;

public class AdditionalCreditMessage implements ClientMessage {

	@JsonProperty("civName")
	private final Civilization.Name				civName;
	@JsonProperty("tech")
	private final Technology					tech;
	@JsonProperty("credits")
	private final ArrayList<Technology.Type>	credits;

	@JsonCreator
	public AdditionalCreditMessage(@JsonProperty("civName") Civilization.Name civName,
			@JsonProperty("tech") Technology tech, @JsonProperty("credits") ArrayList<Technology.Type> credits) {
		this.civName = civName;
		this.tech = tech;
		this.credits = credits;
	}

	/**
	 * @return the civName
	 */
	public Civilization.Name getCivName() {
		return this.civName;
	}

	public Technology getTech() {
		return this.tech;
	}

	/**
	 * @return the credits
	 */
	public ArrayList<Technology.Type> getCredits() {
		return this.credits;
	}

	@Override
	public String toString() {
		return "Additional credits for " + this.civName.toString() + " provided by " + this.tech + " set to: "
				+ Arrays.toString(credits.toArray());
	}

	@Override
	public EventType getEventType() {
		return GameEvent.EventType.TECH_PURCHASE;
	}


}
