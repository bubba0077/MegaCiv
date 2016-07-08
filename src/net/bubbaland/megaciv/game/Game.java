package net.bubbaland.megaciv.game;

import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Game {

	@JsonProperty("civs")
	private ArrayList<Civilization>	civs;
	@JsonProperty("turnNumber")
	private int						turnNumber;

	public Game(HashMap<String, String> startingCivs) {
		this.civs = new ArrayList<Civilization>();
		for (String civName : startingCivs.keySet()) {
			this.addCivilization(civName, startingCivs.get(civName));
		}
		this.turnNumber = 1;
	}

	public Game(@JsonProperty("civs") ArrayList<Civilization> civs, @JsonProperty("turnNumber") int turnNumber) {
		this.civs = civs;
		this.turnNumber = turnNumber;
	}

	private void addCivilization(String civName, String playerName) {
		this.civs.add(new Civilization(civName, playerName));
	}

	public ArrayList<Civilization> getCivilizations() {
		return this.civs;
	}

	public int nCivilizations() {
		return this.civs.size();
	}
}
