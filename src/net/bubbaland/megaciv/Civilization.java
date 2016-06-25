package net.bubbaland.megaciv;

import java.util.ArrayList;

public class Civilization {

	private String					name, player;
	private int						census;
	private ArrayList<Technology>	techs;

	public Civilization(String name, String player) {
		this.name = name;
		this.setPlayer(player);
		this.census = 1;
		this.techs = new ArrayList<Technology>();
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

}
