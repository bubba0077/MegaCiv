package net.bubbaland.megaciv.game;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.text.WordUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Civilization.Age;
import net.bubbaland.megaciv.game.Civilization.AstRequirements;
import net.bubbaland.megaciv.game.Civilization.AstTableData;
import net.bubbaland.megaciv.game.Civilization.Name;
import net.bubbaland.megaciv.game.Civilization.Region;
import net.bubbaland.megaciv.game.Civilization.SortDirection;

/**
 * Primary class for game data. All data necessary to represent the game state is held within an instance of this class.
 * This class also loads static data from the Civ_Constants.xml file, which contains constants such as the default
 * starting civilizations, AST requirements, trade cards, etc.
 * 
 * @author Walter Kolczynski
 *
 */
public class Game implements Serializable {

	private static final long	serialVersionUID		= 3617165171580835437L;

	public static final int		VP_PER_AST_STEP			= 5;
	public static final int		VP_FROM_ONLY_LATEIRON	= 5;
	public static final int		MAX_CITIES				= 9;
	public static final int		MAX_POPULATION			= 55;

	public static enum Difficulty {
		BASIC, EXPERT
	}


	@JsonIgnore
	private final static String																			CIV_CONSTANTS_FILENAME	=
			"Civ_Constants.xml";

	@JsonIgnore
	public final static HashMap<Integer, Integer>														SMALL_GAME_CREDITS		=
			new HashMap<Integer, Integer>();
	@JsonIgnore
	public final static HashMap<Civilization.Name, Color>												BACKGROUND_COLORS		=
			new HashMap<Civilization.Name, Color>();
	@JsonIgnore
	public final static HashMap<Civilization.Name, Color>												FOREGROUND_COLORS		=
			new HashMap<Civilization.Name, Color>();
	@JsonIgnore
	public final static HashMap<Civilization.Name, Civilization.AstTableData>							AST_TABLE				=
			new HashMap<Civilization.Name, AstTableData>();
	@JsonIgnore
	public final static HashMap<Integer, HashMap<Civilization.Region, ArrayList<Civilization.Name>>>	DEFAULT_STARTING_CIVS	=
			new HashMap<Integer, HashMap<Civilization.Region, ArrayList<Civilization.Name>>>();
	@JsonIgnore
	public final static HashMap<Difficulty, HashMap<Civilization.Age, Civilization.AstRequirements>>	AGE_REQUIREMENTS		=
			new HashMap<Difficulty, HashMap<Civilization.Age, AstRequirements>>();;
	@JsonIgnore
	private final static HashMap<Integer, HashMap<Civilization.Region, TradeCardSet>>					TRADE_GOODS				=
			new HashMap<Integer, HashMap<Civilization.Region, TradeCardSet>>();

	static {
		parseGameConstants();
	}


	@JsonProperty("difficulty")
	private Difficulty difficulty;

	public void setDifficulty(Difficulty difficulty) {
		this.difficulty = difficulty;
		this.civs.forEach(civ -> civ.setDifficulty(difficulty));
	}

	public Difficulty getDifficulty() {
		return this.difficulty;
	}

	@JsonProperty("civs")
	private ArrayList<Civilization>	civs;

	@JsonProperty("currentRound")
	private int						currentRound;

	@JsonProperty("lastRound")
	private int						lastRound;

	@JsonProperty("region")
	private Region					region;

	@JsonProperty("gameLog")
	private ArrayList<GameEvent>	gameLog;

	public Game() {
		this(null, new ArrayList<Civilization>(), null, 1, Integer.MAX_VALUE, new ArrayList<GameEvent>());
	}

	@JsonCreator
	public Game(@JsonProperty("region") Region region, @JsonProperty("civs") ArrayList<Civilization> civs,
			@JsonProperty("difficulty") Difficulty difficulty, @JsonProperty("currentRound") int currentRound,
			@JsonProperty("lastRound") int lastRound, @JsonProperty("gameLog") ArrayList<GameEvent> gameLog) {
		this.region = region;
		this.civs = civs;
		this.currentRound = currentRound;
		this.lastRound = lastRound;
		this.difficulty = difficulty;
		this.gameLog = gameLog;
	}

	public ArrayList<GameEvent> getLog() {
		return this.gameLog;
	}

	public void logEvent(GameEvent event) {
		this.gameLog.add(event);
	}

	public void setRegion(Region region) {
		this.region = region;
	}

	public Region getRegion() {
		return this.region;
	}

	public TradeCardSet getTradeCards() {
		return TRADE_GOODS.get(this.civs.size()).get(this.region);
	}

	public boolean isGameOver() {
		return this.currentRound > this.lastRound;
	}

	public boolean isLastTurn() {
		return this.currentRound == this.lastRound;
	}

