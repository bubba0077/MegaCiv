package net.bubbaland.megaciv.messages;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.GameEvent;
import net.bubbaland.megaciv.game.Technology;

public class TechPurchaseMessage extends ClientMessage {

	@JsonProperty("civName")
	private final Civilization.Name		civName;
	@JsonProperty("newTechs")
	private final ArrayList<Technology>	newTechs;

	@JsonCreator
	public TechPurchaseMessage(@JsonProperty("civName") Civilization.Name civName,
			@JsonProperty("newTechs") ArrayList<Technology> newTechs) {
		super(GameEvent.EventType.TECH_PURCHASE);
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
		// TODO Auto-generated method stub
		return null;
	}

}
