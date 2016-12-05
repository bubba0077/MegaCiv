package net.bubbaland.megaciv.game;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import org.apache.commons.lang3.text.WordUtils;

import net.bubbaland.gui.StringTools;
import net.bubbaland.megaciv.game.Civilization.Region;

public class TradeStack {
	public static final int																MAX_HTML_WIDTH	= 80;
	private final HashMap<TradeStack.TradeGood, HashMap<Civilization.Region, Integer>>	goods;
	private ArrayList<TradeStack.Calamity>												calamities;
	private final int																	stackNumber;

	public TradeStack(int stackNumber) {
		this.stackNumber = stackNumber;
		this.goods = new HashMap<TradeStack.TradeGood, HashMap<Civilization.Region, Integer>>();
	}

	public void addCard(TradeStack.TradeGood good, Civilization.Region region, int quantity) {
		if (!this.goods.containsKey(good)) {
			this.goods.put(good, new HashMap<Civilization.Region, Integer>());
		}
		this.goods.get(good).put(region, quantity);
		this.calamities = new ArrayList<Calamity>();
	}

	public void addCalamity(TradeStack.Calamity calamity) {
		this.calamities.add(calamity);
	}

	public int getStackNumber() {
		return this.stackNumber;
	}

	public HashMap<TradeStack.TradeGood, HashMap<Civilization.Region, Integer>> getGoods() {
		return this.goods;
	}

	public ArrayList<Calamity> getCalamities() {
		this.calamities.sort(new CalamityComparator());
		return this.calamities;
	}

	@Override
	public String toString() {
		String s = "  Stack Number " + this.stackNumber + "\n";
		for (final TradeCard card : this.goods.keySet()) {
			s = s + "    " + card.toString() + "\n";
			for (final Region region : this.goods.get(card).keySet()) {
				final int quantity = this.goods.get(card).get(region);
				s = s + "      " + region.toString() + ": " + quantity + "\n";
			}
		}
		return s;
	}

	private class CalamityComparator implements Comparator<Calamity> {

		@Override
		public int compare(Calamity arg0, Calamity arg1) {
			return Integer.compare(arg0.sortOrder(), arg1.sortOrder());
		}

	}

	public interface TradeCard {

	}

	public interface Calamity extends TradeCard {
		public int sortOrder();

		public String toHtmlString();
	}

	public enum MinorCalamity implements Calamity {
		TEMPEST("Take 2 damage in total from coastal areas of your choice and lose 5 treasury tokens"),
		SQUANDERED_WEALTH("Lose 10 treasury tokens."),
		CITY_RIOTS("Reduce 1 of your cities and lose 5 treasury tokens."),
		CITY_IN_FLAMES("Destroy 1 of your cities. You may choose to pay 10 treasury tokens to prevent this."),
		TRIBAL_CONFLICT("Take 5 damage."), MINOR_UPRISING("Destroy 1 of your cities."),
		BANDITRY(
				"Discard 2 commodity cards of your choice. For each card you must discard, you may choose to pay 4 treasury tokens to prevent it."),
		COASTAL_MIGRATION("Destroy 1 of your coastal cities and lose 5 treasury tokens.");

		private String html;

		private MinorCalamity(String text) {
			String s = "<html><strong>" + this.toString() + "</strong><BR/>";
			s = s + "<i>Minor Calamity</i><BR/>";
			s = s + StringTools.wrapHtml(text, MAX_HTML_WIDTH);
			s = s + "</html>";
			this.html = s;
		}

		@Override
		public String toHtmlString() {
			return this.html;
		}

		@Override
		public int sortOrder() {
			return 0;
		}

		@Override
		public String toString() {
			String s = this.name();
			s = s.replace("_", " ");
			s = WordUtils.capitalizeFully(s);
			return s;
		}
	}