	public void nextRound() {
		if (this.civs.stream().anyMatch(civ -> civ.getCurrentAge() == Age.LATE_IRON)
				&& this.lastRound > this.currentRound) {
			this.lastRound = this.difficulty == Difficulty.EXPERT ? this.currentRound + 1 : this.currentRound;
		}
		this.currentRound++;
		this.civs.forEach(civ -> civ.setPurchased(false));
	}

	public int getCurrentRound() {
		return Math.min(this.currentRound, this.lastRound);
	}

	public void addCivilization(Civilization.Name name) {
		Civilization civ = new Civilization(name, difficulty);
		this.civs.add(civ);
	}

	public void assignStartCredits() {
		int credit = SMALL_GAME_CREDITS.get(this.civs.size());
		this.civs.forEach(civ -> civ.setSmallGameCredits(credit));
	}

	public void retireCivilization(Civilization.Name name) {
		this.civs.removeIf(civ -> civ.getName().equals(name));
	}

	public synchronized void addCivilization(ArrayList<Civilization.Name> names) {
		names.forEach(name -> this.addCivilization(name));
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
		return new ArrayList<Civilization>(this.civs);
	}

	public int getNCivilizations() {
		return this.civs.size();
	}

	public ArrayList<Civilization.Name> getCivilizationNames() {
		return new ArrayList<Civilization.Name>(
				this.civs.stream().map(civ -> civ.getName()).collect(Collectors.toList()));
	}

	public Civilization getCivilization(Civilization.Name name) {
		return this.civs.stream().filter(civ -> civ.getName().equals(name)).findAny().orElse(null);
	}

	public synchronized void setCivilization(Civilization newCiv) {
		this.civs.removeIf(civ -> civ.getName().equals(newCiv.getName()));
		this.civs.add(newCiv);
	}

	public String toString() {
		String s = "Game Data:\n";
		s = s + "AST Difficulty: " + this.difficulty + "\n";
		for (Civilization civ : Civilization.sortByAst(this.getCivilizations(), SortDirection.DESCENDING)) {
			s = s + civ.toFullString() + "\n\n";
		}
		return s;
	}

	private static void parseGameConstants() {
		final InputStream fileStream = Civilization.class.getResourceAsStream(CIV_CONSTANTS_FILENAME);
		final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		Document doc = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(fileStream);
		} catch (SAXException | IOException | ParserConfigurationException exception) {
			exception.printStackTrace();
		}
		doc.getDocumentElement().normalize();

		/*
		 * Ast Table
		 */
		final Element astTableElement = (Element) doc.getDocumentElement().getElementsByTagName("AstTable").item(0);
		NodeList civNodes = astTableElement.getElementsByTagName("Civilization");

