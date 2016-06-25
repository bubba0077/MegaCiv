package net.bubbaland.megaciv;

import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.Technology.Type;

public class Civilization {

	@JsonProperty("name")
	private String							name;
	@JsonProperty("player")
	private String							player;
	@JsonProperty("census")
	private int								census;
	@JsonProperty("techs")
	private ArrayList<Technology>			techs;
	@JsonProperty("typeCredits")
	private final HashMap<Type, Integer>	typeCredits;

	public Civilization(String name, String player) {
		this.name = name;
		this.setPlayer(player);
		this.census = 1;
		this.techs = new ArrayList<Technology>();
		this.typeCredits = new HashMap<Type, Integer>();
	}

	public Civilization(@JsonProperty("name") String name, @JsonProperty("player") String player,
			@JsonProperty("census") int census, @JsonProperty("techs") ArrayList<Technology> techs,
			@JsonProperty("typeCredits") HashMap<Type, Integer> typeCredits) {
		this.name = name;
		this.player = player;
		this.census = census;
		this.techs = techs;
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

}