	public enum MajorNontradableCalamity implements Calamity {
		VOLCANIC_ERUPTION_EARTHQUAKE(
				"<strong>Volcanic Eruption</strong>: Only if you have a city in an area with a volcano,"
						+ " destroy all units (irrespective of ownership) in the area(s) touched by the volcano. "
						+ "If you have cities in more than 1 area with a volcano, select the volcano that would "
						+ "affect the most of your unit points.<BR/>"
						+ "<strong>Earthquake</strong>: If you have no cities in an area with a volcano, "
						+ "select and destroy 1 city and select and reduce 1 city adjacent by land or water "
						+ "(irrespective of ownership).<BR/>"
						+ "<span color=green><strong>Engineering</strong>: In the case of an Earthquake, your city "
						+ "is reduced rather than destroyed.</span>") {
			@Override
			public String toString() {
				return "Volcano/Earthquake";
			}
		},
		FAMINE("Take 10 damage and assign 5 damage to each of 3 players of your choice.<BR/>"
				+ "<span color=red><strong>Agriculture</strong>: If you are the primary victim, take 5 additional damage.</span><BR/>"
				+ "<span color=green><strong>Pottery</strong>: Prevent 5 damage.</span><BR/>"
				+ "<span color=green><strong>Calendar</strong>: Prevent 5 damage.</span>"),
		FLOOD("Only if you have any units on a flood plain, take 15 damage from the flood plain. "
				+ "If you have any units on more than 1 flood plain, select the flood plain where the "
				+ "most of your units would be affected. All other players with units on the same flood "
				+ "plain take 5 damage from that flood plain as well. Cities built on black city sites "
				+ "are not considered to be on the flood plain. If you have no units on a flood plain, "
				+ "take 5 damage in total from coastal areas of your choice instead.<BR/>"
				+ "<span color=green><strong>Engineering</strong>: Prevent 5 damage.</span>"),
		CIVIL_WAR("Select all but 35 of your unit points. All units thus selected must be in areas adjacent "
				+ "to each other if possible. In each of those areas all of your units must be selected. "
				+ "The beneficiary annexes all selected units.<BR/>"
				+ "<span color=green><strong>Music</strong>: Select 5 less unit points.</span><BR/>"
				+ "<span color=green><strong>Drama and Poetry</strong>: Select 5 less unit points.</span><BR/>"
				+ "<span color=green><strong>Democracy</strong>: Select 10 less unit points.</span><BR/>"
				+ "<span color=red><strong>Philosophy</strong>: Select 5 additional unit points.</span><BR/>"
				+ "<span color=red><strong>Military</strong>: Select 5 additional unit points.</span><BR/>"),
		CYCLONE("Select the open sea area that has the largest number of your cities in areas directly adjacent"
				+ " to it. Select 3 of these cities. All other players with cities in areas directly adjacent to"
				+ " the same open sea area must select 2 of their cities in areas adjacent to the open sea area "
				+ "as well. Reduce all selected cities.<BR/>"
				+ "<span color=red><strong>Trade Empire</strong>: Select 1 additional city in an area adjacent to the open sea area.</span><BR/>"
				+ "<span color=green><strong>Masonry</strong>: Unselect 1 of your cities.</span><BR/>"
				+ "<span color=green><strong>Calendar</strong>: Unselect 2 of your cities.</span><BR/>"),
		TYRANNY("The beneficiary selects and annexes 15 of your unit points. All units selected this way must be "
				+ "in areas adjacent to each other as much as possible, and in each of those areas all of your "
				+ "units must be selected.<BR/>"
				+ "<span color=green><strong>Sculpture</strong>: The beneficiary selects and annexes 5 less unit points.</span><BR/>"
				+ "<span color=green><strong>Law</strong>: The beneficiary selects and annexes 5 less unit points.</span><BR/>"
				+ "<span color=red><strong>Monarchy</strong>: The beneficiary selects and annexes 5 additional unit points.</span><BR/>"
				+ "<span color=red><strong>Provincial Empire</strong>: The beneficiary selects and annexes 5 additional unit points.</span><BR/>"),
		CORRUPTION("Discard commodity cards with a total face value (not set value) of at least 10 points.<BR/>"
				+ "<span color=green><strong>Law</strong>: Discard 5 less points of face value.</span><BR/>"
				+ "<span color=red><strong>Coinage</strong>: Discard 5 additional points of face value.</span><BR/>"
				+ "<span color=red><strong>Wonder of the World</strong>: Discard 5 additional points of face value.</span>"),
		REGRESSION("Your succession marker on the A.S.T. is moved 1 step backward.<BR/>"
				+ "<span color=red><strong>Fundamentalism</strong>: Your marker is moved backward 1 additional step.</span><BR/>"
				+ "<span color=green><strong>Enlightenment</strong>: For each step backward, you may choose to prevent the effect by "
				+ "destroying 2 of your cities (if possible non-coastal</span><BR/>"
				+ "<span color=green><strong>Library</strong>: Your marker is moved backward 1 less step.</span>");

