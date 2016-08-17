package net.bubbaland.megaciv.game;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.text.WordUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Civilization.Age;
import net.bubbaland.megaciv.game.Civilization.AstRequirements;
import net.bubbaland.megaciv.game.Civilization.AstTableData;
import net.bubbaland.megaciv.game.Civilization.Name;
import net.bubbaland.megaciv.game.Civilization.Region;
import net.bubbaland.megaciv.game.Civilization.SortDirection;

public class Game implements Serializable {

	private static final long	serialVersionUID	= 3617165171580835437L;

	public static final int		VP_PER_AST_STEP		= 5;
	public static final int		MAX_CITIES			= 9;
	public static final int		MAX_POPULATION		= 55;

	public static enum Difficulty {
		BASIC, EXPERT
	}

	private final static String																			CIV_CONSTANTS_FILENAME	=
			"Civ_Constants.xml";

	public final static HashMap<Civilization.Name, Color>												BACKGROUND_COLORS;
	public final static HashMap<Civilization.Name, Color>												FOREGROUND_COLORS;
	public final static HashMap<Civilization.Name, Civilization.AstTableData>							AST_TABLE;
	public final static HashMap<Integer, HashMap<Civilization.Region, ArrayList<Civilization.Name>>>	DEFAULT_STARTING_CIVS;
	public final static HashMap<Difficulty, HashMap<Civilization.Age, Civilization.AstRequirements>>	AGE_REQUIREMENTS;

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
				final int astRank =
						Integer.parseInt(civElement.getElementsByTagName("AstRank").item(0).getTextContent());


				HashMap<Difficulty, HashMap<Age, Integer>> hash = new HashMap<Difficulty, HashMap<Age, Integer>>();
				for (Difficulty difficulty : EnumSet.allOf(Game.Difficulty.class)) {
					final Element civAstElement = (Element) civElement
							.getElementsByTagName(WordUtils.capitalizeFully(difficulty.name())).item(0);
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

			final Element startingCivElement =
					(Element) doc.getDocumentElement().getElementsByTagName("StartingCivs").item(0);
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
						final Civilization.Name name =
								Civilization.Name.valueOf(civElement.getAttribute("name").toUpperCase());
						civs.add(name);
					}

					regionHash.put(region, civs);
				}

				DEFAULT_STARTING_CIVS.put(nCivs, regionHash);
			}

			final Element requirementElement =
					(Element) doc.getDocumentElement().getElementsByTagName("AstRequirements").item(0);
			NodeList difficultyNodes = requirementElement.getElementsByTagName("Difficulty");
			for (int d = 0; d < difficultyNodes.getLength(); d++) {
				Difficulty difficulty = Difficulty.valueOf(( (Element) difficultyNodes.item(d) ).getAttribute("level"));
				NodeList ageNodes = ( (Element) difficultyNodes.item(d) ).getElementsByTagName("Age");

				HashMap<Age, AstRequirements> astReqs = new HashMap<Age, AstRequirements>();
				for (int a = 0; a < ageNodes.getLength(); a++) {
					Element ageElement = ( (Element) ageNodes.item(a) );
					Age age = Age.valueOf(ageElement.getAttribute("name"));
					String reqText = ageElement.getElementsByTagName("Text").item(0).getTextContent();
					int minCities =
							Integer.parseInt(ageElement.getElementsByTagName("MinCities").item(0).getTextContent());
					int minAdvances =
							Integer.parseInt(ageElement.getElementsByTagName("MinAdvances").item(0).getTextContent());
					int minLevelOneTechs =
							Integer.parseInt(ageElement.getElementsByTagName("MinL1Techs").item(0).getTextContent());
					int minLevelTwoPlusTechs = Integer
							.parseInt(ageElement.getElementsByTagName("MinL2PlusTechs").item(0).getTextContent());
					int minLevelThreeTechs =
							Integer.parseInt(ageElement.getElementsByTagName("MinL3Techs").item(0).getTextContent());
					int minTechVP =
							Integer.parseInt(ageElement.getElementsByTagName("MinTechVP").item(0).getTextContent());
					astReqs.put(age, new AstRequirements(reqText, minCities, minAdvances, minLevelOneTechs,
							minLevelTwoPlusTechs, minLevelThreeTechs, minTechVP));
				}
				AGE_REQUIREMENTS.put(difficulty, astReqs);
			}


		} catch (final ParserConfigurationException | SAXException | IOException e) {

		}
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

