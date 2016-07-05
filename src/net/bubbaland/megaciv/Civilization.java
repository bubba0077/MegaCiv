package net.bubbaland.megaciv;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.Technology.Type;

public class Civilization implements Comparable<Civilization> {

	private static final HashMap<String, Integer> AST_TABLE;
	static {
		AST_TABLE = new HashMap<String, Integer>();
		AST_TABLE.put("Minoa", 1);
		AST_TABLE.put("Saba", 2);
		AST_TABLE.put("Assyria", 3);
		AST_TABLE.put("Maurya", 4);
		AST_TABLE.put("Celt", 5);
		AST_TABLE.put("Babylon", 6);
		AST_TABLE.put("Carthage", 7);
		AST_TABLE.put("Dravidia", 8);
		AST_TABLE.put("Hatti", 9);
		AST_TABLE.put("Kushan", 10);
		AST_TABLE.put("Rome", 11);
		AST_TABLE.put("Persia", 12);
		AST_TABLE.put("Iberia", 13);
		AST_TABLE.put("Nubia", 14);
		AST_TABLE.put("Hellas", 15);
		AST_TABLE.put("Indus", 16);
		AST_TABLE.put("Egypt", 17);
		AST_TABLE.put("Parthia", 18);
	}

	@JsonProperty("name")
	private String							name;
	@JsonProperty("player")
	private String							player;
	@JsonProperty("census")
	private int								census;
	@JsonProperty("astPosition")
	private int								astPosition;
	@JsonProperty("techs")
	private ArrayList<Technology>			techs;
	@JsonProperty("typeCredits")
	private final HashMap<Type, Integer>	typeCredits;

	public Civilization(String name, String player) {
		this(name, player, 1, new ArrayList<Technology>(), new HashMap<Type, Integer>(), 0);
	}

	public Civilization(@JsonProperty("name") String name, @JsonProperty("player") String player,
			@JsonProperty("census") int census, @JsonProperty("techs") ArrayList<Technology> techs,
			@JsonProperty("typeCredits") HashMap<Type, Integer> typeCredits,
			@JsonProperty("astPosition") int astPosition) {
		this.name = name;
		this.player = player;
		this.census = census;
		this.techs = techs;
		this.astPosition = astPosition;
		this.typeCredits = typeCredits;
	}

	public String getName() {
		return name;
	}

	public int getCensus() {
		return census;
	}

	public void setCensus(int census) {
		this.census = census;
	}

	public void getTech(Technology newTech) {
		this.techs.add(newTech);
	}

	public ArrayList<Technology> getTechs() {
		return this.techs;
	}

	public String getPlayer() {
		return player;
	}

	public void setPlayer(String player) {
		this.player = player;
	}

	public HashMap<Type, Integer> getTypeCredits() {
		return this.typeCredits;
	}

	public int getAST() {
		return AST_TABLE.get(this.name);
	}

	@Override
	/* Compare by AST Rank (default) */
	public int compareTo(Civilization otherCiv) {
		return Integer.compare(this.getAST(), otherCiv.getAST());
	}

}

class CensusComparator implements Comparator<Civilization> {
	public int compare(Civilization civ1, Civilization civ2) {
		int result = Integer.compare(civ1.getCensus(), civ2.getCensus());
		if (result == 0) {
			result = civ1.compareTo(civ2);
		}
		return result;
	}
}

class AstPositionComparator implements Comparator<Civilization> {
	public int compare(Civilization civ1, Civilization civ2) {
		return Integer.compare(civ1.getAST(), civ2.getAST());
	}
}
