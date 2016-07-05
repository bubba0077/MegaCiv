package net.bubbaland.megaciv;

import java.util.ArrayList;
import java.util.HashMap;

public class Game {

	private ArrayList<Civilization> civs;

	public Game(HashMap<String, String> startingCivs) {
		this.civs = new ArrayList<Civilization>();
		for (String civName : startingCivs.keySet()) {
			this.addCivilization(civName, startingCivs.get(civName));
		}
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
