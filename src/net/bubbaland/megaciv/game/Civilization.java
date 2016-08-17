package net.bubbaland.megaciv.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang3.text.WordUtils;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Game.Difficulty;

public class Civilization implements Serializable, Comparable<Civilization> {

	private static final long serialVersionUID = -9210563148479097901L;

	public static enum Name {
		MINOA, SABA, ASSYRIA, MAURYA, CELT, BABYLON, CARTHAGE, DRAVIDIA, HATTI, KUSHAN, ROME, PERSIA, IBERIA, NUBIA,
		HELLAS, INDUS, EGYPT, PARTHIA;

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

	public static enum AstChange {
		REGRESS, NONE, ADVANCE
	}

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

		@Override
		public String toString() {
			return WordUtils.capitalizeFully(( this.name() + " Age" ).replace("_", " "));
		}

	}

	public static enum SortOption {
		AST, POPULATION, CITIES, AST_POSITION, VP, MOVEMENT;
	}

	public static enum SortDirection {
		ASCENDING, DESCENDING;
	}

	@JsonProperty("name")
	private Name													name;
	@JsonProperty("player")
	private String													player;
	@JsonProperty("population")
	private int														population;
	@JsonProperty("astPosition")
	private int														astPosition;
	@JsonProperty("nCities")
	private int														nCities;
	@JsonProperty("techs")
	private ArrayList<Technology>									techs;

	@JsonProperty("typeCredits")
	private final HashMap<Technology, ArrayList<Technology.Type>>	extraTypeCredits;								// Each
																													// listed
																													// type
																													// worth
																													// 5
																													// credits
																													// each
	@JsonProperty("difficulty")
	private Difficulty												difficulty;

	@JsonProperty("hasPurchased")
	private boolean													hasPurchased;

	public Civilization(Name name, Difficulty difficulty) {

		this(name, null, 1, 0, new ArrayList<Technology>(), new HashMap<Technology, ArrayList<Technology.Type>>() {
			private static final long serialVersionUID = 6611228131955386821L;

			{
				put(Technology.WRITTEN_RECORD, new ArrayList<Technology.Type>() {
					private static final long serialVersionUID = 3890391732659207236L;

					{
						add(Technology.Type.SCIENCE);
						add(Technology.Type.SCIENCE);
					}
				});
				put(Technology.MONUMENT, new ArrayList<Technology.Type>() {
					private static final long serialVersionUID = 3890391732659207236L;

					{
						add(Technology.Type.SCIENCE);
						add(Technology.Type.SCIENCE);
						add(Technology.Type.SCIENCE);
						add(Technology.Type.SCIENCE);
					}
				});
			}
		}, 0, difficulty, false);
	}

	@JsonCreator
	private Civilization(@JsonProperty("name") Name name, @JsonProperty("player") String player,
			@JsonProperty("population") int population, @JsonProperty("nCities") int nCities,
			@JsonProperty("techs") ArrayList<Technology> techs,
			@JsonProperty("typeCredits") HashMap<Technology, ArrayList<Technology.Type>> typeCredits,
			@JsonProperty("astPosition") int astPosition, @JsonProperty("difficulty") Difficulty difficulty,
			@JsonProperty("hasPurchased") boolean hasPurchased) {
		this.name = name;
		this.player = player;
		this.population = population;
		this.nCities = nCities;
		this.techs = techs;
		this.astPosition = astPosition;
		this.extraTypeCredits = typeCredits;
		this.hasPurchased = hasPurchased;
		this.difficulty = difficulty;
	}

	public Name getName() {
		return this.name;
	}

	public String toString() {
		return WordUtils.capitalizeFully(this.name.name());
	}

	// public void incrementAST() {
	// this.astPosition++;
	// }
	//
	// public void decrementAST() {
	// this.astPosition--;
	// }

