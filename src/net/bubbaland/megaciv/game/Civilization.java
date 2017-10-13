package net.bubbaland.megaciv.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.text.WordUtils;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.client.gui.GuiClient;
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
		DESCENDING, ASCENDING;
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
	private int getTechCountByVP(int vp) {
		return (int) this.techs.keySet().parallelStream().filter(t -> t.getVP() == vp).count();
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
		int cityCount =
				this.techs.containsKey(Technology.WONDER_OF_THE_WORLD) ? this.getCityCount() + 1 : this.getCityCount();
		return cityCount >= reqs.getMinCities() && this.techs.size() >= reqs.getMinAdvances()
				&& this.getTechCountByVP(1) >= reqs.getMinLevelOneTechs()
				&& this.getTechCountByVP(3) + this.getTechCountByVP(6) >= reqs.getMinLevelTwoPlusTechs()
				&& this.getTechCountByVP(6) >= reqs.getMinLevelThreeTechs()
				&& this.getVPfromTech() >= reqs.getMinTechVP();
	}

	/**
	 * Create a string listing all of the AST requirements for the given age, color-coded by whether the civilization
	 * currently meets each requirement. Requirements in green are currently satisfied; requirements in red are
	 * currently not satisfied.
	 * 
	 * @param age
	 *            The specified Age.
	 * @param printAgeName
	 *            TODO
	 * @return A string representation of the given age's requirements.
	 */
	public String astRequirementString(Age age, boolean printAgeName) {
		AstRequirements reqs = Game.AGE_REQUIREMENTS.get(this.difficulty).get(age);
		String s = "<html>";
		if (printAgeName) {
			s = s + age.toString() + "<BR/>";
		}
		if (age == Age.STONE) {
			s = s + "No Requirements</html>";
			return s;
		}
		if (reqs.getMinCities() > 0) {
			int cityCount = this.techs.containsKey(Technology.WONDER_OF_THE_WORLD) ? this.getCityCount() + 1 : this
					.getCityCount();
			String colorName = cityCount >= reqs.getMinCities() ? "green" : "red";
			String iconName = cityCount >= reqs.getMinCities() ? "check" : "x";
			s = s + "<img height=\"10\" width=\"10\" align=\"bottom\" src=\""
					+ GuiClient.class.getResource("images/" + iconName + ".png") + "\"> <span color='" + colorName
					+ "'>&nbsp;" + reqs.getMinCities() + " Cities</span><BR/>";
		}
		if (reqs.getMinAdvances() > 0) {
			String colorName = this.techs.size() >= reqs.getMinAdvances() ? "green" : "red";
			String iconName = this.techs.size() >= reqs.getMinAdvances() ? "check" : "x";
			s = s + "<img height=\"10\" width=\"10\" align=\"bottom\" src=\""
					+ GuiClient.class.getResource("images/" + iconName + ".png") + "\"> <span color='" + colorName
					+ "'>&nbsp;" + reqs.getMinAdvances() + " Advances</span><BR/>";
		}
		if (reqs.getMinTechVP() > 0) {
			String colorName = this.getVPfromTech() >= reqs.getMinTechVP() ? "green" : "red";
			String iconName = this.getVPfromTech() >= reqs.getMinTechVP() ? "check" : "x";
			s = s + "<img height=\"10\" width=\"10\" align=\"bottom\" src=\""
					+ GuiClient.class.getResource("images/" + iconName + ".png") + "\"> <span color='" + colorName
					+ "'>&nbsp;" + reqs.getMinTechVP() + " VP from Advances</span><BR/>";
		}
		if (reqs.getMinLevelOneTechs() > 0) {
			String colorName = this.getTechCountByVP(1) >= reqs.getMinLevelOneTechs() ? "green" : "red";
			String iconName = this.getTechCountByVP(1) >= reqs.getMinLevelOneTechs() ? "check" : "x";
			s = s + "<img height=\"10\" width=\"10\" align=\"bottom\" src=\""
					+ GuiClient.class.getResource("images/" + iconName + ".png") + "\"> <span color='" + colorName
					+ "'>&nbsp;" + reqs.getMinLevelOneTechs() + " Advances < 100</span><BR/>";
		}
		if (reqs.getMinLevelTwoPlusTechs() > 0) {
			String colorName = this.getTechCountByVP(3) + this.getTechCountByVP(6) >= reqs
					.getMinLevelTwoPlusTechs() ? "green" : "red";
			String iconName = this.getTechCountByVP(3) + this.getTechCountByVP(6) >= reqs
					.getMinLevelTwoPlusTechs() ? "check" : "x";
			s = s + "<img height=\"10\" width=\"10\" align=\"bottom\" src=\""
					+ GuiClient.class.getResource("images/" + iconName + ".png") + "\"> <span color='" + colorName
					+ "'>&nbsp;" + reqs.getMinLevelTwoPlusTechs() + " Advances > 100</span><BR/>";
		}
		if (reqs.getMinLevelThreeTechs() > 0) {
			String colorName = this.getTechCountByVP(6) >= reqs.getMinLevelThreeTechs() ? "green" : "red";
			String iconName = this.getTechCountByVP(6) >= reqs.getMinLevelThreeTechs() ? "check" : "x";
			s = s + "<img height=\"10\" width=\"10\" align=\"bottom\" src=\""
					+ GuiClient.class.getResource("images/" + iconName + ".png") + "\"> <span color='" + colorName
					+ "'>&nbsp;" + reqs.getMinLevelThreeTechs() + " Advances > 200</span><BR/>";
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

		// Reduce the cost by the maximum type discount.
		cost = cost - tech.getTypes().stream().mapToInt(t -> this.getTypeCredit(t)).max().orElse(0);

		// Apply any additional credit based on the specific advance.
		cost = cost - this.getTechs().parallelStream().mapToInt(t -> t.getTechCredit(tech)).sum();

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
		// Start with game-start credits
		int credit = this.smallGameCredits.get(type);

		// Add credits from each advance
		credit = credit + this.techs.keySet().parallelStream().mapToInt(t -> t.getTypeCredit(type)).sum();

		// Add additional credits chosen for Monument and Written Record
		credit = credit + this.typeCredits.keySet().stream().filter(t -> this.hasTech(t))
				.mapToInt(t -> Collections.frequency(this.typeCredits.get(t), type)).sum() * Game.VP_PER_AST_STEP;

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
		if (direction == SortDirection.ASCENDING) {
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
		if (direction == SortDirection.ASCENDING) {
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
		if (direction == SortDirection.ASCENDING) {
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
		if (direction == SortDirection.ASCENDING) {
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
		if (direction == SortDirection.ASCENDING) {
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
		if (direction == SortDirection.ASCENDING) {
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
		return this.techs.keySet().stream().mapToInt(t -> t.getVP()).sum();
	}

	/**
	 * Get the number of victory points from all sources.
	 * 
	 * @return The number of victory points.
	 */
	public int getVP(ArrayList<Civilization> allCivs) {
		int lateIronPoints = this.onlyLateIron(allCivs) ? Game.VP_FROM_ONLY_LATEIRON : 0;
		return this.nCities + this.astPosition * Game.VP_PER_AST_STEP + getVPfromTech() + lateIronPoints;
	}

	/**
	 * Get the number of victory points from all sources except the Late Iron Age bonus.
	 * 
	 * @return The number of victory points.
	 */
	public int getVP() {
		return this.nCities + this.astPosition * Game.VP_PER_AST_STEP + getVPfromTech();
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

	public final static class techCostComparator implements Comparator<Technology> {
		private Civilization civ;

		public techCostComparator() {
			this.civ = null;
		}

		public techCostComparator(Civilization civ) {
			this.civ = civ;
		}

		public int compare(Technology list1, Technology list2) {
			if (civ == null) {
				return Integer.compare(list1.getBaseCost(), list2.getBaseCost());
			} else {
				return Integer.compare(civ.getCost(list1), civ.getCost(list2));
			}
		}
	}

	public final static class totalTechCostComparator implements Comparator<List<Technology>> {
		private Civilization civ;

		public totalTechCostComparator() {
			this.civ = null;
		}

		public totalTechCostComparator(Civilization civ) {
			this.civ = civ;
		}

		public int compare(List<Technology> list1, List<Technology> list2) {
			if (civ == null) {
				return Integer.compare(list1.stream().mapToInt(t -> t.getBaseCost()).sum(),
						list2.stream().mapToInt(t -> t.getBaseCost()).sum());
			} else {
				return Integer.compare(civ.getTotalCost(list1), civ.getTotalCost(list2));
			}
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

	/**
	 * Determines the cost to purchase all technologies in a given list
	 * 
	 * @param techList
	 *            List of technologies to purchase
	 * @return Total cost to purchase all technologies in list, applying all appropriate discounts
	 */
	public int getTotalCost(List<Technology> techList) {

		ArrayList<Technology> techCopy = new ArrayList<Technology>(techList);

		/*
		 * If Anatomy is in the list, remove the most expensive tier 1 technology from the list so its cost is not
		 * included in the title (it is free).
		 */
		if (techList.contains(Technology.ANATOMY)) {
			Technology freeTech = techList.parallelStream().filter(t -> t.getBaseCost() < 100)
					.collect(Collectors.maxBy(new techCostComparator(this))).orElse(null);
			if (freeTech != null) {
				techCopy.remove(freeTech);
			}
		}

		/**
		 * If Library is in the list, determine the discount, which may not exceed the price of one other tech or 40.
		 * 
		 */
		int discount = 0;
		if (techList.contains(Technology.LIBRARY)) {
			techCopy.remove(Technology.LIBRARY);
			discount = Math.min(40, techCopy.stream().mapToInt(t -> this.getCost(t)).max().orElse(0));
			techCopy.add(Technology.LIBRARY);
		}

		/**
		 * Add up the cost of all technologies (except the free one for Anatomy, if applicable) and then reduce the cost
		 * by the Library discount (if applicable)
		 */
		int cost = techCopy.stream().mapToInt(t -> this.getCost(t)).sum() - discount;

		return cost;
	}

	/**
	 * Get the lowest cost list of techs that are worth a given number of VP total
	 * 
	 * @param vp
	 *            Number of total VP for the techs
	 * @param availableTechs
	 *            Techs available to purchase
	 * @return Lowest cost list of techs that are worth vp
	 */
	private List<Technology> getOptimalTechsWorthNVp(int vp, ArrayList<Technology> availableTechs) {
		ArrayList<List<Technology>> list = new ArrayList<List<Technology>>();

		int maxL3 = vp / 6;
		int maxL2 = vp / 3;
		int maxL1 = vp;

		/*
		 * We will never need more techs than the maximum number for each level, so only consider those
		 */
		List<Technology> l1Techs = availableTechs.stream().filter(t -> t.getVP() == 1)
				.sorted(new Technology.techCostComparator()).limit(maxL1).collect(Collectors.toList());
		List<Technology> l2Techs = availableTechs.stream().filter(t -> t.getVP() == 3)
				.sorted(new Technology.techCostComparator()).limit(maxL2).collect(Collectors.toList());
		List<Technology> l3Techs = availableTechs.stream().filter(t -> t.getVP() == 6)
				.sorted(new Technology.techCostComparator()).limit(maxL3).collect(Collectors.toList());
		/*
		 * Always include Anatomy and Library if available as they are special cases that change the total cost
		 */
		l3Techs.addAll(availableTechs.stream()
				.filter(t -> ( t == Technology.ANATOMY && !l3Techs.contains(Technology.ANATOMY) )
						|| ( t == Technology.LIBRARY && !l3Techs.contains(Technology.LIBRARY) ))
				.collect(Collectors.toList()));

		/*
		 * For every combination of nL3, nL2, and nL1 that totals the correct number of VP, check all combinations of
		 * techs from those lists looking for the minimum cost
		 */
		/*- For instance, 12 VP can be gained by:
		 * 2 tier-three (6 VP each), 0 tier-two (3 VP each), and 0 tier-one (1 VP each): (2,0,0)
		 * 1 tier-three, 2 tier-two, and 0 tier-one: (1,2,0)
		 * (1,1,3)
		 * (0,4,0)
		 * (0,3,3)
		 * (0,2,6)
		 * (0,1,9)
		 * (0,0,12)
		 * 
		 */
		IntStream.rangeClosed(0, vp / 6)
				.forEach(nL3 -> IntStream.rangeClosed(0, ( vp - nL3 * 6 ) / 3)
						.forEach(nL2 -> list.add(getOptimalTechByCombinations(l1Techs, l2Techs, l3Techs,
								vp - ( nL3 * 6 + nL2 * 3 ), nL2, nL3))));

		List<Technology> optimalList = list.stream().filter(l -> l != null)
				.collect(Collectors.minBy(new totalTechCostComparator())).orElse(null);

		return optimalList;
	}

	/**
	 * Returns the optimal tech purchase, in terms of maximizing VP, for a given budget.
	 * 
	 * Note: While the return will always have the lowest cost for the max VP possible, purchases including Anatomy (and
	 * less often Library) may be sub-optimal in the sense that the "free" tier-one technology taken may not be the most
	 * expensive available.
	 * 
	 * @param budget
	 *            Amount of money available to spend
	 * @return List of technologies worth the most VP at the lowest cost
	 */
	public List<Technology> getOptimalTechs(int budget) {
		ArrayList<Technology> availableTechs = new ArrayList<Technology>(EnumSet.allOf(Technology.class));
		availableTechs.removeAll(this.getTechs());

		List<Technology> optimalTechs = new ArrayList<Technology>();

		int maxCost = availableTechs.parallelStream().mapToInt(t -> this.getCost(t)).max().orElse(Integer.MAX_VALUE);
		long startTime = System.nanoTime();
		for (int vp = ( budget / maxCost ) * 6 + 1; vp <= availableTechs.size(); vp++) {
			// long loopStartTime = System.nanoTime();
			// System.out.println("Trying " + vp + " VP");
			List<Technology> candidate = this.getOptimalTechsWorthNVp(vp, availableTechs);
			// System.out.println(
			// "Candidate: " + Arrays.toString(candidate.toArray()) + " Cost: " + this.getTotalCost(candidate));
			// long loopEndTime = System.nanoTime();
			// System.out.println("Loop time: " + ( loopEndTime - loopStartTime ) / 1000000000.0 + " s");

			if (this.getTotalCost(candidate) <= budget) {
				optimalTechs = candidate;
			} else {
				// System.out.println("Candidate over budget! Optimal purchase found.");
				break;
			}
		}
		long endTime = System.nanoTime();
		System.out.println("Optimal purchase found in " + ( endTime - startTime ) / 1000000000.0 + " s");

		return optimalTechs;
	}

	/**
	 * Returns the minimum cost technology list for all possible combinations of list provided
	 * 
	 * @param l1Techs
	 *            List of tier-one technologies
	 * @param l2Techs
	 *            List of tier-two technologies
	 * @param l3Techs
	 *            List of tier-three technologies
	 * @param nL1
	 *            Number of tier-one technologies to use
	 * @param nL2
	 *            Number of tier-two technologies to use
	 * @param nL3
	 *            Number of tier-three technologies to use
	 * @return Lowest-cost combination of technologies
	 */
	private static ArrayList<Technology> getOptimalTechByCombinations(List<Technology> l1Techs,
			List<Technology> l2Techs, List<Technology> l3Techs, int nL1, int nL2, int nL3) {
		List<List<Technology>> l1List = Combinations(l1Techs, nL1).collect(Collectors.toList());
		List<List<Technology>> l2List = Combinations(l2Techs, nL2).collect(Collectors.toList());
		List<List<Technology>> l3List = Combinations(l3Techs, nL3).collect(Collectors.toList());

		return ( (Stream<ArrayList<Technology>>) l1List.parallelStream().flatMap(l1t -> {
			return l2List.parallelStream().flatMap(l2t -> {
				return l3List.parallelStream().map(l3t -> {
					ArrayList<Technology> list = new ArrayList<Technology>();
					list.addAll(l1t);
					list.addAll(l2t);
					list.addAll(l3t);
					return list;
				});
			});
		}) ).filter(l -> l != null).collect(Collectors.minBy(new totalTechCostComparator())).orElse(null);
	}

	/**
	 * Copied from https://stackoverflow.com/questions/28515516/enumeration-combinations-of-k-elements-using-java-8
	 * 
	 * @param l
	 * @param size
	 * @return
	 */
	private static <E> Stream<List<E>> Combinations(List<E> l, int size) {
		if (size == 0) {
			return Stream.of(Collections.emptyList());
		} else {
			return IntStream.range(0, l.size()).boxed().<List<E>> flatMap(
					i -> Combinations(l.subList(i + 1, l.size()), size - 1).map(t -> Civilization.pipe(l.get(i), t)));
		}
	}

	private static <E> List<E> pipe(E head, List<E> tail) {
		List<E> newList = new ArrayList<>(tail);
		newList.add(0, head);
		return newList;
	}


}

