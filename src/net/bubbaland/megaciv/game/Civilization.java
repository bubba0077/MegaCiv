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


/**
 * Class for specifying the data object for a single civilization.
 * 
 * @author Walter Kolczynski
 *
 */
public class Civilization implements Serializable, Comparable<Civilization> {

	private static final long serialVersionUID = -9210563148479097901L;

	/**
	 * The names of the 18 allowed civilizations.
	 */
	public static enum Name {
		MINOA, SABA, ASSYRIA, MAURYA, CELT, BABYLON, CARTHAGE, DRAVIDIA, HATTI, KUSHAN, ROME, PERSIA, IBERIA, NUBIA,
		HELLAS, INDUS, EGYPT, PARTHIA;

		/*
		 * A static HashSet<String> that contains all of the civilization names
		 */
		public final static HashSet<String> strings = new HashSet<String>() {
			private static final long serialVersionUID = -4960803070653152009L;

			{
				for (Civilization.Name name : EnumSet.allOf(Name.class)) {
					add(name.toString());
				}
			}
		};

		/**
		 * Determine whether a civilization name is associated with a given string.
		 * 
		 * @param s
		 *            The possible civilization name.
		 * @return Whether s is a civilization name.
		 */
		public static boolean contains(String s) {
			for (String s1 : strings) {
				if (s1.equalsIgnoreCase(s)) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Enumeration of possible regions. BOTH is used for games containing both EAST and WEST nations.
	 */
	public static enum Region {
		EAST, WEST, BOTH
	};

	/**
	 * Enumeration of possible results for an AST change.
	 */
	public static enum AstChange {
		REGRESS, NONE, ADVANCE
	}

	/**
	 * Enumeration of ages along the AST track.
	 */
	public static enum Age {
		STONE, EARLY_BRONZE, MIDDLE_BRONZE, LATE_BRONZE, EARLY_IRON, LATE_IRON {

			@Override
			// LATE_IRON is the final age, so there is no next age.
			public Age nextAge() {
				return null;
			}
		};

		/**
		 * Method providing the next age in the sequence.
		 * 
		 * @return The next age in sequence.
		 */
		public Age nextAge() {
			return values()[this.ordinal() + 1];
		}

		/**
		 * Create a prettier string representation of the age.
		 */
		@Override
		public String toString() {
			return WordUtils.capitalizeFully(( this.name() + " Age" ).replace("_", " "));
		}

	}

	/**
	 * Enumeration of allowed ways to sort a list of civilizations.
	 */
	public static enum SortOption {
		AST, POPULATION, CITIES, AST_POSITION, VP, MOVEMENT;
	}

	/**
	 * Enumeration of directions for sorting a list of civilizations.
	 */
	public static enum SortDirection {
		ASCENDING, DESCENDING;
	}

	/**
	 * The civilization name
	 */
	@JsonProperty("name")
	private Name													name;
	/**
	 * The name of the player controlling this civilization.
	 */
	@JsonProperty("player")
	private String													player;
	/**
	 * The AST Position of the civilization (number of spaces advanced).
	 */
	@JsonProperty("astPosition")
	private int														astPosition;
	/**
	 * The population of this civilization.
	 */
	@JsonProperty("population")
	private int														population;
	/**
	 * The number of cities this civilization controls.
	 */
	@JsonProperty("nCities")
	private int														nCities;
	/**
	 * A list of the advances the civilization has purchased.
	 */
	@JsonProperty("techs")
	private HashMap<Technology, Integer>							techs;

	/**
	 * Some advances ({@link Technology#MONUMENT Monument} and {@link Technology#WRITTEN_RECORD Written Record}) provide
	 * additional credits that are chosen by the player. This HashMap tracks the additional credits chosen. In order to
	 * allow correction of incorrect choices, the source advance is tracked as the key in the HashMap, with the list of
	 * credits the associated value.
	 */
	@JsonProperty("typeCredits")
	private final HashMap<Technology, ArrayList<Technology.Type>>	typeCredits;		// Each
	// listed
	// type
	// worth
	// 5
	// credits
	// each

	@JsonProperty("smallGameCredits")
	private HashMap<Technology.Type, Integer>						smallGameCredits;

	/**
	 * The current difficulty of the game. This property really belongs to the Game, but Civilization needs it to
	 * properly calculate AST requirements. Whenever the difficulty is changed in Game, Game updates the difficulty for
	 * each Civilization.
	 */
	@JsonProperty("difficulty")
	private Difficulty												difficulty;

	/**
	 * A boolean for tracking whether this civilization has purchased advances yet this turn.
	 */
	@JsonProperty("hasPurchased")
	private boolean													hasPurchased;

	/**
	 * The primary constructor for creating a civilization. Values are initialized to their start-of-game values.
	 * 
	 * @param name
	 *            The civilization name.
	 * @param difficulty
	 *            The current difficulty of the game.
	 */
	public Civilization(Name name, Difficulty difficulty) {

		this(name, null, 1, 0, new HashMap<Technology, Integer>(), new HashMap<Technology.Type, Integer>() {
			private static final long serialVersionUID = -8494150522727469271L;
			{
				for (Technology.Type type : Technology.Type.values()) {
					put(type, 0);
				}
			}
		}, new HashMap<Technology, ArrayList<Technology.Type>>() {
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

	/**
	 * An internal constructor that specifies all fields. This constructor is used to recreate the object from the JSON,
	 * as well as in the {@link #clone() clone} method.
	 * 
	 * @param name
	 *            The civilization name.
	 * @param player
	 *            The player controlling the civilization.
	 * @param population
	 *            The current population.
	 * @param nCities
	 *            The current number of cities.
	 * @param techs
	 *            A list of advances discovered by the civilization.
	 * @param typeCredits
	 *            A list of additional credits.
	 * @param astPosition
	 *            Current AST position of this civilization.
	 * @param difficulty
	 *            Current game difficulty.
	 * @param hasPurchased
	 *            Has the civilization purchased advances in the current round?
	 */
	@JsonCreator
	private Civilization(@JsonProperty("name") Name name, @JsonProperty("player") String player,
			@JsonProperty("population") int population, @JsonProperty("nCities") int nCities,
			@JsonProperty("techs") HashMap<Technology, Integer> techs,
			@JsonProperty("smallGameCredits") HashMap<Technology.Type, Integer> scenarioCredits,
			@JsonProperty("typeCredits") HashMap<Technology, ArrayList<Technology.Type>> typeCredits,
			@JsonProperty("astPosition") int astPosition, @JsonProperty("difficulty") Difficulty difficulty,
			@JsonProperty("hasPurchased") boolean hasPurchased) {
		this.name = name;
		this.player = player;
		this.population = population;
		this.nCities = nCities;
		this.techs = techs;
		this.astPosition = astPosition;
		this.typeCredits = typeCredits;
		this.smallGameCredits = scenarioCredits;
		this.hasPurchased = hasPurchased;
		this.difficulty = difficulty;
	}

	/**
	 * Get the civilization name.
	 * 
	 * @return The civilization name.
	 */
	public Name getName() {
		return this.name;
	}

	/**
	 * Override the default {@link Object#toString() toString()} method to return a string representation of the
	 * civilization name.
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return WordUtils.capitalizeFully(this.name.name());
	}

	/**
	 * Modify the current AST position based on the specified change.
	 * 
	 * @param change
	 *            The type of AST change to make.
	 */
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

	/**
	 * Get the Age of the civilization at a specified AST position.
	 * 
	 * @param astStep
	 *            The AST position
	 * @return The Age of this civilization at specified position.
	 */
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

	/**
	 * Get the current Age of the civilization.
	 * 
	 * @return The current Age.
	 */
	public Age getCurrentAge() {
		return this.getAge(this.astPosition);
	}

	/**
	 * Get the Age of the next AST step. This is used to determine whether a civilization advances.
	 * 
	 * @return
	 */
	public Age getNextStepAge() {
		return this.getAge(this.astPosition + 1);
	}

	/**
	 * Get the number of advances the civilization owns worth the specified victory point value.
	 * 
	 * @param vp
	 *            The victory point amount.
	 * @return The number of advances the civilization owns worth the specified VP.
	 */
	public int getTechCountByVP(int vp) {
		int n = 0;
		for (Technology tech : this.techs.keySet()) {
			if (tech.getVP() == vp) {
				n++;
			}
		}
		return n;
	}

	/**
	 * Determine whether this civilization satisfies the requirements for advancing to the next AST step.
	 * 
	 * @return boolean indicating whether this civilization satisfies the requirements for advancing to the next AST
	 *         step.
	 */
	public boolean passAstRequirements() {
		return this.passAstRequirements(this.getNextStepAge());
	}

	/**
	 * Determine whether this civilization satisfied the requirements for advancing through the given Age.
	 * 
	 * @param age
	 *            The specified Age.
	 * @return boolean indicating whether this civilization satisfied the requirements for advancing through the given
	 *         Age.
	 */
	public boolean passAstRequirements(Age age) {
		AstRequirements reqs = Game.AGE_REQUIREMENTS.get(this.difficulty).get(age);
		return this.getCityCount() >= reqs.getMinCities() && this.techs.size() >= reqs.getMinAdvances()
				&& this.getTechCountByVP(1) >= reqs.getMinLevelOneTechs()
				&& this.getTechCountByVP(3) + this.getTechCountByVP(6) >= reqs.getMinLevelTwoPlusTechs()
				&& this.getTechCountByVP(6) >= reqs.getMinLevelThreeTechs()
				&& this.getVPfromTech() >= reqs.getMinTechVP();
	}

	/**
	 * Create a string listing all of the AST requirements for the given age, color-coded by whether the civiliztion
	 * currently meets each requirement. Requirements in green are currently satisfied; requirements in red are
	 * currently not satisfied.
	 * 
	 * @param age
	 *            The specified Age.
	 * @return A string representation of the given age's requirements.
	 */
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

	/**
	 * Get the AST step on which the specified Age begins for this civilization.
	 * 
	 * @param age
	 *            The specified Age.
	 * @return The first AST step of the given Age.
	 */
	public int getAgeStart(Civilization.Age age) {
		return Game.AST_TABLE.get(this.getName()).getAgeStart(age, this.difficulty);
	}

	/**
	 * Set the current game difficulty.
	 * 
	 * @param difficulty
	 *            The new game difficulty.
	 */
	public void setDifficulty(Difficulty difficulty) {
		this.difficulty = difficulty;
	}

	/**
	 * Get the current population.
	 * 
	 * @return The current population.
	 */
	public int getPopulation() {
		return this.population;
	}

	/**
	 * Set the current population.
	 * 
	 * @param population
	 *            The new population.
	 */
	public void setPopulation(int population) {
		this.population = population;
	}

	public void setSmallGameCredits(int credit) {
		for (Technology.Type type : Technology.Type.values()) {
			this.smallGameCredits.put(type, credit);
		}
	}

	/**
	 * Get the current number of cities.
	 * 
	 * @return The current number of cities.
	 */
	public int getCityCount() {
		return this.nCities;
	}

	/**
	 * Set the current number of cities.
	 * 
	 * @param nCities
	 *            The new number of cities.
	 */
	public void setCityCount(int nCities) {
		this.nCities = nCities;
	}

	/**
	 * Add (purchase) a new advance to this civilization.
	 * 
	 * @param newTech
	 *            The new advance.
	 * @param round
	 *            TODO
	 */
	public void addTech(Technology newTech, int round) {
		this.techs.put(newTech, round);
	}

	/**
	 * Removes all technologies that were purchased in the given round. This allows the undo of technology purchases.
	 * 
	 * @param currentRound
	 *            the current round number to undo.
	 * 
	 */
	public ArrayList<Technology> undoTechPurchase(int currentRound) {
		ArrayList<Technology> undoneTechs = new ArrayList<Technology>();
		for (Technology tech : Civilization.this.techs.keySet()) {
			Integer round = Civilization.this.techs.get(tech);
			if (round == currentRound) {
				undoneTechs.add(tech);
			}
		}
		for (Technology tech : undoneTechs) {
			this.techs.remove(tech);
		}
		return undoneTechs;
	}

	/**
	 * Set whether this civilization has purchased advances yet this turn.
	 * 
	 * @param hasPurchased
	 *            boolean specifying whether advances have been purchased this turn.
	 */
	public void setPurchased(boolean hasPurchased) {
		this.hasPurchased = hasPurchased;
	}

	/**
	 * Get whether this civilization has purchased advances yet this turn.
	 * 
	 * @return boolean specifying whether advances have been purchased this turn.
	 */
	public boolean hasPurchased() {
		return this.hasPurchased;
	}

	/**
	 * Get a copy of this Civilization object. This is used to create a copy of Civilization data while editing to allow
	 * for reversion to the previous state.
	 * 
	 * @return A deep copy of this Civilization.
	 */
	public Civilization clone() {
		HashMap<Technology, Integer> techs = new HashMap<Technology, Integer>() {
			private static final long serialVersionUID = 577732084086917712L;

			{
				for (Technology tech : Civilization.this.techs.keySet()) {
					put(tech, Civilization.this.techs.get(tech));
				}
			}
		};
		HashMap<Technology, ArrayList<Technology.Type>> extraTypeCredits =
				new HashMap<Technology, ArrayList<Technology.Type>>() {
					private static final long serialVersionUID = 1L;
					{
						for (Technology tech : Civilization.this.typeCredits.keySet()) {
							put(tech, Civilization.this.typeCredits.get(tech));
						}
					}
				};
		return new Civilization(this.name, this.player, this.population, this.nCities, techs, smallGameCredits,
				extraTypeCredits, this.astPosition, this.difficulty, this.hasPurchased);
	}

	/**
	 * Get a list of the advances owned by this civilization
	 * 
	 * @return A list of advances owned by this civilization.
	 */
	public ArrayList<Technology> getTechs() {
		return new ArrayList<Technology>(this.techs.keySet());
	}

	/**
	 * Determine whether this civilization owns an advance.
	 * 
	 * @param tech
	 *            The specified advance.
	 * @return Whether this civilization owns the given advance.
	 */
	public boolean hasTech(Technology tech) {
		return this.techs.containsKey(tech);
	}

	/**
	 * Get the player playing this civilization.
	 * 
	 * @return The player name.
	 */
	public String getPlayer() {
		return this.player;
	}

	/**
	 * Set the player playing this civilization.
	 * 
	 * @param player
	 *            The new player name.
	 */
	public void setPlayer(String player) {
		this.player = player;
	}

	/**
	 * Get the current cost for this civilization to purchase an advance. This includes all credits from previously
	 * purchased advance cards.
	 * 
	 * @param tech
	 *            The specified advance
	 * @return The cost for this civilization to purchase the given advance.
	 */
	public int getCost(Technology tech) {
		int cost = tech.getBaseCost();

		int maxTypeDiscount = 0;

		// Determine the type that provides the largest discount.
		for (Technology.Type type : tech.getTypes()) {
			maxTypeDiscount = Math.max(this.getTypeCredit(type), maxTypeDiscount);
		}

		// Reduce the cost by the maximum type discount.
		cost = cost - maxTypeDiscount;

		// Apply any additional credit based on the specific advance.
		for (Technology tech2 : this.getTechs()) {
			cost = cost - tech2.getTechCredit(tech);
		}

		// If the cost is negative, return 0 instead.
		return Math.max(cost, 0);
	}

	/**
	 * Get the number of credits towards the specified advance Type. This includes credits from all purchased advances,
	 * including additional credits chosen for {@link Technology#MONUMENT Monument} and {@link Technology#WRITTEN_RECORD
	 * Written Record}.
	 * 
	 * @param type
	 *            The specified type.
	 * @return The number of credits for the specified type.
	 */
	public int getTypeCredit(Technology.Type type) {
		int credit = this.smallGameCredits.get(type);
		// Add credits from each advance
		for (Technology tech : this.techs.keySet()) {
			credit = credit + tech.getTypeCredit(type);
		}
		// Add additional credits chosen for Monument and Written Record
		for (Technology tech : this.typeCredits.keySet()) {
			if (this.hasTech(tech)) {
				credit = credit + Collections.frequency(this.typeCredits.get(tech), type) * Game.VP_PER_AST_STEP;
			}
		}
		return credit;
	}

	/**
	 * Get a list of the chosen credits for advances that provide additional credits ({@link Technology#MONUMENT
	 * Monument} and {@link Technology#WRITTEN_RECORD Written Record}).
	 * 
	 * @param tech
	 *            The specified advance.
	 * @return A list of the type credits chosen (worth 5 each).
	 */
	public ArrayList<Technology.Type> getTypeCredits(Technology tech) {
		return this.typeCredits.get(tech);
	}

	/**
	 * Set the additional type credits for advances that provide additional credits ({@link Technology#MONUMENT
	 * Monument} and {@link Technology#WRITTEN_RECORD Written Record}).
	 * 
	 * @param tech
	 *            The specified advance.
	 * @param newCredits
	 *            A list of the type credits chosen (worth 5 each).
	 */
	public void addTypeCredits(Technology tech, ArrayList<Technology.Type> newCredits) {
		this.typeCredits.put(tech, newCredits);
	}

	/**
	 * Get the current AST position.
	 * 
	 * @return The current AST position.
	 */
	public int getAstPosition() {
		return this.astPosition;
	}

	/**
	 * Set the current AST position.
	 * 
	 * @param newAstPosition
	 *            The new AST position.
	 */
	public void setAstPosition(int newAstPosition) {
		this.astPosition = newAstPosition;
	}

	/**
	 * Get the AST rank of this civilization.
	 * 
	 * @return The AST rank.
	 */
	public int getAst() {
		return Game.AST_TABLE.get(this.name).astRank;
	}

	/**
	 * Sort the specified list of civilizations using the specified sort method and direction.
	 * 
	 * @param civs
	 *            List of civilizations to be sorted.
	 * @param sort
	 *            The desired sort type.
	 * @param direction
	 *            The desired sort direction.
	 * @return A sorted list of civilizations.
	 */
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

	/**
	 * Sort a list of civilizations based on the number of victory points. Ties are broken based on AST.
	 * 
	 * @param civs
	 *            List of civilizations to be sorted.
	 * @param direction
	 *            The desired sort direction.
	 * @return A sorted list of civilizations.
	 */
	public static ArrayList<Civilization> sortByVP(ArrayList<Civilization> civs, SortDirection direction) {
		Collections.sort(civs, new VpComparator());
		if (direction == SortDirection.DESCENDING) {
			Collections.reverse(civs);
		}
		return civs;
	}

	/**
	 * Sort a list of civilizations based on the number of cities. Ties are broken based on AST rank. This sort is used
	 * for handing out trade cards.
	 * 
	 * @param civs
	 *            List of civilizations to be sorted.
	 * @param direction
	 *            The desired sort direction.
	 * @return A sorted list of civilizations.
	 */
	public static ArrayList<Civilization> sortByCities(ArrayList<Civilization> civs, SortDirection direction) {
		Collections.sort(civs, new CityComparator());
		if (direction == SortDirection.DESCENDING) {
			Collections.reverse(civs);
		}
		return civs;
	}

	/**
	 * Sort a list of civilizations based on the movement order. This is usually census order, but may be modified by
	 * the advance {@link Technology#MILITARY Military}.
	 * 
	 * @param civs
	 *            List of civilizations to be sorted.
	 * @param direction
	 *            The desired sort direction.
	 * @return A sorted list of civilizations.
	 */
	public static ArrayList<Civilization> sortByMovement(ArrayList<Civilization> civs, SortDirection direction) {
		Collections.sort(civs, new MovementComparator());
		if (direction == SortDirection.DESCENDING) {
			Collections.reverse(civs);
		}
		return civs;
	}

	/**
	 * Sort a list of civilizations, then convert to a list of only the civilization names.
	 * 
	 * @param civs
	 *            List of civilizations to be sorted.
	 * @param sort
	 *            The desired sort method.
	 * @param direction
	 *            The desired sort direction.
	 * @return
	 */
	public static ArrayList<Civilization.Name> sortByToName(ArrayList<Civilization> civs, Civilization.SortOption sort,
			SortDirection direction) {
		ArrayList<Civilization> sortedCivs = sortBy(civs, sort, direction);
		ArrayList<Civilization.Name> sortedNames = new ArrayList<Civilization.Name>();
		for (Civilization civ : sortedCivs) {
			sortedNames.add(civ.getName());
		}
		return sortedNames;
	}

	/**
	 * Sort a list of civilizations based on the AST [rank].
	 * 
	 * @param civs
	 *            List of civilizations to be sorted.
	 * @param direction
	 *            The desired sort direction.
	 * @return A sorted list of civilizations.
	 */
	public static ArrayList<Civilization> sortByAst(ArrayList<Civilization> civs, SortDirection direction) {
		Collections.sort(civs);
		if (direction == SortDirection.DESCENDING) {
			Collections.reverse(civs);
		}
		return civs;
	}

	/**
	 * Sort a list of civilizations based on the census. Ties are broken by AST rank.
	 * 
	 * @param civs
	 *            List of civilizations to be sorted.
	 * @param direction
	 *            The desired sort direction.
	 * @return A sorted list of civilizations.
	 */
	public static ArrayList<Civilization> sortByCensus(ArrayList<Civilization> civs, SortDirection direction) {
		Collections.sort(civs, new CensusComparator());
		if (direction == SortDirection.DESCENDING) {
			Collections.reverse(civs);
		}
		return civs;
	}


	/**
	 * Sort a list of civilizations based on their AST position (number of steps advanced).
	 * 
	 * @param civs
	 *            List of civilizations to be sorted.
	 * @param direction
	 *            The desired sort direction.
	 * @return A sorted list of civilizations.
	 */
	public static ArrayList<Civilization> sortByAstPosition(ArrayList<Civilization> civs, SortDirection direction) {
		Collections.sort(civs, new AstPositionComparator());
		if (direction == SortDirection.DESCENDING) {
			Collections.reverse(civs);
		}
		return civs;
	}

	/**
	 * Get the number of victory points of owned advances.
	 * 
	 * @return The number of victory points.
	 */
	public int getVPfromTech() {
		int vp = 0;
		for (Technology tech : this.techs.keySet()) {
			vp = vp + tech.getVP();
		}
		return vp;
	}

	/**
	 * Get the number of victory points from all sources except the Late Iron Age bonus.
	 * 
	 * @return The number of victory points.
	 */
	public int getVP() {
		return this.nCities + this.astPosition * Game.VP_PER_AST_STEP + getVPfromTech();
		// TODO Need to add adjustment for Late Iron Age only bonus
	}

	/**
	 * Create a string that lists the number of advances owned at each of the three tiers.
	 * 
	 * @return A string specifying the number of advances owned in each tier.
	 */
	public String getTechBreakdownString() {
		String s = "<html>";
		s = s + "<table cellpadding='1' cellspacing='1'><tr><td align='right'>1 VP (&lt;100)</td><td>"
				+ this.getTechCountByVP(1) + "</td></tr>";
		s = s + "<tr><td>3 VP (&gt;100)</td><td align='right'>" + this.getTechCountByVP(3) + "</td></tr>";
		s = s + "<tr><td>6 VP (&gt;200)</td><td align='right'>" + this.getTechCountByVP(6)
				+ "</td></tr></table></html>";
		return s;
	}

	/**
	 * Create a string that lists the number of victory points provided from each source (AST, advances, Cities).
	 * 
	 * @return A string specifying the number of victory points from each source.
	 */
	public String getVpBreakdownString() {
		String s = "<html>";
		s = s + "<table cellpadding='1' cellspacing='1'><tr><td>AST</td><td align='right'>"
				+ this.astPosition * Game.VP_PER_AST_STEP + "</td></tr>";
		s = s + "<tr><td>Tech</td><td align='right'>" + this.getVPfromTech() + "</td></tr>";
		s = s + "<tr><td>Cities</td><td align='right'>" + this.getCityCount() + "</td></tr></table></html>";
		return s;
	}

	/**
	 * Create a string that includes much of the current state of the Civilization. Used mostly for development.
	 * 
	 * @return A string including much of the current state of this Civliization.
	 */
	public String toFullString() {
		String s = this.toString() + " (" + this.player + ")\n";
		s = s + "Current Score: " + this.getVP() + "\n";
		s = s + "Current AST Step: " + this.astPosition + "(" + this.getCurrentAge() + ")\n";
		s = s + "Next Step Age: " + this.getNextStepAge() + "\n";
		s = s + "Cities: " + this.nCities + " Population: " + this.population + "\n";
		s = s + "Advances:" + this.techs + "</html>";
		return s;
	}

	/**
	 * A comparator for comparing two civilizations by census.
	 */
	private final static class CensusComparator implements Comparator<Civilization> {
		public int compare(Civilization civ1, Civilization civ2) {
			int result = -Integer.compare(civ1.getPopulation(), civ2.getPopulation());
			if (result == 0) {
				result = civ1.compareTo(civ2);
			}
			return result;
		}
	}

	/**
	 * A comparator for comparing two civilizations by number of victory points.
	 */
	private final static class VpComparator implements Comparator<Civilization> {
		public int compare(Civilization civ1, Civilization civ2) {
			int result = -Integer.compare(civ1.getVP(), civ2.getVP());
			if (result == 0) {
				result = civ1.compareTo(civ2);
			}
			return result;
		}
	}

	/**
	 * A comparator for comparing two civilizations by number of cities.
	 */
	private final static class CityComparator implements Comparator<Civilization> {
		public int compare(Civilization civ1, Civilization civ2) {
			int result = -Integer.compare(civ1.getCityCount(), civ2.getCityCount());
			if (result == 0) {
				result = -civ1.compareTo(civ2);
			}
			return result;
		}
	}

	/**
	 * A comparator for comparing two civilizations by AST position.
	 */
	private final static class AstPositionComparator implements Comparator<Civilization> {
		public int compare(Civilization civ1, Civilization civ2) {
			int result = -Integer.compare(civ1.astPosition, civ2.astPosition);
			if (result == 0) {
				result = civ1.compareTo(civ2);
			}
			return result;
		}
	}

	/**
	 * A comparator for comparing two civilizations by which moves first.
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Civilization otherCiv) {
		int result = this.name.compareTo(otherCiv.name);
		return result;
	}

	/**
	 * A data object for holding AST requirements for a given Age.
	 */
	public static final class AstRequirements {

		@JsonProperty("minCities")
		private final int minCities;

		/**
		 * @return The minimum number of cities to advance in AST.
		 */
		public int getMinCities() {
			return this.minCities;
		}

		/**
		 * @return The minimum number of advances to advance in AST.
		 */
		public int getMinAdvances() {
			return this.minAdvances;
		}

		/**
		 * @return The minimum number of tier one advances to advance in AST.
		 */
		public int getMinLevelOneTechs() {
			return this.minLevelOneTechs;
		}

		/**
		 * @return The minimum number of tier two advances to advance in AST.
		 */
		public int getMinLevelTwoPlusTechs() {
			return this.minLevelTwoPlusTechs;
		}

		/**
		 * @return The minimum number of tier three advances to advance in AST.
		 */
		public int getMinLevelThreeTechs() {
			return this.minLevelThreeTechs;
		}

		/**
		 * @return The minimum number of victory points from advances to advance in AST.
		 */
		public int getMinTechVP() {
			return this.minTechVP;
		}

		/**
		 * @return The text representation of these requirements.
		 */
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

		@Override
		public String toString() {
			return "Text: " + this.text + "\n  Min Cities: " + this.minCities + "\n  Min Advances: " + this.minAdvances
					+ "\n  Min L1 Techs: " + this.minLevelOneTechs + "\n  Min L2+ Techs: " + this.minLevelTwoPlusTechs
					+ "\n  Min L3 Techs: " + this.minLevelThreeTechs + "\n  Min Tech VP: " + this.minTechVP;
		}

	}

	/**
	 * A data object to hold all of the static data from the AST table for this civilization.
	 */
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

		/**
		 * @return AST rank for this civilization.
		 */
		public int getAstRank() {
			return this.astRank;
		}

		/**
		 * @return Normal region for this civilization.
		 */
		public Region getRegion() {
			return this.region;
		}

		/**
		 * Get the AST step on which the specified age begins.
		 * 
		 * @param age
		 *            The specified age.
		 * @param difficulty
		 *            The game difficulty.
		 * @return The first AST step of the specified age.
		 */
		public int getAgeStart(Age age, Difficulty difficulty) {
			return this.subTable.get(difficulty).get(age).intValue();
		}
	}

	/**
	 * Determine whether this civilization is the only one in the late iron age.
	 * 
	 * @param civs
	 *            Array of all civilizations in game
	 * @return Whether this civilization is the only one in the late iron age.
	 */
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

	/**
	 * Remove a technological advance from this civilization.
	 * 
	 * @param tech
	 */
	public void removeTech(Technology tech) {
		this.techs.remove(tech);
	}


}

