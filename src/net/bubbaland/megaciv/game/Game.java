package net.bubbaland.megaciv.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Civilization.SortDirection;

public class Game implements Serializable {

	private static final long	serialVersionUID	= 3617165171580835437L;

	public static final int		MAX_CITIES			= 9;
	public static final int		MAX_POPULATION		= 55;

	public static enum Difficulty {
		BASIC, EXPERT
	}

	@JsonProperty("difficulty")
	private Difficulty difficulty;

	public void setDifficulty(Difficulty difficulty) {
		this.difficulty = difficulty;
	}

	public Difficulty getDifficulty() {
		return this.difficulty;
	}

	@JsonProperty("civs")
	private HashMap<Civilization.Name, Civilization>	civs;

	@JsonProperty("turnNumber")
	private int											turnNumber;

	public Game() {
		this.civs = new HashMap<Civilization.Name, Civilization>();
		this.turnNumber = 1;
		this.setDifficulty(Difficulty.BASIC);
	}

	@JsonCreator
	public Game(@JsonProperty("civs") HashMap<Civilization.Name, Civilization> civs,
			@JsonProperty("turnNumber") int turnNumber, @JsonProperty("difficulty") Difficulty difficulty) {
		this.civs = civs;
		this.turnNumber = turnNumber;
		this.difficulty = difficulty;
	}

	public void nextTurn() {
		this.turnNumber++;
		for (Civilization civ : this.civs.values()) {
			civ.setPurchased(false);
		}
	}

	public int getTurn() {
		return this.turnNumber;
	}

	public void addCivilization(Civilization.Name name) {
		Civilization civ = new Civilization(name, difficulty);
		this.civs.put(name, civ);
	}

	public void retireCivilization(Civilization.Name name) {
		this.civs.remove(name);
	}

	public void addCivilization(ArrayList<Civilization.Name> names) {
		for (Civilization.Name name : names) {
			this.addCivilization(name);
		}
	}

	public int lastAstStep() {
		switch (difficulty) {
			case BASIC:
				return 15;
			case EXPERT:
				return 16;
		}
		return -1;
	}

	public ArrayList<Civilization> getCivilizations() {
		return new ArrayList<Civilization>(this.civs.values());
	}

	public int getNCivilizations() {
		return this.civs.size();
	}

	public ArrayList<Civilization.Name> getCivilizationNames() {
		return new ArrayList<Civilization.Name>(this.civs.keySet());
	}

	public Civilization getCivilization(Civilization.Name name) {
		return this.civs.get(name);
	}

	public void setCivilization(Civilization civ) {
		this.civs.put(civ.getName(), civ);
	}

	public String toString() {
		String s = "Game Data:\n";
		s = s + "AST Difficulty: " + this.difficulty + "\n";
		for (Civilization civ : Civilization.sortByAst(this.getCivilizations(), SortDirection.ASCENDING)) {
			s = s + civ.toFullString() + "\n\n";
		}
		return s;
	}
}

