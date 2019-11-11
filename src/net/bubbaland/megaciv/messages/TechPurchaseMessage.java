package net.bubbaland.megaciv.messages;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.GameEvent;
import net.bubbaland.megaciv.game.GameEvent.EventType;
import net.bubbaland.megaciv.game.Technology;

public class TechPurchaseMessage implements ClientMessage {

	@JsonProperty("civName")
	private final Civilization.Name		civName;
	@JsonProperty("newTechs")
	private final ArrayList<Technology>	newTechs;

	@JsonCreator
	public TechPurchaseMessage(@JsonProperty("civName") final Civilization.Name civName,
			@JsonProperty("newTechs") final ArrayList<Technology> newTechs) {
		this.civName = civName;
		this.newTechs = newTechs;
	}

	public Civilization.Name getCivName() {
		return this.civName;
	}

	public ArrayList<Technology> getTechs() {
		return this.newTechs;
	}

	@Override
	public String toString() {
		return this.civName.toString() + " purchased the following technologies: " + this.newTechs.toString();
	}

	@Override
	public EventType getEventType() {
		return GameEvent.EventType.TECH_PURCHASE;
	}

}