		private String html;

		private MajorNontradableCalamity(String text) {
			String s = "<html><strong>" + this.toString() + "</strong><BR/>";
			s = s + "<i>Major Calamity — Non-Tradable</i><BR/>";
			s = s + StringTools.wrapHtml(text, MAX_HTML_WIDTH);
			s = s + "</html>";
			this.html = s;
		}

		@Override
		public String toHtmlString() {
			return this.html;
		}

		@Override
		public int sortOrder() {
			return 1;
		}

		@Override
		public String toString() {
			String s = this.name();
			s = s.replace("_", " ");
			s = WordUtils.capitalizeFully(s);
			return s;
		}
	}

	// <span color=green><strong></strong>:</span>

	public enum MajorTradableCalamity implements Calamity {
		TREACHERY("The beneficiary selects and annexes 1 of your cities.<BR/>"
				+ "<span color=red><strong>Diplomacy</strong>: The beneficiary selects and annexes 1 additional city.</span>"),
		SLAVE_REVOLT(
				"Your city support rate is increased by 2 during the resolution of Slave Revolt. Perform a check for city "
						+ "support and reduce cities until you have sufficient support.<BR/>"
						+ "<span color=green><strong>Mythology</strong>: Your city support rate is decreased by 1 during the "
						+ "resolution of <strong>Slave Revolt</strong>.</span><BR/>"
						+ "<span color=green><strong>Enlightenment</strong>: Your city support rate is decreased by 1 during the "
						+ "resolution of <strong>Slave Revolt</strong>.</span><BR/>"
						+ "<span color=red><strong>Mining</strong>: Your city support rate is increased by 1 during the "
						+ "resolution of <strong>Slave Revolt</strong>.</span>"),
		SUPERSTITION("Reduce 3 of your cities.<BR/>"
				+ "<span color=green><strong>Mysticism</strong>: Reduce 1 less city.</span><BR/>"
				+ "<span color=green><strong>Deism</strong>: Reduce 1 less city.</span><BR/>"
				+ "<span color=green><strong>Enlightenment</strong>: Reduce 1 less city.</span><BR/>"
				+ "<span color=red><strong>Universal Doctrine</strong>: Reduce 1 additional city.</span>"),
		BARBARIAN_HORDES(
				"The beneficiary selects 1 of your cities (if possible a wilderness city), which is attacked by "
						+ "15 barbarian tokens. After combat, the beneficiary moves all barbarian tokens in excess of the "
						+ "population limit to an area adjacent by land or water containing your units and combat is "
						+ "resolved again. The beneficiary may only move barbarian tokens into an area containing a city "
						+ "if the attack would be successful. This process is repeated until no population limit is exceeded "
						+ "by the barbarian tokens or no area can legally be chosen. Any barbarian tokens in excess of a "
						+ "population limit are then destroyed.<BR/>"
						+ "<span color=green><strong>Monarchy</strong>: 5 less barbarian tokens are used.</span><BR/>"
						+ "<span color=red><strong>Politics</strong>: 5 additional barbarian tokens are used.</span><BR/>"
						+ "<span color=red><strong>Provincial Empire</strong>: 5 additional barbarian tokens are used.</span>"),
		EPIDEMIC("Take 15 damage and select 2 other players that must take 10 damage as well. The beneficiary may not "
				+ "be selected as a secondary victim.<BR/>"
				+ "<span color=green><strong>Medicine</strong>: Prevent 5 damage.</span><BR/>"
				+ "<span color=green><strong>Enlightenment</strong>: If you are the primary victim, prevent 5 damage.</span><BR/>"
				+ "<span color=red><strong>Roadbuilding</strong>: If you are the primary victime, take 5 additional damage.</span><BR/>"
				+ "<span color=red><strong>Trade Empire</strong>: If you are the primary victime, take 5 additional damage.</span><BR/>"
				+ "<span color=green><strong>Anatomoy</strong>: If you are a secondary victim, prevent 5 damage.</span>"),
		CIVIL_DISORDER("Reduce all but 3 of your cities.<BR/>"
				+ "<span color=green><strong>Music</strong>: Reduce 1 less city.</span><BR/>"
				+ "<span color=green><strong>Drama and Poetry</strong>: Reduce 1 less city.</span><BR/>"
				+ "<span color=green><strong>Law</strong>: Reduce 1 less city.</span><BR/>"
				+ "<span color=green><strong>Democracy</strong>: Reduce 1 less city.</span><BR/>"
				+ "<span color=red><strong>Advanced Military</strong>: Reduce 1 additional city.</span><BR/>"
				+ "<span color=red><strong>Naval Warfare</strong>: Reduce 1 additional city.</span>"),
		ICONOCLASM_AND_HERESY(
				"Reduce 4 of your cities and select 2 other players that must reduce 1 of their cities as well. "
						+ "The beneficiary may not be selected as a secondary victim.<BR/>"
						+ "<span color=green><strong>Philosophy</strong>: Reduce 2 less cities.</span><BR/>"
						+ "<span color=green><strong>Theology</strong>: Reduce 3 less cities.</span><BR/>"
						+ "<span color=red><strong>Monotheism</strong>: Reduce 1 additional city.</span><BR/>"
						+ "<span color=green><strong>Theocracy</strong>: You may choose to discard 2 commodity cards "
						+ "to prevent the city reduction effect for you.</span><BR/>") {
			@Override
			public String toString() {
				return "Iconoclasm and Heresy";
			}
		},
		PIRACY("The beneficiary selects 2 of your coastal cities and you select 1 coastal city from each of 2 other "
				+ "players. All selected cities are replaced by pirate cities. The beneficiary may not be selected "
				+ "as a secondary victim.<BR/>"
				+ "<span color=red><strong>Cartography</strong>: If you are the primary victim, the beneficiary selects "
				+ "and replaces 1 additional coastal city.</span><BR/>"
				+ "<span color=red><strong>Naval Warfare</strong>: If you are the primary victim, the beneficiary selects "
				+ "and replaces 1 less coastal city. You may not be selected as a secondary victim.</span><BR/>");

