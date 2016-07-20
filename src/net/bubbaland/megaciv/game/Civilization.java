package net.bubbaland.megaciv.game;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Game.Difficulty;

public class Civilization implements Serializable, Comparable<Civilization> {

	private static final long serialVersionUID = -9210563148479097901L;

	public static enum Name {
		MINOA, SABA, ASSYRIA, MAURYA, CELT, BABYLON, CARTHAGE, DRAVIDIA, HATTI, KUSHAN, ROME, PERSIA, IBERIA, NUBIA, HELLAS, INDUS, EGYPT, PARTHIA;

		public final static HashSet<String> strings = new HashSet<String>() {
			private static final long serialVersionUID = -4960803070653152009L;

			{
				for (Civilization.Name name : EnumSet.allOf(Name.class)) {
					add(name.toString());
				}
			}
		};

		public static boolean contains(String s) {
			for (String s1 : strings) {
				if (s1.equalsIgnoreCase(s)) {
					return true;
				}
			}
			return false;
		}
	}

	public static enum Region {
		EAST, WEST, BOTH
	};

	public static enum Age {
		STONE, EARLY_BRONZE, MIDDLE_BRONZE, LATE_BRONZE, EARLY_IRON, LATE_IRON {
			@Override
			public Age nextAge() {
				return null;
			}
		};

		public Age nextAge() {
			return values()[this.ordinal() + 1];
		}

	}

	public static enum SortOption {
		AST, POPULATION, CITIES, AST_POSITION, VP, MOVEMENT;
	}

	public static enum SortDirection {
		ASCENDING, DESCENDING;
	}

	public final static HashMap<Game.Difficulty, HashMap<Civilization.Age, AstRequirements>>			AGE_REQUIREMENTS;

	public final static HashMap<Civilization.Name, AstTableData>										AST_TABLE;
	public final static HashMap<Civilization.Name, Color>												FOREGROUND_COLORS,
			BACKGROUND_COLORS;
	public final static HashMap<Integer, HashMap<Civilization.Region, ArrayList<Civilization.Name>>>	DEFAULT_STARTING_CIVS;

	public final static String																			CIV_CONSTANTS_FILENAME	= "Civ_Constants.xml";

