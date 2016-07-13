package net.bubbaland.megaciv.game;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Civilization.AstTableData.AstSubTableData;
import net.bubbaland.megaciv.game.Game.Difficulty;

public class Civilization implements Serializable, Comparable<Civilization> {

	private static final long serialVersionUID = -9210563148479097901L;

	public static enum Name {
		MINOA, SABA, ASSYRIA, MAURYA, CELT, BABYLON, CARTHAGE, DRAVIDIA, HATTI, KUSHAN, ROME, PERSIA, IBERIA, NUBIA, HELLAS, INDUS, EGYPT, PARTHIA
	}

	public static enum Region {
		EAST, WEST
	};

	public static enum Age {
		STONE, EARLY_BRONZE, MIDDLE_BRONZE, LATE_BRONZE, EARLY_IRON, LATE_IRON
	}

	public static enum SortOption {
		AST, POPULATION, AST_POSITION;
	}

	public final static HashMap<Civilization.Name, AstTableData>	astTable;

	private HashMap<Civilization.Name, Color>						civColors;

	public final static String										CIV_CONSTANTS_FILENAME	= "Civ_Constants.xml";


	static {
		astTable = new HashMap<Civilization.Name, AstTableData>();

		try {
			final InputStream fileStream = Civilization.class.getResourceAsStream(CIV_CONSTANTS_FILENAME);

			final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			final Document doc = dBuilder.parse(fileStream);
			doc.getDocumentElement().normalize();
			final Element astTableElement = doc.getDocumentElement();
			NodeList civNodes = astTableElement.getElementsByTagName("Civilization");

			for (int c = 0; c < civNodes.getLength(); c++) {
				final Element civElement = (Element) civNodes.item(c);
				final Civilization.Name name = Civilization.Name.valueOf(civElement.getAttribute("name").toUpperCase());
				Region region = null;
				switch (civElement.getElementsByTagName("Region").item(0).getTextContent()) {
					case "East":
						region = Region.EAST;
					case "West":
						region = Region.WEST;
				}
				final int astRank = Integer
						.parseInt(civElement.getElementsByTagName("AstRank").item(0).getTextContent());

				HashMap<Difficulty, AstSubTableData> hash = new HashMap<Difficulty, AstSubTableData>();
				for (Difficulty difficulty : Game.Difficulty.values()) {
					final Element civAstElement = (Element) civElement
							.getElementsByTagName(Game.capitalizeFirst(difficulty.name())).item(0);
					final int earlyBronzeStart = Integer
							.parseInt(civAstElement.getElementsByTagName("EarlyBronzeStart").item(0).getTextContent());
					final int middleBronzeStart = Integer
							.parseInt(civAstElement.getElementsByTagName("MiddleBronzeStart").item(0).getTextContent());
					final int lateBronzeStart = Integer
							.parseInt(civAstElement.getElementsByTagName("LateBronzeStart").item(0).getTextContent());
					final int earlyIronStart = Integer
							.parseInt(civAstElement.getElementsByTagName("EarlyIronStart").item(0).getTextContent());
					final int lateIronStart = Integer
							.parseInt(civAstElement.getElementsByTagName("LateIronStart").item(0).getTextContent());


					hash.put(difficulty, new AstSubTableData(earlyBronzeStart, middleBronzeStart, lateBronzeStart,
							earlyIronStart, lateIronStart));
				}

				astTable.put(name, new AstTableData(astRank, region, hash));
			}

		} catch (final ParserConfigurationException | SAXException | IOException e) {

		}
	}

	@JsonProperty("name")
	private Name									name;
	@JsonProperty("player")
	private String									player;
	@JsonProperty("population")
	private int										population;
	@JsonProperty("astPosition")
	private int										astPosition;
	@JsonProperty("nCities")
	private int										nCities;
	@JsonProperty("techs")
	private ArrayList<Technology>					techs;
	@JsonProperty("typeCredits")
	private final HashMap<Technology.Type, Integer>	typeCredits;
	@JsonProperty("difficulty")
	private Difficulty								difficulty;

	public Civilization(Name name, Difficulty difficulty) {
		this(name, null, 1, 0, new ArrayList<Technology>(), new HashMap<Technology.Type, Integer>(), 0, difficulty);
	}

