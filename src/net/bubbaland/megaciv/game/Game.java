package net.bubbaland.megaciv.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Game implements Serializable {

	private static final long	serialVersionUID	= 3617165171580835437L;

	@JsonProperty("version")
	private int					version;

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
		this.version = 0;
		this.setDifficulty(Difficulty.BASIC);
	}

	@JsonCreator
	public Game(@JsonProperty("civs") HashMap<Civilization.Name, Civilization> civs,
			@JsonProperty("turnNumber") int turnNumber, @JsonProperty("difficulty") Difficulty difficulty,
			@JsonProperty("version") int version) {
		this.civs = civs;
		this.turnNumber = turnNumber;
		this.version = version;
		this.difficulty = difficulty;
	}

	public void addCivilization(Civilization.Name name) {
		Civilization civ = new Civilization(name, difficulty);
		this.civs.put(name, civ);
		this.version++;
	}

	public void addCivilization(ArrayList<Civilization.Name> names) {
		for (Civilization.Name name : names) {
			this.addCivilization(name);
		}
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

	public static String capitalizeFirst(String str) {
		return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
	}

	public String toString() {
		String s = "Game Data:\n";
		s = s + "AST Difficulty: " + this.difficulty + "\n";
		for (Civilization civ : Civilization.sortByAst(this.getCivilizations())) {
			s = s + civ.toFullString() + "\n\n";
		}
		return s;
	}
}

