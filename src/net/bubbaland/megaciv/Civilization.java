package net.bubbaland.megaciv;

import java.io.IOException;
import java.io.InputStream;
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

import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.Technology.Type;

public class Civilization implements Comparable<Civilization> {

	public final static String CIV_CONSTANTS_FILENAME = "Civ_Constants.xml";

	public static enum Region {
		EAST, WEST
	};

	private static HashMap<String, AstTableData> AST_TABLE;

	public static void initializeAst(boolean useExpert) {
		AST_TABLE = new HashMap<String, AstTableData>();
		final String difficulty = useExpert ? "Expert" : "Basic";

		try {
			final InputStream fileStream = Civilization.class.getResourceAsStream(CIV_CONSTANTS_FILENAME);

			final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			final Document doc = dBuilder.parse(fileStream);
			doc.getDocumentElement().normalize();
			NodeList civNodes = doc.getElementsByTagName("Civilization");

			for (int c = 0; c < civNodes.getLength(); c++) {
				final Element civElement = (Element) civNodes.item(c);
				final String name = civElement.getAttribute("name");
				Region region = null;
				switch (civElement.getElementsByTagName("Region").item(0).getTextContent()) {
					case "East":
						region = Region.EAST;
					case "West":
						region = Region.WEST;
				}
				final int astPosition = Integer
						.parseInt(civElement.getElementsByTagName("AstPosition").item(0).getTextContent());
				final Element civAstElement = (Element) civElement.getElementsByTagName(difficulty);
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
				AST_TABLE.put(name, new AstTableData(astPosition, region, earlyBronzeStart, middleBronzeStart,
						lateBronzeStart, earlyIronStart, lateIronStart));
			}

		} catch (final ParserConfigurationException | SAXException | IOException e) {

		}
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
		return AST_TABLE.get(this.name).astRank;
	}

	public int getEarlyBronzeStart() {
		return AST_TABLE.get(this.name).earlyBronzeStart;
	}

	public int getMiddleBronzeStart() {
		return AST_TABLE.get(this.name).middleBronzeStart;
	}

	public int lateEarlyBronzeStart() {
		return AST_TABLE.get(this.name).lateBronzeStart;
	}

	public int getEarlyIronStart() {
		return AST_TABLE.get(this.name).earlyIronStart;
	}

	public int getLateIronStart() {
		return AST_TABLE.get(this.name).lateIronStart;
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

	@Override
	/* Compare by AST Rank (default) */
	public int compareTo(Civilization otherCiv) {
		return Integer.compare(this.getAST(), otherCiv.getAST());
	}

	private final static class CensusComparator implements Comparator<Civilization> {
		public int compare(Civilization civ1, Civilization civ2) {
			int result = Integer.compare(civ1.getCensus(), civ2.getCensus());
			if (result == 0) {
				result = civ1.compareTo(civ2);
			}
			return result;
		}
	}

	private final static class AstPositionComparator implements Comparator<Civilization> {
		public int compare(Civilization civ1, Civilization civ2) {
			return Integer.compare(civ1.getAST(), civ2.getAST());
		}
	}

	private final static class AstTableData {
		@JsonProperty("astRank")
		final private int		astRank;
		@JsonProperty("earlyBronzeStart")
		final private int		earlyBronzeStart;
		@JsonProperty("middleBronzeStart")
		final private int		middleBronzeStart;
		@JsonProperty("lateBronzeStart")
		final private int		lateBronzeStart;
		@JsonProperty("earlyIronStart")
		final private int		earlyIronStart;
		@JsonProperty("lateIronStart")
		final private int		lateIronStart;
		@JsonProperty("region")
		final private Region	region;

		public AstTableData(@JsonProperty("astRank") final int astRank, @JsonProperty("region") final Region region,
				@JsonProperty("earlyBronzeStart") final int earlyBronzeStart,
				@JsonProperty("middleBronzeStart") final int middleBronzeStart,
				@JsonProperty("lateBronzeStart") final int lateBronzeStart,
				@JsonProperty("earlyIronStart") final int earlyIronStart,
				@JsonProperty("lateIronStart") final int lateIronStart) {
			this.astRank = astRank;
			this.earlyBronzeStart = earlyBronzeStart;
			this.middleBronzeStart = middleBronzeStart;
			this.lateBronzeStart = lateBronzeStart;
			this.earlyIronStart = earlyIronStart;
			this.lateIronStart = lateIronStart;
			this.region = region;
		}
	}
}