	@JsonCreator
	private Civilization(@JsonProperty("name") Name name, @JsonProperty("player") String player,
			@JsonProperty("population") int population, @JsonProperty("nCities") int nCities,
			@JsonProperty("techs") ArrayList<Technology> techs,
			@JsonProperty("typeCredits") HashMap<Technology.Type, Integer> typeCredits,
			@JsonProperty("astPosition") int astPosition, @JsonProperty("difficulty") Difficulty difficulty) {
		this.name = name;
		this.player = player;
		this.population = population;
		this.nCities = nCities;
		this.techs = techs;
		this.astPosition = astPosition;
		this.typeCredits = typeCredits;
		this.difficulty = difficulty;
	}

	public Name getName() {
		return this.name;
	}

	public String toString() {
		return Game.capitalizeFirst(this.name.name());
	}

	public void incrementAST() {
		this.astPosition++;
	}

	public void decrementAST() {
		this.astPosition--;
	}

	public Age getAge(int astStep) {
		return astStep >= this.getLateIronStart() ? Age.LATE_IRON : astStep >= this
				.getEarlyIronStart() ? Age.EARLY_IRON : astStep >= this
						.getLateBronzeStart() ? Age.LATE_BRONZE : astStep >= this
								.getMiddleBronzeStart() ? Age.MIDDLE_BRONZE : astStep >= this
										.getEarlyBronzeStart() ? Age.EARLY_BRONZE : Age.STONE;
	}

	public Age getCurrentAge() {
		return this.getAge(this.astPosition);
	}

	public Age getNextStepAge() {
		return this.astPosition + 1 >= this.getLateIronStart() ? Age.LATE_IRON : this.astPosition >= this
				.getEarlyIronStart() ? Age.EARLY_IRON : this.astPosition >= this
						.getLateBronzeStart() ? Age.LATE_BRONZE : this.astPosition >= this
								.getMiddleBronzeStart() ? Age.MIDDLE_BRONZE : this.astPosition >= this
										.getEarlyBronzeStart() ? Age.EARLY_BRONZE : Age.STONE;
	}

	public void setDifficulty(Difficulty difficulty) {
		this.difficulty = difficulty;
	}

	public int getPopulation() {
		return this.population;
	}

	public void setPopulation(int population) {
		this.population = population;
	}

	public int getCityCount() {
		return this.nCities;
	}

	public void setCityCount(int nCities) {
		this.nCities = nCities;
	}

	public void addTech(Technology newTech) {
		this.techs.add(newTech);
	}

	public ArrayList<Technology> getTechs() {
		return this.techs;
	}

	public String getPlayer() {
		return this.player;
	}

	public void setPlayer(String player) {
		this.player = player;
	}

	public HashMap<Technology.Type, Integer> getTypeCredits() {
		return this.typeCredits;
	}

	public void addTypeCredits(HashMap<Technology.Type, Integer> newCredits) {
		for (Technology.Type type : newCredits.keySet()) {
			this.typeCredits.put(type, this.typeCredits.get(type) + newCredits.get(type));
		}
	}

	public int getAstPosition() {
		return this.astPosition;
	}

	public int getAst() {
		return astTable.get(this.name).astRank;
	}

	public int getEarlyBronzeStart() {
		return astTable.get(this.name).getEarlyBronzeStart(difficulty);
	}

	public int getMiddleBronzeStart() {
		return astTable.get(this.name).getMiddleBronzeStart(difficulty);
	}

	public int getLateBronzeStart() {
		return astTable.get(this.name).getLateBronzeStart(difficulty);
	}

	public int getEarlyIronStart() {
		return astTable.get(this.name).getEarlyIronStart(difficulty);
	}

	public int getLateIronStart() {
		return astTable.get(this.name).getLateIronStart(difficulty);
	}

	public static ArrayList<Civilization> sortBy(ArrayList<Civilization> civs, Civilization.SortOption sort) {
		switch (sort) {
			case AST:
				return sortByAst(civs);
			case POPULATION:
				return sortByCensus(civs);
			case AST_POSITION:
				return sortByAstPosition(civs);
		}
		return null;
	}

	public static ArrayList<Civilization.Name> sortByToName(ArrayList<Civilization> civs,
			Civilization.SortOption sort) {
		ArrayList<Civilization> sortedCivs = sortBy(civs, sort);
		ArrayList<Civilization.Name> sortedNames = new ArrayList<Civilization.Name>();
		for (Civilization civ : sortedCivs) {
			sortedNames.add(civ.getName());
		}
		return sortedNames;
	}