	static {
		AST_TABLE = new HashMap<Civilization.Name, AstTableData>();
		DEFAULT_STARTING_CIVS = new HashMap<Integer, HashMap<Civilization.Region, ArrayList<Civilization.Name>>>();
		AGE_REQUIREMENTS = new HashMap<Game.Difficulty, HashMap<Civilization.Age, AstRequirements>>();

		FOREGROUND_COLORS = new HashMap<Civilization.Name, Color>();
		BACKGROUND_COLORS = new HashMap<Civilization.Name, Color>();

		try {
			final InputStream fileStream = Civilization.class.getResourceAsStream(CIV_CONSTANTS_FILENAME);

			final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			final Document doc = dBuilder.parse(fileStream);
			doc.getDocumentElement().normalize();
			final Element astTableElement = (Element) doc.getDocumentElement().getElementsByTagName("AstTable").item(0);
			NodeList civNodes = astTableElement.getElementsByTagName("Civilization");

			for (int c = 0; c < civNodes.getLength(); c++) {
				final Element civElement = (Element) civNodes.item(c);
				final Civilization.Name name = Civilization.Name.valueOf(civElement.getAttribute("name").toUpperCase());
				Region region = Region
						.valueOf(civElement.getElementsByTagName("Region").item(0).getTextContent().toUpperCase());
				final int astRank = Integer
						.parseInt(civElement.getElementsByTagName("AstRank").item(0).getTextContent());


				HashMap<Difficulty, HashMap<Age, Integer>> hash = new HashMap<Difficulty, HashMap<Age, Integer>>();
				for (Difficulty difficulty : EnumSet.allOf(Game.Difficulty.class)) {
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

					hash.put(difficulty, new HashMap<Age, Integer>() {
						private static final long serialVersionUID = 1L;

						{
							put(Age.STONE, 0);
							put(Age.EARLY_BRONZE, earlyBronzeStart);
							put(Age.MIDDLE_BRONZE, middleBronzeStart);
							put(Age.LATE_BRONZE, lateBronzeStart);
							put(Age.EARLY_IRON, earlyIronStart);
							put(Age.LATE_IRON, lateIronStart);

						}
					});
				}

				AST_TABLE.put(name, new AstTableData(astRank, region, hash));

				FOREGROUND_COLORS
						.put(name,
								new Color(new BigInteger(
										civElement.getElementsByTagName("Foreground").item(0).getTextContent(), 16)
												.intValue()));
				BACKGROUND_COLORS
						.put(name,
								new Color(new BigInteger(
										civElement.getElementsByTagName("Background").item(0).getTextContent(), 16)
												.intValue()));
			}

			final Element startingCivElement = (Element) doc.getDocumentElement().getElementsByTagName("StartingCivs")
					.item(0);
			NodeList startingNodes = startingCivElement.getElementsByTagName("PlayerCount");

			for (int i = 0; i < startingNodes.getLength(); i++) {
				final Element startingElement = (Element) startingNodes.item(i);
				final int nCivs = Integer.parseInt(startingElement.getAttribute("count"));
				NodeList regionsNodes = startingElement.getElementsByTagName("Region");

				HashMap<Region, ArrayList<Name>> regionHash = new HashMap<Region, ArrayList<Name>>();

				for (int node = 0; node < regionsNodes.getLength(); node++) {
					Element regionElement = ( (Element) regionsNodes.item(node) );
					Region region = Region.valueOf(regionElement.getAttribute("name").toUpperCase());

					ArrayList<Name> civs = new ArrayList<Name>();

					civNodes = regionElement.getElementsByTagName("Civilization");

					for (int c = 0; c < civNodes.getLength(); c++) {
						final Element civElement = (Element) civNodes.item(c);
						final Civilization.Name name = Civilization.Name
								.valueOf(civElement.getAttribute("name").toUpperCase());
						civs.add(name);
					}

					regionHash.put(region, civs);
				}

				DEFAULT_STARTING_CIVS.put(nCivs, regionHash);
			}

			final Element requirementElement = (Element) doc.getDocumentElement()
					.getElementsByTagName("AstRequirements").item(0);
			NodeList difficultyNodes = requirementElement.getElementsByTagName("Difficulty");
			for (int d = 0; d < difficultyNodes.getLength(); d++) {
				Difficulty difficulty = Difficulty.valueOf(( (Element) difficultyNodes.item(d) ).getAttribute("level"));
				NodeList ageNodes = requirementElement.getElementsByTagName("Age");

				HashMap<Age, AstRequirements> astReqs = new HashMap<Age, AstRequirements>();
				for (int a = 0; a < ageNodes.getLength(); a++) {
					Element ageElement = ( (Element) ageNodes.item(a) );
					Age age = Age.valueOf(ageElement.getAttribute("name"));
					String reqText = ageElement.getElementsByTagName("Text").item(0).getTextContent();
					int minCities = Integer
							.parseInt(ageElement.getElementsByTagName("MinCities").item(0).getTextContent());
					int minAdvances = Integer
							.parseInt(ageElement.getElementsByTagName("MinAdvances").item(0).getTextContent());
					int minLevelOneTechs = Integer
							.parseInt(ageElement.getElementsByTagName("MinL1Techs").item(0).getTextContent());
					int minLevelTwoPlusTechs = Integer
							.parseInt(ageElement.getElementsByTagName("MinL2PlusTechs").item(0).getTextContent());
					int minLevelThreeTechs = Integer
							.parseInt(ageElement.getElementsByTagName("MinL3Techs").item(0).getTextContent());
					int minTechVP = Integer
							.parseInt(ageElement.getElementsByTagName("MinTechVP").item(0).getTextContent());
					astReqs.put(age, new AstRequirements(reqText, minCities, minAdvances, minLevelOneTechs,
							minLevelTwoPlusTechs, minLevelThreeTechs, minTechVP));
				}
				AGE_REQUIREMENTS.put(difficulty, astReqs);
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
	private final HashMap<Technology.Type, Integer>	extraTypeCredits;
	@JsonProperty("difficulty")
	private Difficulty								difficulty;

	public Civilization(Name name, Difficulty difficulty) {
		this(name, null, 1, 0, new ArrayList<Technology>(), new HashMap<Technology.Type, Integer>() {
			private static final long serialVersionUID = 6611228131955386821L;

			{
				for (Technology.Type type : EnumSet.allOf(Technology.Type.class)) {
					put(type, 0);
				}
			}
		}, 0, difficulty);
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
		this.extraTypeCredits = typeCredits;
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
		Age age = Age.STONE;
		for (Age a : EnumSet.allOf(Age.class)) {
			if (astStep >= this.getAgeStart(a)) {
				age = a;
			} else {
				return age;
			}
		}
		return age;
	}

	public Age getCurrentAge() {
		return this.getAge(this.astPosition);
	}

	public Age getNextStepAge() {
		return this.getAge(this.astPosition + 1);
	}

	public int getTechCountByVP(int vp) {
		int n = 0;
		for (Technology tech : this.techs) {
			if (tech.getVP() == vp) {
				n++;
			}
		}
		return n;
	}

	public boolean passAstReqirements() {
		AstRequirements reqs = AGE_REQUIREMENTS.get(this.difficulty).get(this.getNextStepAge());
		return this.getCityCount() >= reqs.getMinCities() && this.techs.size() >= reqs.getMinAdvances()
				&& this.getTechCountByVP(1) >= reqs.getMinLevelOneTechs()
				&& this.getTechCountByVP(3) + this.getTechCountByVP(6) >= reqs.getMinLevelTwoPlusTechs()
				&& this.getTechCountByVP(6) >= reqs.getMinLevelThreeTechs()
				&& this.getVPfromTech() >= reqs.getMinTechVP();
	}

	public int getAgeStart(Civilization.Age age) {
		return Civilization.AST_TABLE.get(this.getName()).getAgeStart(age, this.difficulty);
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

	public Civilization clone() {
		ArrayList<Technology> techs = new ArrayList<Technology>() {
			private static final long serialVersionUID = 577732084086917712L;

			{
				for (Technology tech : Civilization.this.techs) {
					add(tech);
				}
			}
		};
		HashMap<Technology.Type, Integer> extraTypeCredits = new HashMap<Technology.Type, Integer>() {
			private static final long serialVersionUID = 1L;
			{
				for (Technology.Type type : Technology.Type.values()) {
					put(type, Civilization.this.extraTypeCredits.get(type));
				}
			}
		};
		return new Civilization(this.name, this.player, this.population, this.nCities, techs, extraTypeCredits,
				this.astPosition, this.difficulty);
	}

	public ArrayList<Technology> getTechs() {
		return this.techs;
	}

	public boolean hasTech(Technology tech) {
		return this.techs.contains(tech);
	}

	public String getPlayer() {
		return this.player;
	}

	public void setPlayer(String player) {
		this.player = player;
	}

	public int getTypeCredit(Technology.Type type) {
		int credit = this.extraTypeCredits.get(type);
		for (Technology tech : this.techs) {
			credit = credit + tech.getTypeCredit(type);
		}
		return credit;
	}

	public HashMap<Technology.Type, Integer> getExtraTypeCredits() {
		return this.extraTypeCredits;
	}

	public void addTypeCredits(HashMap<Technology.Type, Integer> newCredits) {
		for (Technology.Type type : newCredits.keySet()) {
			this.extraTypeCredits.put(type, this.extraTypeCredits.get(type) + newCredits.get(type));
		}
	}

	public int getAstPosition() {
		return this.astPosition;
	}

	public void setAstPosition(int newAstPosition) {
		this.astPosition = newAstPosition;
	}

	public int getAst() {
		return AST_TABLE.get(this.name).astRank;
	}

	public static ArrayList<Civilization> sortBy(ArrayList<Civilization> civs, Civilization.SortOption sort,
			SortDirection direction) {
		switch (sort) {
			case AST:
				return sortByAst(civs, direction);
			case CITIES:
				return sortByCities(civs, direction);
			case POPULATION:
				return sortByCensus(civs, direction);
			case AST_POSITION:
				return sortByAstPosition(civs, direction);
			case VP:
				return sortByVP(civs, direction);
			case MOVEMENT:
				return sortByMovement(civs, direction);
		}
		return null;
	}

	public static ArrayList<Civilization> sortByVP(ArrayList<Civilization> civs, SortDirection direction) {
		Collections.sort(civs, new VpComparator());
		if (direction == SortDirection.DESCENDING) {
			Collections.reverse(civs);
		}
		return civs;
	}

	public static ArrayList<Civilization> sortByCities(ArrayList<Civilization> civs, SortDirection direction) {
		Collections.sort(civs, new CityComparator());
		if (direction == SortDirection.DESCENDING) {
			Collections.reverse(civs);
		}
		return civs;
	}

	public static ArrayList<Civilization> sortByMovement(ArrayList<Civilization> civs, SortDirection direction) {
		Collections.sort(civs, new MovementComparator());
		if (direction == SortDirection.DESCENDING) {
			Collections.reverse(civs);
		}
		return civs;
	}

	public static ArrayList<Civilization.Name> sortByToName(ArrayList<Civilization> civs, Civilization.SortOption sort,
			SortDirection direction) {
		ArrayList<Civilization> sortedCivs = sortBy(civs, sort, direction);
		ArrayList<Civilization.Name> sortedNames = new ArrayList<Civilization.Name>();
		for (Civilization civ : sortedCivs) {
			sortedNames.add(civ.getName());
		}
		return sortedNames;
	}

	public static ArrayList<Civilization> sortByAst(ArrayList<Civilization> civs, SortDirection direction) {
		Collections.sort(civs);
		if (direction == SortDirection.DESCENDING) {
			Collections.reverse(civs);
		}
		return civs;
	}

	public static ArrayList<Civilization> sortByCensus(ArrayList<Civilization> civs, SortDirection direction) {
		Collections.sort(civs, new CensusComparator());
		if (direction == SortDirection.DESCENDING) {
			Collections.reverse(civs);
		}
		return civs;
	}

	public static ArrayList<Civilization> sortByAstPosition(ArrayList<Civilization> civs, SortDirection direction) {
		Collections.sort(civs, new AstPositionComparator());
		if (direction == SortDirection.DESCENDING) {
			Collections.reverse(civs);
		}
		return civs;
	}

	public int getVPfromTech() {
		int vp = 0;
		for (Technology tech : this.techs) {
			vp = vp + tech.getVP();
		}
		return vp;
	}

	public int getVP() {
		return this.nCities + this.astPosition * 5 + getVPfromTech();
		// TODO Need to add adjustment for Late Iron Age only bonus
	}

	public String toFullString() {
		String s = this.toString() + " (" + this.player + ")\n";
		s = s + "Current Score: " + this.getVP() + "\n";
		s = s + "Current AST Step: " + this.astPosition + "(" + this.getCurrentAge() + ")\n";
		s = s + "Next Step Age: " + this.getNextStepAge() + "\n";
		s = s + "Cities: " + this.nCities + " Population: " + this.population + "\n";
		s = s + "Technologies:" + this.techs;
		return s;
	}

	private final static class CensusComparator implements Comparator<Civilization> {
		public int compare(Civilization civ1, Civilization civ2) {
			int result = -Integer.compare(civ1.getPopulation(), civ2.getPopulation());
			if (result == 0) {
				result = civ1.compareTo(civ2);
			}
			return result;
		}
	}

	private final static class VpComparator implements Comparator<Civilization> {
		public int compare(Civilization civ1, Civilization civ2) {
			int result = -Integer.compare(civ1.getVP(), civ2.getVP());
			if (result == 0) {
				result = civ1.compareTo(civ2);
			}
			return result;
		}
	}

	private final static class CityComparator implements Comparator<Civilization> {
		public int compare(Civilization civ1, Civilization civ2) {
			int result = -Integer.compare(civ1.getCityCount(), civ2.getCityCount());
			if (result == 0) {
				result = civ1.compareTo(civ2);
			}
			return result;
		}
	}

	private final static class AstPositionComparator implements Comparator<Civilization> {
		public int compare(Civilization civ1, Civilization civ2) {
			int result = -Integer.compare(civ1.astPosition, civ2.astPosition);
			if (result == 0) {
				result = civ1.compareTo(civ2);
			}
			return result;
		}
	}

	private final static class MovementComparator implements Comparator<Civilization> {
		public int compare(Civilization civ1, Civilization civ2) {
			boolean civ1Military = civ1.hasTech(Technology.MILITARY);
			boolean civ2Military = civ2.hasTech(Technology.MILITARY);

			if (civ1Military ^ civ2Military) {
				return civ1Military ? 1 : -1;
			} else {
				return new CensusComparator().compare(civ1, civ2);
			}
		}
	}

	@Override
	public int compareTo(Civilization otherCiv) {
		int result = this.name.compareTo(otherCiv.name);
		return result;
	}

	public static final class AstRequirements {

		@JsonProperty("minCities")
		private final int minCities;

		/**
		 * @return the minCities
		 */
		public int getMinCities() {
			return this.minCities;
		}

		/**
		 * @return the minAdvances
		 */
		public int getMinAdvances() {
			return this.minAdvances;
		}

		/**
		 * @return the minLevelOneTechs
		 */
		public int getMinLevelOneTechs() {
			return this.minLevelOneTechs;
		}

		/**
		 * @return the minLevelTwoPlusTechs
		 */
		public int getMinLevelTwoPlusTechs() {
			return this.minLevelTwoPlusTechs;
		}

		/**
		 * @return the minLevelThreeTechs
		 */
		public int getMinLevelThreeTechs() {
			return this.minLevelThreeTechs;
		}

		/**
		 * @return the minTechVP
		 */
		public int getMinTechVP() {
			return this.minTechVP;
		}

		public String getText() {
			return this.text;
		}

		@JsonProperty("minAdvances")
		private final int		minAdvances;
		@JsonProperty("minLevelOneTechs")
		private final int		minLevelOneTechs;
		@JsonProperty("minLevelTwoPlusTechs")
		private final int		minLevelTwoPlusTechs;
		@JsonProperty("minLevelThreeTechs")
		private final int		minLevelThreeTechs;
		@JsonProperty("minTechVP")
		private final int		minTechVP;
		@JsonProperty("text")
		private final String	text;

		@JsonCreator
		public AstRequirements(@JsonProperty("text") final String text, @JsonProperty("minCities") final int minCities,
				@JsonProperty("minAdvances") final int minAdvances,
				@JsonProperty("minLevelOneTechs") final int minLevelOneTechs,
				@JsonProperty("minLevelTwoPlusTechs") final int minLevelTwoPlusTechs,
				@JsonProperty("minLevelThreeTechs") final int minLevelThreeTechs,
				@JsonProperty("minTechVP") final int minTechVP) {
			this.text = text;
			this.minCities = minCities;
			this.minAdvances = minAdvances;
			this.minLevelOneTechs = minLevelOneTechs;
			this.minLevelTwoPlusTechs = minLevelTwoPlusTechs;
			this.minLevelThreeTechs = minLevelThreeTechs;
			this.minTechVP = minTechVP;
		}

	}

	static final class AstTableData {
		@JsonProperty("astRank")
		public final int												astRank;
		@JsonProperty("region")
		public final Region												region;
		@JsonProperty("subTable")
		public final HashMap<Game.Difficulty, HashMap<Age, Integer>>	subTable;

		@JsonCreator
		public AstTableData(@JsonProperty("astRank") final int astRank, @JsonProperty("region") final Region region,
				@JsonProperty("subTable") HashMap<Game.Difficulty, HashMap<Age, Integer>> subTable) {
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

		public int getAgeStart(Age age, Difficulty difficulty) {
			return this.subTable.get(difficulty).get(age).intValue();
		}
	}

	public boolean onlyLateIron(ArrayList<Civilization> civs) {
		if (this.getCurrentAge() != Age.LATE_IRON) {
			return false;
		}
		for (Civilization civ : civs) {
			if (!civ.equals(this) && civ.getCurrentAge() == Age.LATE_IRON) {
				return false;
			}
		}
		return true;
	}

	public void removeTech(Technology tech) {
		this.techs.remove(tech);
	}


}