		private String html;

		private MajorTradableCalamity(String text) {
			String s = "<html><strong>" + this.toString() + "</strong><BR/>";
			s = s + "<i>Major Calamity — Tradable</i><BR/>";
			s = s + StringTools.wrapHtml(text, MAX_HTML_WIDTH);
			s = s + "</html>";
			this.html = s;
		}

		@Override
		public String toHtmlString() {
			return this.html;
		}

		@Override
		public int sortOrder() {
			return 2;
		}

		@Override
		public String toString() {
			String s = this.name();
			s = s.replace("_", " ");
			s = WordUtils.capitalizeFully(s);
			return s;
		}
	}

	public enum TradeGood implements TradeCard {
		OCHRE, FLAX, CLAY, HIDES, BONE, PAPYRUS, STONE, IRON, FURS, WAX, FISH, TIMBER, FRUIT, SALT, CERAMICS, WOOL,
		COTTON, OIL, SUGAR, GRAIN, WINE, LACQUER, TEXTILES, LIVESTOCK, GLASS, TIN, SILVER, COPPER, BRONZE, LEAD, RESIN,
		JADE, INCENSE, SPICE, HERBS, MARBLE, DYE, GEMSTONES, TEA, OBSIDIAN, IVORY, SILK, GOLD, PEARLS, AMBER;

		@Override
		public String toString() {
			String s = this.name();
			s = s.replace("_", " ");
			s = WordUtils.capitalizeFully(s);
			return s;
		}
	}
}