	public static ArrayList<Civilization> sortByAst(ArrayList<Civilization> civs) {
		Collections.sort(civs);
		return civs;
	}

	public static ArrayList<Civilization> sortByCensus(ArrayList<Civilization> civs) {
		Collections.sort(civs, new CensusComparator());
		return civs;
	}

	public static ArrayList<Civilization> sortByAstPosition(ArrayList<Civilization> civs) {
		Collections.sort(civs, new AstPositionComparator());
		return civs;
	}

	public int getVP() {
		int vp = this.nCities + this.astPosition * 5;
		for (Technology tech : this.techs) {
			vp = +tech.getVP();
		}
		// Need to add adjustment for Late Iron Age only bonus
		return vp;
	}

	public String toFullString() {
		String s = this.toString() + " (" + this.player + ")\n";
		s = s + "Current Score: " + this.getVP() + "\n";
		s = s + "Current AST Step: " + this.astPosition + "(" + this.getCurrentAge() + ")\n";
		s = s + "Next Step Age: " + this.getNextStepAge() + "\n";
		s = s + "Cities: " + this.nCities + " Population: " + this.population + "\n";
		s = s + "Technologies:" + String.join(this.techs.toString());
		return s;
	}

	private final static class CensusComparator implements Comparator<Civilization> {
		public int compare(Civilization civ1, Civilization civ2) {
			int result = Integer.compare(civ1.getPopulation(), civ2.getPopulation());
			if (result == 0) {
				result = civ1.compareTo(civ2);
			}
			return result;
		}
	}

	private final static class AstPositionComparator implements Comparator<Civilization> {
		public int compare(Civilization civ1, Civilization civ2) {
			return Integer.compare(civ1.astPosition, civ2.astPosition);
		}
	}

	@Override
	public int compareTo(Civilization otherCiv) {
		int result = this.name.compareTo(otherCiv.name);
		return result;
	}

	static final class AstTableData {
		@JsonProperty("astRank")
		public final int										astRank;
		@JsonProperty("region")
		public final Region										region;
		@JsonProperty("subTable")
		public final HashMap<Game.Difficulty, AstSubTableData>	subTable;

		@JsonCreator
		public AstTableData(@JsonProperty("astRank") final int astRank, @JsonProperty("region") final Region region,
				@JsonProperty("subTable") HashMap<Game.Difficulty, AstSubTableData> subTable) {
			this.astRank = astRank;
			this.region = region;
			this.subTable = subTable;
		}

		public int getAstRank() {
			return this.astRank;
		}

		public Region getRegion() {
			return this.region;
		}

		public int getEarlyBronzeStart(Difficulty difficulty) {
			return this.subTable.get(difficulty).earlyBronzeStart;
		}

		public int getMiddleBronzeStart(Difficulty difficulty) {
			return this.subTable.get(difficulty).middleBronzeStart;
		}

		public int getLateBronzeStart(Difficulty difficulty) {
			return this.subTable.get(difficulty).lateBronzeStart;
		}

		public int getEarlyIronStart(Difficulty difficulty) {
			return this.subTable.get(difficulty).earlyIronStart;
		}

		public int getLateIronStart(Difficulty difficulty) {
			return this.subTable.get(difficulty).lateIronStart;
		}

		static final class AstSubTableData {
			@JsonProperty("earlyBronzeStart")
			public final int	earlyBronzeStart;
			@JsonProperty("middleBronzeStart")
			public final int	middleBronzeStart;
			@JsonProperty("lateBronzeStart")
			public final int	lateBronzeStart;
			@JsonProperty("earlyIronStart")
			public final int	earlyIronStart;
			@JsonProperty("lateIronStart")
			public final int	lateIronStart;

			private AstSubTableData(@JsonProperty("earlyBronzeStart") final int earlyBronzeStart,
					@JsonProperty("middleBronzeStart") final int middleBronzeStart,
					@JsonProperty("lateBronzeStart") final int lateBronzeStart,
					@JsonProperty("earlyIronStart") final int earlyIronStart,
					@JsonProperty("lateIronStart") final int lateIronStart) {
				this.earlyBronzeStart = earlyBronzeStart;
				this.middleBronzeStart = middleBronzeStart;
				this.lateBronzeStart = lateBronzeStart;
				this.earlyIronStart = earlyIronStart;
				this.lateIronStart = lateIronStart;
			}
		}

	}


}