		for (int c = 0; c < civNodes.getLength(); c++) {
			final Element civElement = (Element) civNodes.item(c);
			final Civilization.Name name = Civilization.Name.valueOf(civElement.getAttribute("name").toUpperCase());
			Region region =
					Region.valueOf(civElement.getElementsByTagName("Region").item(0).getTextContent().toUpperCase());
			final int astRank = Integer.parseInt(civElement.getElementsByTagName("AstRank").item(0).getTextContent());


			HashMap<Difficulty, HashMap<Age, Integer>> hash = new HashMap<Difficulty, HashMap<Age, Integer>>();
			for (Difficulty difficulty : EnumSet.allOf(Difficulty.class)) {
				final Element civAstElement =
						(Element) civElement.getElementsByTagName(WordUtils.capitalizeFully(difficulty.name())).item(0);
				final int earlyBronzeStart = Integer
						.parseInt(civAstElement.getElementsByTagName("EarlyBronzeStart").item(0).getTextContent());
				final int middleBronzeStart = Integer
						.parseInt(civAstElement.getElementsByTagName("MiddleBronzeStart").item(0).getTextContent());
				final int lateBronzeStart = Integer
						.parseInt(civAstElement.getElementsByTagName("LateBronzeStart").item(0).getTextContent());
				final int earlyIronStart =
						Integer.parseInt(civAstElement.getElementsByTagName("EarlyIronStart").item(0).getTextContent());
				final int lateIronStart =
						Integer.parseInt(civAstElement.getElementsByTagName("LateIronStart").item(0).getTextContent());

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

			FOREGROUND_COLORS.put(name,
					new Color(new BigInteger(civElement.getElementsByTagName("Foreground").item(0).getTextContent(), 16)
							.intValue()));
			BACKGROUND_COLORS.put(name,
					new Color(new BigInteger(civElement.getElementsByTagName("Background").item(0).getTextContent(), 16)
							.intValue()));
		}


		/*
		 * Default Starting Civs
		 */
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

		/*
		 * Small game credits
		 */
		final Element creditTopElement =
				(Element) doc.getDocumentElement().getElementsByTagName("SmallGameCredits").item(0);
		NodeList creditNodes = creditTopElement.getElementsByTagName("PlayerCount");

		for (int i = 0; i < creditNodes.getLength(); i++) {
			final Element creditElement = (Element) creditNodes.item(i);
			final int nCivs = Integer.parseInt(creditElement.getAttribute("count"));
			final int credit = Integer.parseInt(creditElement.getTextContent());

			SMALL_GAME_CREDITS.put(nCivs, credit);
		}


		/*
		 * AST Requirements
		 */
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
				int minCities = Integer.parseInt(ageElement.getElementsByTagName("MinCities").item(0).getTextContent());
				int minAdvances =
						Integer.parseInt(ageElement.getElementsByTagName("MinAdvances").item(0).getTextContent());
				int minLevelOneTechs =
						Integer.parseInt(ageElement.getElementsByTagName("MinL1Techs").item(0).getTextContent());
				int minLevelTwoPlusTechs =
						Integer.parseInt(ageElement.getElementsByTagName("MinL2PlusTechs").item(0).getTextContent());
				int minLevelThreeTechs =
						Integer.parseInt(ageElement.getElementsByTagName("MinL3Techs").item(0).getTextContent());
				int minTechVP = Integer.parseInt(ageElement.getElementsByTagName("MinTechVP").item(0).getTextContent());
				astReqs.put(age, new AstRequirements(reqText, minCities, minAdvances, minLevelOneTechs,
						minLevelTwoPlusTechs, minLevelThreeTechs, minTechVP));
			}
			AGE_REQUIREMENTS.put(difficulty, astReqs);
		}

		/*
		 * Trade Stacks
		 */
		final Element tradeElement = (Element) doc.getDocumentElement().getElementsByTagName("TradeCards").item(0);
		NodeList playerCountNodes = tradeElement.getElementsByTagName("PlayerCount");
		for (int c = 0; c < playerCountNodes.getLength(); c++) {
			Element countElement = (Element) playerCountNodes.item(c);
			int count = Integer.parseInt(countElement.getAttribute("count"));

			TRADE_GOODS.put(count, new HashMap<Region, TradeCardSet>());

			NodeList regionNodes = countElement.getElementsByTagName("GameRegion");
			for (int r = 0; r < regionNodes.getLength(); r++) {
				Element regionElement = (Element) regionNodes.item(r);
				Region region = Civilization.Region.valueOf(regionElement.getAttribute("name").toUpperCase());

				TradeCardSet tradeGoods = new TradeCardSet();

				NodeList stackNodes = regionElement.getElementsByTagName("Stack");
				for (int s = 0; s < stackNodes.getLength(); s++) {
					Element stackElement = (Element) stackNodes.item(s);
					int stackNumber = Integer.parseInt(stackElement.getAttribute("number"));
					TradeStack stack = new TradeStack(stackNumber);

					NodeList goodNodes = stackElement.getElementsByTagName("Good");
					for (int g = 0; g < goodNodes.getLength(); g++) {
						Element goodElement = (Element) goodNodes.item(g);
						TradeStack.TradeGood good =
								TradeStack.TradeGood.valueOf(goodElement.getAttribute("name").toUpperCase());
						NodeList innerRegionNodes = goodElement.getElementsByTagName("Region");
						for (int ir = 0; ir < innerRegionNodes.getLength(); ir++) {
							Element innerRegionElement = (Element) innerRegionNodes.item(ir);
							Region innerRegion =
									Civilization.Region.valueOf(innerRegionElement.getAttribute("name").toUpperCase());
							int quantity = Integer.parseInt(innerRegionElement.getTextContent());

							stack.addCard(good, innerRegion, quantity);
						}
					}

					NodeList calamityNodes = stackElement.getElementsByTagName("Calamity");
					for (int x = 0; x < calamityNodes.getLength(); x++) {
						Element calamityElement = (Element) calamityNodes.item(x);
						String calamityType = calamityElement.getAttribute("type");
						String calamityName = calamityElement.getTextContent().toUpperCase();
						TradeStack.Calamity calamity = null;
						switch (calamityType) {
							case "minor":
								calamity = TradeStack.MinorCalamity.valueOf(calamityName);
								break;
							case "major-tradable":
								calamity = TradeStack.MajorTradableCalamity.valueOf(calamityName);
								break;
							case "major-nontradable":
								calamity = TradeStack.MajorNontradableCalamity.valueOf(calamityName);
								break;
							default:
								try {
									throw ( new IOException("Error parsing " + CIV_CONSTANTS_FILENAME
											+ " for trade cards: Invalid calamity type specified for player count "
											+ count + ", stack #" + stackNumber + ": " + calamityType) );
								} catch (IOException exception) {
									// TODO Auto-generated catch block
									exception.printStackTrace();
								}
						}

						if (calamity != null) {
							stack.addCalamity(calamity);
						}
					}

					tradeGoods.setStack(stackNumber, stack);
				}

				TRADE_GOODS.get(count).put(region, tradeGoods);
			}
		}
	}
}