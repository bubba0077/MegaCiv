package net.bubbaland.megaciv.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Technology {

	public static Technology ADVANCED_MILITARY, AGRICULTURE, ANATOMY, ARCHITECHTURE, ASTRONAVIGATION, CALENDAR,
			CARTOGRAPHY, CLOTH_MAKING, COINAGE, CULTURAL_ASCENDANCY, DEISM, DEMOCRACY, DIASPORA, DIPLOMACY,
			DRAMA_AND_POETRY, EMPIRICISM, ENGINEERING, ENLIGHTENMENT, FUNDAMENTALISM, LAW, LIBRARY, LITERACY, MASONRY,
			MATHEMATICS, MEDICINE, METALWORKING, MILITARY, MINING, MONARCHY, MONOTHEISM, MONUMENT, MUSIC, MYSTICISM,
			MYTHOLOGY, NAVAL_WARFARE, PHILOSOPHY, POLITICS, POTTERY, PROVINCIAL_EMPIRE, PUBLIC_WORKS, RHETORIC,
			ROADBUILDING, SCULPTURE, THEOCRACY, THEOLOGY, TRADE_EMPIRE, TRADE_ROUTES, UNIVERSAL_DOCTRINE, URBANISM,
			WONDER_OF_THE_WORLD, WRITTEN_RECORD;

	public enum Type {
		SCIENCE, ARTS, CRAFTS, CIVICS, RELIGION
	};

	private final static HashMap<Type, String> COLOR;

	static {
		COLOR = new HashMap<Type, String>();
		COLOR.put(Type.SCIENCE, "green");
		COLOR.put(Type.ARTS, "blue");
		COLOR.put(Type.CRAFTS, "orange");
		COLOR.put(Type.CIVICS, "red");
		COLOR.put(Type.RELIGION, "yellow");
	}

	@JsonProperty("name")
	private final String						name;
	@JsonProperty("text")
	private final String						text;
	@JsonProperty("baseCost")
	private final int							baseCost;
	@JsonProperty("vp")
	private final int							vp;
	@JsonProperty("types")
	private final ArrayList<Type>				types;
	@JsonProperty("typeCredits")
	private final HashMap<Type, Integer>		typeCredits;
	@JsonProperty("techCredits")
	private final HashMap<Technology, Integer>	techCredits;

	private Technology(String name, Type[] types, int baseCost, String text) {
		this.name = name;
		this.types = (ArrayList<Type>) Arrays.asList(types);
		this.baseCost = baseCost;
		this.vp = ( baseCost >= 200 ) ? 6 : ( baseCost >= 100 ) ? 3 : 1;
		this.typeCredits = new HashMap<Type, Integer>();
		this.techCredits = new HashMap<Technology, Integer>();
		this.text = text;
	}

	private Technology(@JsonProperty("name") String name, @JsonProperty("types") ArrayList<Type> types,
			@JsonProperty("baseCost") int baseCost, @JsonProperty("vp") int vp, @JsonProperty("text") String text,
			@JsonProperty("typeCredits") HashMap<Type, Integer> typeCredits,
			@JsonProperty("techCredits") HashMap<Technology, Integer> techCredits) {
		this.name = name;
		this.types = types;
		this.baseCost = baseCost;
		this.vp = vp;
		this.typeCredits = typeCredits;
		this.techCredits = techCredits;
		this.text = text;
	}

	public String getName() {
		return this.name;
	}

	public String getText() {
		return this.text;
	}

	public Type[] getTypes() {
		Type[] types = null;
		return this.types.toArray(types);
	}

	public int getBaseCost() {
		return this.baseCost;
	}

	public int getCost(Civilization civ) {
		ArrayList<Technology> techs = civ.getTechs();
		int cost = this.baseCost;
		for (Technology tech : techs) {
			for (Type type : tech.typeCredits.keySet()) {
				if (this.types.contains(type)) {
					cost = -tech.typeCredits.get(type);
				}
			}
			for (Technology tech2 : tech.techCredits.keySet()) {
				if (this.equals(tech2)) {
					cost = -tech.techCredits.get(tech2);
				}
			}
		}
		for (Type type : civ.getTypeCredits().keySet()) {
			if (this.types.contains(type)) {
				cost = -civ.getTypeCredits().get(type);
			}
		}
		return cost;
	}

	public int getVP() {
		return this.vp;
	}

	public String toString() {
		String s = this.name + "\n";
		for (Type t : this.types) {
			s += t.toString() + " (" + COLOR.get(t) + ") ";
		}
		s += "\n";
		s += "Base Cost: " + this.baseCost + "   VP: " + this.vp + "\n";
		s += "Credits: ";
		for (Type t : this.typeCredits.keySet()) {
			s += this.typeCredits.get(t) + " " + t.toString() + " ";
		}
		for (Technology t : this.techCredits.keySet()) {
			s += this.techCredits.get(t) + " " + t.name + " ";
		}
		s += "\n";
		s += this.text + "\n";
		return s;
	}

	static {
		ADVANCED_MILITARY = new Technology("Advanced Military", new Type[] { Type.CIVICS }, 240,
				"1) In conflicts, you may choose to remove tokens from areas adjacent by land. After each round of token removal a new check for token majority must be made. You may decide to wait for other token conflicts to be resolved first.\n"
						+ "2) You are allowed to cause conflict in areas containing units belonging to players holding Cultural Ascendancy.\n"
						+ "Civil Disorder: Reduce 1 additional city.");
		ADVANCED_MILITARY.typeCredits.put(Type.CIVICS, 20);
		ADVANCED_MILITARY.typeCredits.put(Type.CRAFTS, 5);

		AGRICULTURE = new Technology("Agriculture", new Type[] { Type.CRAFTS }, 120,
				"The population limit of '0', '1' and '2' areas on the board is increased by 1 for you as long as these areas do not contain any other players's units or barbarion tokens.\n"
						+ "Famine: If you are the primary victim, take 5 additional damage.");
		AGRICULTURE.typeCredits.put(Type.CRAFTS, 10);
		AGRICULTURE.typeCredits.put(Type.SCIENCE, 5);
		AGRICULTURE.techCredits.put(DEMOCRACY, 20);

		ANATOMY = new Technology("Anatomy", new Type[] { Type.SCIENCE }, 270,
				"(*) Upon purchase, you may choose to acquire a science card with an undiscounted cost price of less then 100 for free.\n"
						+ "Epidemic: If you are a secondary victim, prevent 5 damage.");
		ANATOMY.typeCredits.put(Type.CRAFTS, 5);
		ANATOMY.typeCredits.put(Type.SCIENCE, 20);

		ARCHITECHTURE = new Technology("Architechture", new Type[] { Type.ARTS }, 140,
				"Once per turn, when constructing a city you may choose to pay up to half of the required number of tokens from treasury.");
		ARCHITECHTURE.typeCredits.put(Type.ARTS, 10);
		ARCHITECHTURE.typeCredits.put(Type.SCIENCE, 5);
		ARCHITECHTURE.techCredits.put(MINING, 20);

		ASTRONAVIGATION = new Technology("Astronavigation", new Type[] { Type.SCIENCE }, 80,
				"Your ships are allowed to move through open sea areas.");
		ASTRONAVIGATION.typeCredits.put(Type.RELIGION, 5);
		ASTRONAVIGATION.typeCredits.put(Type.SCIENCE, 10);
		ASTRONAVIGATION.techCredits.put(CALENDAR, 10);

		CALENDAR = new Technology("Calendar", new Type[] { Type.SCIENCE }, 180,
				"Famine: Prevent 5 damage.\n" + "Cyclone: Reduce 2 less selected cities.");
		CALENDAR.typeCredits.put(Type.CIVICS, 5);
		CALENDAR.typeCredits.put(Type.SCIENCE, 10);
		CALENDAR.techCredits.put(PUBLIC_WORKS, 20);

		CARTOGRAPHY = new Technology("Cartography", new Type[] { Type.SCIENCE }, 160,
				"During the Trade Cards Acquisition phase, you may acquire additional trade cards from stack 2 for 5 treasury tokens and/or from stack 7 for 13 treasury tokens per card.\n"
						+ "Piracy: If you are the primary victim, the beneficiary selects and replaces 1 additional coastal city.");
		CARTOGRAPHY.typeCredits.put(Type.ARTS, 5);
		CARTOGRAPHY.typeCredits.put(Type.SCIENCE, 10);
		CARTOGRAPHY.techCredits.put(LIBRARY, 20);

		CLOTH_MAKING = new Technology("Cloth Making", new Type[] { Type.CRAFTS }, 50,
				"Your ships are allowed to move 5 steps.");
		CLOTH_MAKING.typeCredits.put(Type.ARTS, 5);
		CLOTH_MAKING.typeCredits.put(Type.CRAFTS, 10);
		CLOTH_MAKING.techCredits.put(NAVAL_WARFARE, 10);

		COINAGE = new Technology("Coinage", new Type[] { Type.SCIENCE }, 90,
				"You may choose to increase or decrease your tax rate by 1.\n"
						+ "Corruption: Discard 5 additional points of face value.");
		COINAGE.typeCredits.put(Type.CIVICS, 5);
		COINAGE.typeCredits.put(Type.SCIENCE, 10);
		COINAGE.techCredits.put(TRADE_ROUTES, 10);

		CULTURAL_ASCENDANCY = new Technology("Cultural Ascendancy", new Type[] { Type.ARTS }, 280,
				"1) Players are not allowed to cause conflict in areas containing your units, except for areas where a conflict situation already occurs. This does not count for players holding Cultural Ascendancy or Advanced Military.\n"
						+ "2) Your units are protected against the effect of Politics.\n"
						+ "3) Your default city support rate is increased by 1.");
		CULTURAL_ASCENDANCY.typeCredits.put(Type.ARTS, 20);
		CULTURAL_ASCENDANCY.typeCredits.put(Type.RELIGION, 5);

		DEISM = new Technology("Deism", new Type[] { Type.RELIGION }, 70, "Superstition: Reduce 1 less city.");
		DEISM.typeCredits.put(Type.CRAFTS, 5);
		DEISM.typeCredits.put(Type.RELIGION, 5);
		DEISM.techCredits.put(FUNDAMENTALISM, 10);

		DEMOCRACY = new Technology("Democracy", new Type[] { Type.CIVICS }, 220,
				"During the Tax Collection phase you collect tax as usual but your cities do not revolt as a result of a shortage in tax collection.\n"
						+ "Civil War: Select 10 less unit points.\n" + "Civil Disorder: Reduce 1 less city.");
		DEMOCRACY.typeCredits.put(Type.ARTS, 5);
		DEMOCRACY.typeCredits.put(Type.CIVICS, 20);

		DIASPORA = new Technology("Diaspora", new Type[] { Type.RELIGION }, 270,
				"Special ability: You may choose to take up 5 of your tokens from the board and place them anywhere on the board, providing that no population limits are exceeded.");
		DIASPORA.typeCredits.put(Type.ARTS, 5);
		DIASPORA.typeCredits.put(Type.RELIGION, 20);

		DIPLOMACY = new Technology("Diplomacy", new Type[] { Type.ARTS }, 160,
				"Players are not allowed to move tokens into areas containing your cities, except for areas where a conflict situation already occurs. This does not count for players holding Diplomacy or Military");
		DIPLOMACY.typeCredits.put(Type.ARTS, 10);
		DIPLOMACY.typeCredits.put(Type.CIVICS, 5);
		DIPLOMACY.techCredits.put(PROVINCIAL_EMPIRE, 20);

		DRAMA_AND_POETRY = new Technology("Drama and Poetry", new Type[] { Type.ARTS }, 80,
				"Civil War: Select 5 less unit points.\n" + "Civil Disorder: Reduce 1 less city.");
		DRAMA_AND_POETRY.typeCredits.put(Type.ARTS, 10);
		DRAMA_AND_POETRY.typeCredits.put(Type.RELIGION, 5);
		DRAMA_AND_POETRY.techCredits.put(RHETORIC, 10);

		EMPIRICISM = new Technology("Empiricism", new Type[] { Type.CIVICS }, 60, "—");
		EMPIRICISM.typeCredits.put(Type.ARTS, 5);
		EMPIRICISM.typeCredits.put(Type.CIVICS, 5);
		EMPIRICISM.typeCredits.put(Type.CRAFTS, 5);
		EMPIRICISM.typeCredits.put(Type.RELIGION, 5);
		EMPIRICISM.typeCredits.put(Type.SCIENCE, 10);
		EMPIRICISM.techCredits.put(MEDICINE, 10);

		ENGINEERING = new Technology("Engineering", new Type[] { Type.CRAFTS, Type.SCIENCE }, 160,
				"1) Other players require 8 tokens to succesfully attack your cities. Your cities are then replaced by 6 tokens. This does not apply when the attacking player also holds Engineering.\n"
						+ "2) You recquire 6 tokens to succesfully attack other player's cities. Their cities are then replaced by 5 tokens. This does not apply when defending player also holds Engineering.\n"
						+ "Earthquake: Your city is reduced instead of destroyed.\n" + "Flood: Prevent 5 damage.");
		ENGINEERING.typeCredits.put(Type.CRAFTS, 10);
		ENGINEERING.typeCredits.put(Type.SCIENCE, 10);
		ENGINEERING.techCredits.put(ROADBUILDING, 20);

		ENLIGHTENMENT = new Technology("Enlightenment", new Type[] { Type.RELIGION }, 160,
				"Superstition: Reduce 1 less city.\n"
						+ "Slave Revolt: Your city support rate is decreased by 1 during the resolution of Slave Revolt.\n"
						+ "Epidemic: If you are the primary victim, prevent 5 damage.\n"
						+ "Regression: For each step backward, you may choose to prevent the effect by destroying 2 of your cities (if possible non-coastal).");
		ENLIGHTENMENT.typeCredits.put(Type.CRAFTS, 5);
		ENLIGHTENMENT.typeCredits.put(Type.RELIGION, 10);
		ENLIGHTENMENT.techCredits.put(PHILOSOPHY, 20);

		FUNDAMENTALISM = new Technology("Fundamental", new Type[] { Type.RELIGION }, 150,
				"Special ability: You may choose to destroy all units in an area adjacent by land to your units. Barbarion tokens, pirate cities and units belonging to players holding Fundamentalism or Philosophy are unaffected.\n"
						+ "Regression: Your marker is moved backward 1 additional step.");
		FUNDAMENTALISM.typeCredits.put(Type.ARTS, 5);
		FUNDAMENTALISM.typeCredits.put(Type.RELIGION, 10);
		FUNDAMENTALISM.techCredits.put(MONOTHEISM, 20);

		LAW = new Technology("Law", new Type[] { Type.CIVICS }, 150,
				"Tyranny: The beneficiary selects and annexes 5 less unit points.\n"
						+ "Civil Disorder: Reduce 1 less city.\n" + "Corruption: Discard 5 less points of face value.");
		LAW.typeCredits.put(Type.CIVICS, 10);
		LAW.typeCredits.put(Type.RELIGION, 5);
		LAW.techCredits.put(CULTURAL_ASCENDANCY, 20);

		LIBRARY = new Technology("Library", new Type[] { Type.SCIENCE }, 220,
				"(*) You may discount the cost of any one (1) other Civilization Advance that you purchase in the same turn as Library by 40 points.\n"
						+ "Regression: Your marker is moved backward 1 less step.");
		LIBRARY.typeCredits.put(Type.ARTS, 5);
		LIBRARY.typeCredits.put(Type.SCIENCE, 20);

		LITERACY = new Technology("Literacy", new Type[] { Type.ARTS, Type.CIVICS }, 110, "—");
		LITERACY.typeCredits.put(Type.ARTS, 10);
		LITERACY.typeCredits.put(Type.CIVICS, 10);
		LITERACY.typeCredits.put(Type.CRAFTS, 5);
		LITERACY.typeCredits.put(Type.RELIGION, 5);
		LITERACY.typeCredits.put(Type.SCIENCE, 5);
		LITERACY.techCredits.put(MATHEMATICS, 20);

		MASONRY = new Technology("Masonry", new Type[] { Type.CRAFTS }, 60,
				"Cyclone: Reduce 1 less of your selected cities.");
		MASONRY.typeCredits.put(Type.CRAFTS, 10);
		MASONRY.typeCredits.put(Type.SCIENCE, 5);
		MASONRY.techCredits.put(ENGINEERING, 10);

		MATHEMATICS = new Technology("Mathematics", new Type[] { Type.ARTS, Type.SCIENCE }, 250, "—");
		MATHEMATICS.typeCredits.put(Type.ARTS, 20);
		MATHEMATICS.typeCredits.put(Type.CIVICS, 10);
		MATHEMATICS.typeCredits.put(Type.CRAFTS, 10);
		MATHEMATICS.typeCredits.put(Type.RELIGION, 10);
		MATHEMATICS.typeCredits.put(Type.SCIENCE, 20);

		MEDICINE = new Technology("Medicine", new Type[] { Type.SCIENCE }, 140, "Epidemic: Prevent 5 damage.");
		MEDICINE.typeCredits.put(Type.CRAFTS, 5);
		MEDICINE.typeCredits.put(Type.SCIENCE, 10);
		MEDICINE.techCredits.put(ANATOMY, 20);

		METALWORKING = new Technology("Metalworking", new Type[] { Type.CRAFTS }, 90,
				"In conflicts, for each round of token removal all other players not holding Metalworking must remove their token first.");
		METALWORKING.typeCredits.put(Type.CIVICS, 5);
		METALWORKING.typeCredits.put(Type.CRAFTS, 10);
		METALWORKING.techCredits.put(MILITARY, 10);

		MILITARY = new Technology("Military", new Type[] { Type.CIVICS }, 170,
				"1) Your movement phase is after all other players not holding Military have moved.\n"
						+ "2) You are allowed to move tokens into areas containing cities belonging to players holding Diplomacy.");
		MILITARY.typeCredits.put(Type.CIVICS, 10);
		MILITARY.typeCredits.put(Type.CRAFTS, 5);
		MILITARY.techCredits.put(ADVANCED_MILITARY, 20);

		MINING = new Technology("Mining", new Type[] { Type.CRAFTS }, 230,
				"1) During the Trade Cards Acquisition phase, you may acquire additional trade cards from stack 6 and/or stack 8 for 13 treasury tokens per card.\n"
						+ "2) Treasury tokens are worth 2 points when purchasing Civilization Advances.\n"
						+ "Slave Revolt: Your city support rate is increased by 1 during the resolution of Slave Revolt.");
		MINING.typeCredits.put(Type.CRAFTS, 20);
		MINING.typeCredits.put(Type.SCIENCE, 5);

		MONARCHY = new Technology("Monarchy", new Type[] { Type.CIVICS }, 60,
				"You may choose to increase your tax rate by 1.\n"
						+ "Barbarian Hordes: 5 less barbarian tokens are used.\n"
						+ "Tyranny: The beneficiary selects and annexes 5 additional unit points.");
		MONARCHY.typeCredits.put(Type.RELIGION, 5);
		MONARCHY.typeCredits.put(Type.SCIENCE, 10);
		MONARCHY.techCredits.put(LAW, 10);

		MONOTHEISM = new Technology("Montheism", new Type[] { Type.RELIGION }, 240,
				"Special Ability: You may choose to annex all units in an area adjacent by land to your units. Barbarian tokens, pirate cities and units belonging to players holding Monotheism or Theology are unaffected.\n"
						+ "Iconoclasm and Heresy: Reduce 1 additional city.");
		MONOTHEISM.typeCredits.put(Type.CIVICS, 5);
		MONOTHEISM.typeCredits.put(Type.RELIGION, 20);

		MONUMENT = new Technology("Monument", new Type[] { Type.CRAFTS, Type.RELIGION }, 180,
				"(*) Acquire 20 additional points of credit tokens in any combination of colors.");
		MONUMENT.typeCredits.put(Type.CRAFTS, 10);
		MONUMENT.typeCredits.put(Type.RELIGION, 10);
		MONUMENT.techCredits.put(WONDER_OF_THE_WORLD, 20);

		MUSIC = new Technology("Music", new Type[] { Type.ARTS }, 80,
				"Civil War: Select 5 less unit points.\n" + "Civil Disorder: Reduce 1 less city.");
		MUSIC.typeCredits.put(Type.ARTS, 10);
		MUSIC.typeCredits.put(Type.RELIGION, 5);
		MUSIC.techCredits.put(ENLIGHTENMENT, 10);

		MYSTICISM = new Technology("Mysticism", new Type[] { Type.ARTS, Type.RELIGION }, 50,
				"Superstition: Reduce 1 less city.");
		MYSTICISM.typeCredits.put(Type.ARTS, 5);
		MYSTICISM.typeCredits.put(Type.RELIGION, 5);
		MYSTICISM.techCredits.put(MONUMENT, 10);

		MYTHOLOGY = new Technology("Mythology", new Type[] { Type.RELIGION }, 60,
				"Slave Revolt: Your city support rate is decreased by 1 during the resolution of Slave Revolt.");
		MYTHOLOGY.typeCredits.put(Type.ARTS, 5);
		MYTHOLOGY.typeCredits.put(Type.RELIGION, 5);
		MYTHOLOGY.techCredits.put(LITERACY, 10);

		NAVAL_WARFARE = new Technology("Naval Warfare", new Type[] { Type.CIVICS }, 160,
				"1) You ships are allowed to carry 6 tokens.\n"
						+ "2) In conflicts, you may choose to remove ships from the conflict area instead of tokens. After each round of token removal a new check for token majority must be made.\n"
						+ "Piracy: If you are the primary victim, the beneficiary selects and replaces 1 less coastal city. You may not be selected as a secondary victim.\n"
						+ "Civil Disorder: Reduce 1 additional city.");
		NAVAL_WARFARE.typeCredits.put(Type.CIVICS, 10);
		NAVAL_WARFARE.typeCredits.put(Type.CRAFTS, 5);
		NAVAL_WARFARE.techCredits.put(DIASPORA, 10);

		PHILOSOPHY = new Technology("Philosophy", new Type[] { Type.RELIGION, Type.SCIENCE }, 220,
				"Iconoclasm and Heresy: Reduce 2 less cities.\n"
						+ "You units are protected against the effect of Fundamentalism.\n"
						+ "Civil War: Select 5 additional unit points.");
		PHILOSOPHY.typeCredits.put(Type.RELIGION, 20);
		PHILOSOPHY.typeCredits.put(Type.SCIENCE, 20);

		POLITICS = new Technology("Politics", new Type[] { Type.ARTS }, 230,
				"Special ability: You may choose one of two options:\n"
						+ "1) Gain up to 5 treasury tokens from stock.\n"
						+ "2) Annex all units in an area adjacent by land to your units. Pay treasury tokens equal to the number of units annexed or the effect is cancelled. Barbarian tokens, pirate cities and units belonging to players holding Politics or Cultural Ascendancy are unaffected.\n"
						+ "Barbarian Hordes: 5 additional barbarian tokens are used.");
		POLITICS.typeCredits.put(Type.ARTS, 20);
		POLITICS.typeCredits.put(Type.SCIENCE, 5);

		POTTERY = new Technology("Pottery", new Type[] { Type.CRAFTS }, 60, "Famine: Prevent 5 damage.");
		POTTERY.typeCredits.put(Type.ARTS, 5);
		POTTERY.typeCredits.put(Type.CRAFTS, 10);
		POTTERY.techCredits.put(AGRICULTURE, 10);

		PROVINCIAL_EMPIRE = new Technology("Provincial Empire", new Type[] { Type.CIVICS }, 260,
				"Special ability: You may choose to select up to five players that have units adjacent by land or water to your units. These players must choose and give you a commodity card with a face value of at least 2. Players holding Provincial Empire or Public Works may not be selected.\n"
						+ "Barbarian Hordes: 5 additional barbarian tokens are used.\n"
						+ "Tyranny: The beneficiary selects and annexes 5 additional unit points.");
		PROVINCIAL_EMPIRE.typeCredits.put(Type.CIVICS, 20);
		PROVINCIAL_EMPIRE.typeCredits.put(Type.RELIGION, 5);

		PUBLIC_WORKS = new Technology("Public Works", new Type[] { Type.CIVICS }, 230,
				"1) Areas containing your cities may also contain 1 of your tokens.\n"
						+ "2) You are protected against the effect of Provincial Empire.");
		PUBLIC_WORKS.typeCredits.put(Type.CIVICS, 20);
		PUBLIC_WORKS.typeCredits.put(Type.CRAFTS, 5);

		RHETORIC = new Technology("Rhetoric", new Type[] { Type.ARTS }, 130,
				"During the Trade Cards Acquisition phase, you may acquire additional trade cards from stack 3 for 9 treasury tokens per card.");
		RHETORIC.typeCredits.put(Type.ARTS, 10);
		RHETORIC.typeCredits.put(Type.CIVICS, 5);
		RHETORIC.techCredits.put(POLITICS, 20);

		ROADBUILDING = new Technology("Roadbuilding", new Type[] { Type.CRAFTS }, 220,
				"1) When moving over land, your tokens may move 2 areas. Tokens thare are in a conflict situation after one step are not allowed to move any further.\n"
						+ "2) You hand limit of trade cards is increased by 1.\n"
						+ "Epidemic: If you are the primary victim, take 5 additional 5 damage.");
		ROADBUILDING.typeCredits.put(Type.CRAFTS, 20);
		ROADBUILDING.typeCredits.put(Type.SCIENCE, 5);

		SCULPTURE = new Technology("Sculpture", new Type[] { Type.ARTS }, 50,
				"Tyranny: The beneficiary selects and annexes 5 less unit points.");
		SCULPTURE.typeCredits.put(Type.ARTS, 10);
		SCULPTURE.typeCredits.put(Type.CIVICS, 5);
		SCULPTURE.techCredits.put(ARCHITECHTURE, 10);

		THEOCRACY = new Technology("Theocracy", new Type[] { Type.CIVICS, Type.RELIGION }, 80,
				"Iconoclasm and Heresy: You may choose to discard 2 commodity cards to prevent the city reduction effect for you.");
		THEOCRACY.typeCredits.put(Type.CIVICS, 5);
		THEOCRACY.typeCredits.put(Type.RELIGION, 5);
		THEOCRACY.techCredits.put(UNIVERSAL_DOCTRINE, 10);

		THEOLOGY = new Technology("Theology", new Type[] { Type.RELIGION }, 250,
				"Iconoclasm and Heresy: Reduce 3 cities less.\n"
						+ "Your units are protected against the effect of Monotheism.");
		THEOLOGY.typeCredits.put(Type.RELIGION, 20);
		THEOLOGY.typeCredits.put(Type.SCIENCE, 5);

		TRADE_EMPIRE = new Technology("Trade Empire", new Type[] { Type.CRAFTS }, 260,
				"Once per turn, you may choose to use 1 substitute commodity card of at least the same face value when turning in any incomplete set of commodity cards.\n"
						+ "Cyclone: Select and reduce 1 additional city adjacent to the open sea areas.\n"
						+ "Epidemic: If you are the primary victim, take 5 additional damage.");
		TRADE_EMPIRE.typeCredits.put(Type.CIVICS, 5);
		TRADE_EMPIRE.typeCredits.put(Type.CRAFTS, 20);

		TRADE_ROUTES = new Technology("Trade Routes", new Type[] { Type.CRAFTS }, 180,
				"Special ability: You may choose to discard any number of commodity cards to gain treasure tokens at twice the face value of the commodity cards discarded this way.");
		TRADE_ROUTES.typeCredits.put(Type.CRAFTS, 10);
		TRADE_ROUTES.typeCredits.put(Type.RELIGION, 5);
		TRADE_ROUTES.techCredits.put(TRADE_EMPIRE, 20);

		UNIVERSAL_DOCTRINE = new Technology("Universal Doctrine", new Type[] { Type.RELIGION }, 160,
				"Special ability: You may choose to annex 1 pirate city or up to 5 barbarian tokens anywhere on the board.\n"
						+ "Superstition: Reduce 1 additional city.");
		UNIVERSAL_DOCTRINE.typeCredits.put(Type.CIVICS, 5);
		UNIVERSAL_DOCTRINE.typeCredits.put(Type.RELIGION, 10);
		UNIVERSAL_DOCTRINE.techCredits.put(THEOLOGY, 20);

		URBANISM = new Technology("Urbanism", new Type[] { Type.ARTS }, 50,
				"Once per turn, when constructing a wilderness city you may choose to use up to 4 tokens from areas adjacent by land.");
		URBANISM.typeCredits.put(Type.CIVICS, 10);
		URBANISM.typeCredits.put(Type.SCIENCE, 5);
		URBANISM.techCredits.put(DIPLOMACY, 10);

		WONDER_OF_THE_WORLD = new Technology("Wonder of the World", new Type[] { Type.ARTS, Type.CRAFTS }, 290,
				"1) During the Trade Cards Acquisition phase, you may acquire 1 additional trade card for free from a stack number that is higher than your number of cities in play.\n"
						+ "2) Wonders of the World counts as a city during the A.S.T.-alteration phase.\n"
						+ "Corruption: Discard 5 additional points of face value.");
		WONDER_OF_THE_WORLD.typeCredits.put(Type.ARTS, 20);
		WONDER_OF_THE_WORLD.typeCredits.put(Type.CRAFTS, 20);

		WRITTEN_RECORD = new Technology("Written Record", new Type[] { Type.CIVICS, Type.SCIENCE }, 60,
				"(*) Acquire 10 additional points of credit tokens in any combination of colors.");
		WRITTEN_RECORD.typeCredits.put(Type.CIVICS, 5);
		WRITTEN_RECORD.typeCredits.put(Type.SCIENCE, 5);
		WRITTEN_RECORD.techCredits.put(CARTOGRAPHY, 10);
	}

}