	public void changeAst(AstChange change) {
		switch (change) {
			case REGRESS:
				this.astPosition--;
				break;
			case ADVANCE:
				this.astPosition++;
				break;
			case NONE:
				break;
		}
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

	public boolean passAstRequirements() {
		return this.passAstRequirements(this.getNextStepAge());
	}

	public boolean passAstRequirements(Age age) {
		AstRequirements reqs = Game.AGE_REQUIREMENTS.get(this.difficulty).get(age);
		return this.getCityCount() >= reqs.getMinCities() && this.techs.size() >= reqs.getMinAdvances()
				&& this.getTechCountByVP(1) >= reqs.getMinLevelOneTechs()
				&& this.getTechCountByVP(3) + this.getTechCountByVP(6) >= reqs.getMinLevelTwoPlusTechs()
				&& this.getTechCountByVP(6) >= reqs.getMinLevelThreeTechs()
				&& this.getVPfromTech() >= reqs.getMinTechVP();
	}

	public String astRequirementString(Age age) {
		AstRequirements reqs = Game.AGE_REQUIREMENTS.get(this.difficulty).get(age);
		String s = "<html>" + age.toString() + "";
		if (reqs.getMinCities() > 0) {
			String colorName = this.nCities >= reqs.getMinCities() ? "green" : "red";
			s = s + "<BR/><span color='" + colorName + "'>" + reqs.getMinCities() + " Cities</span>";
		}
		if (reqs.getMinAdvances() > 0) {
			String colorName = this.techs.size() >= reqs.getMinAdvances() ? "green" : "red";
			s = s + "<BR/><span color='" + colorName + "'>" + reqs.getMinAdvances() + " Advances</span>";
		}
		if (reqs.getMinTechVP() > 0) {
			String colorName = this.getVPfromTech() >= reqs.getMinTechVP() ? "green" : "red";
			s = s + "<BR/><span color='" + colorName + "'>" + reqs.getMinTechVP() + " VP from Advances</span>";
		}
		if (reqs.getMinLevelOneTechs() > 0) {
			String colorName = this.getTechCountByVP(1) >= reqs.getMinLevelOneTechs() ? "green" : "red";
			s = s + "<BR/><span color='" + colorName + "'>" + reqs.getMinLevelOneTechs() + " Advances < 100</span>";
		}
		if (reqs.getMinLevelTwoPlusTechs() > 0) {
			String colorName = this.getTechCountByVP(3) + this.getTechCountByVP(6) >= reqs
					.getMinLevelTwoPlusTechs() ? "green" : "red";
			s = s + "<BR/><span color='" + colorName + "'>" + reqs.getMinLevelTwoPlusTechs() + " Advances > 100</span>";
		}
		if (reqs.getMinLevelThreeTechs() > 0) {
			String colorName = this.getTechCountByVP(6) >= reqs.getMinLevelThreeTechs() ? "green" : "red";
			s = s + "<BR/><span color='" + colorName + "'>" + reqs.getMinLevelThreeTechs() + " Advances > 200</span>";
		}
		s = s + "</html>";
		return s;
	}

	public int getAgeStart(Civilization.Age age) {
		return Game.AST_TABLE.get(this.getName()).getAgeStart(age, this.difficulty);
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

	public void setPurchased(boolean hasPurchased) {
		this.hasPurchased = hasPurchased;
	}

	public boolean hasPurchased() {
		return this.hasPurchased;
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
		HashMap<Technology, ArrayList<Technology.Type>> extraTypeCredits =
				new HashMap<Technology, ArrayList<Technology.Type>>() {
					private static final long serialVersionUID = 1L;
					{
						for (Technology tech : Civilization.this.extraTypeCredits.keySet()) {
							put(tech, Civilization.this.extraTypeCredits.get(tech));
						}
					}
				};
		return new Civilization(this.name, this.player, this.population, this.nCities, techs, extraTypeCredits,
				this.astPosition, this.difficulty, this.hasPurchased);
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

	public int getCost(Technology tech) {
		int cost = tech.getBaseCost();

		int maxTypeDiscount = 0;

		for (Technology.Type type : tech.getTypes()) {
			maxTypeDiscount = Math.max(this.getTypeCredit(type), maxTypeDiscount);
		}

		cost = cost - maxTypeDiscount;

		for (Technology tech2 : this.getTechs()) {
			cost = cost - tech2.getTechCredit(tech);
		}

		return Math.max(cost, 0);
	}

	public int getTypeCredit(Technology.Type type) {
		int credit = 0;
		for (Technology tech : this.extraTypeCredits.keySet()) {
			if (this.hasTech(tech)) {
				credit = credit + Collections.frequency(this.extraTypeCredits.get(tech), type) * Game.VP_PER_AST_STEP;
			}
		}
		for (Technology tech : this.techs) {
			credit = credit + tech.getTypeCredit(type);
		}
		return credit;
	}

	public ArrayList<Technology.Type> getExtraTypeCredits(Technology tech) {
		return this.extraTypeCredits.get(tech);
	}

	public void addTypeCredits(Technology tech, ArrayList<Technology.Type> newCredits) {
		this.extraTypeCredits.put(tech, newCredits);
	}

	public int getAstPosition() {
		return this.astPosition;
	}

	public void setAstPosition(int newAstPosition) {
		this.astPosition = newAstPosition;
	}

	public int getAst() {
		return Game.AST_TABLE.get(this.name).astRank;
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
		return this.nCities + this.astPosition * Game.VP_PER_AST_STEP + getVPfromTech();
		// TODO Need to add adjustment for Late Iron Age only bonus
	}

	public String getTechBreakdownString() {
		String s = "<html>";
		s = s + "<table cellpadding='1' cellspacing='1'><tr><td align='right'>1 VP (&lt;100)</td><td>"
				+ this.getTechCountByVP(1) + "</td></tr>";
		s = s + "<tr><td>3 VP (&gt;100)</td><td align='right'>" + this.getTechCountByVP(3) + "</td></tr>";
		s = s + "<tr><td>6 VP (&gt;200)</td><td align='right'>" + this.getTechCountByVP(6)
				+ "</td></tr></table></html>";
		return s;
	}

	public String getVpBreakdownString() {
		String s = "<html>";
		s = s + "<table cellpadding='1' cellspacing='1'><tr><td>AST</td><td align='right'>"
				+ this.astPosition * Game.VP_PER_AST_STEP + "</td></tr>";
		s = s + "<tr><td>Tech</td><td align='right'>" + this.getVPfromTech() + "</td></tr>";
		s = s + "<tr><td>Cities</td><td align='right'>" + this.getCityCount() + "</td></tr></table></html>";
		return s;
	}

	public String toFullString() {
		String s = this.toString() + " (" + this.player + ")\n";
		s = s + "Current Score: " + this.getVP() + "\n";
		s = s + "Current AST Step: " + this.astPosition + "(" + this.getCurrentAge() + ")\n";
		s = s + "Next Step Age: " + this.getNextStepAge() + "\n";
		s = s + "Cities: " + this.nCities + " Population: " + this.population + "\n";
		s = s + "Technologies:" + this.techs + "</html>";
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
				result = -civ1.compareTo(civ2);
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

		public String toString() {
			return "Text: " + this.text + "\n  Min Cities: " + this.minCities + "\n  Min Advances: " + this.minAdvances
					+ "\n  Min L1 Techs: " + this.minLevelOneTechs + "\n  Min L2+ Techs: " + this.minLevelTwoPlusTechs
					+ "\n  Min L3 Techs: " + this.minLevelThreeTechs + "\n  Min Tech VP: " + this.minTechVP;
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

