package net.bubbaland.megaciv.client.messages;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Civilization;

public class AddCivilizationMessage extends ClientMessage {

	@JsonProperty("newCivNames")
	private final ArrayList<Civilization.Name> newCivNames;

	@JsonCreator
	public AddCivilizationMessage(@JsonProperty("newCivNames") ArrayList<Civilization.Name> newCivNames) {
		super();
		this.newCivNames = newCivNames;
	}

	public ArrayList<Civilization.Name> getCivNames() {
		return this.newCivNames;
	}

}
