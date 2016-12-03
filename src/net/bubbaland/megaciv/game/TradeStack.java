package net.bubbaland.megaciv.game;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.text.WordUtils;

import net.bubbaland.megaciv.game.Civilization.Region;

public class TradeStack {
	public static final int															MAX_HTML_WIDTH	= 50;
	private HashMap<TradeStack.TradeGood, HashMap<Civilization.Region, Integer>>	goods;
	private ArrayList<TradeStack.Calamity>											calamities;
	private final int																stackNumber;

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

	public String toString() {
		String s = "  Stack Number " + this.stackNumber + "\n";
		for (TradeCard card : this.goods.keySet()) {
			s = s + "    " + card.toString() + "\n";
			for (Region region : this.goods.get(card).keySet()) {
				int quantity = this.goods.get(card).get(region);
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
			String s = "<html><strong>" + this.toString() + "</strong><br/>";
			s = s + "<i>Minor Calamity</i><BR/>";
			for (String t : text.split("<BR/>")) {
				Pattern regex = Pattern.compile("(.{1," + MAX_HTML_WIDTH + "}(?:\\s|$))", Pattern.DOTALL);
				Matcher matcher = regex.matcher(t);
				while (matcher.find()) {
					s = s + matcher.group() + "<BR/>";
				}
			}
			s = s + "</html>";
			this.html = s;
		}

		public String toHtmlString() {
			return this.html;
		}

		public int sortOrder() {
			return 0;
		}

		public String toString() {
			String s = this.name();
			s = s.replace("_", " ");
			s = WordUtils.capitalizeFully(s);
			return s;
		}
	}

	public enum MajorTradableCalamity implements Calamity {
		VOLCANIC_ERUPTION_EARTHQUAKE(
				"<strong>Volcanic Eruption</strong>: Only if you have a city in an area with a volcano, destroy all units (irrespective of ownership) in the area(s) touched by the volcano. If you have cities in more than 1 area with a volcano, select the volcano that would affect the most of your unit points."
						+ "<BR/><strong>Earthquake</strong>: If you have no cities in an area with a volcano, select and destroy 1 city and select and reduce 1 city adjacent by land or water (irrespective of ownership).") {
			public String toString() {
				return "Volcano/Earthquake";
			}
		},
		FAMINE("Take 10 damage and assign 5 damage to each of 3 players of your choice."),
		FLOOD("Only if you have any units on a flood plain, take 15 damage from the flood plain. If you have any units on more than 1 flood plain, select the flood plain where the most of your units would be affected. All other players with units on the same flood plain take 5 damage from that flood plain as well. Cities built on black city sites are not considered to be on the flood plain. If you have no units on a flood plain, take 5 damage in total from coastal areas of your choice instead."),
		CIVIL_WAR(
				"Select all but 35 of your unit points. All units thus selected must be in areas adjacent to each other if possible. In each of those areas all of your units must be selected. The beneficiary annexes all selected units."),
		CYCLONE("Select the open sea area that has the largest number of your cities in areas directly adjacent to it. Select 3 of these cities. All other players with cities in areas directly adjacent to the same open sea area must select 2 of their cities in areas adjacent to the open sea area as well."),
		TYRANNY("The beneficiary selects and annexes 15 of your unit points. All units selected this way must be in areas adjacent to each other as much as possible, and in each of those areas all of your units must be selected."),
		CORRUPTION("Discard commodity cards with a total face value (not set value) of at least 10 points."),
		REGRESSION("Your succession marker on the A.S.T. is moved 1 step backward.");

		private String html;

		private MajorTradableCalamity(String text) {
			String s = "<html><strong>" + this.toString() + "</strong><br/>";
			s = s + "<i>Major Calamity — Tradable</i><BR/>";
			for (String t : text.split("<BR/>")) {
				Pattern regex = Pattern.compile("(.{1," + MAX_HTML_WIDTH + "}(?:\\s|$))", Pattern.DOTALL);
				Matcher matcher = regex.matcher(t);
				while (matcher.find()) {
					s = s + matcher.group() + "<BR/>";
				}
			}
			s = s + "</html>";
			this.html = s;
		}

		public String toHtmlString() {
			return this.html;
		}

		public int sortOrder() {
			return 1;
		}

		public String toString() {
			String s = this.name();
			s = s.replace("_", " ");
			s = WordUtils.capitalizeFully(s);
			return s;
		}
	}

	public enum MajorNontradableCalamity implements Calamity {
		TREACHERY("The beneficiary selects and annexes 1 of your cities."),
		SLAVE_REVOLT(
				"Your city support rate is increased by 2 during the resolution of Slave Revolt. Perform a check for city support and reduce cities until you have sufficient support."),
		SUPERSTITION("Reduce 3 of your cities."),
		BARBARIAN_HORDES(
				"The beneficiary selects 1 of your cities (if possible a wilderness city), which is attacked by 15 barbarian tokens. After combat, the beneficiary moves all barbarian tokens in excess of the population limit to an area adjacent by land or water containing your units and combat is resolved again. The beneficiary may only move barbarian tokens into an area containing a city if the attack would be successful. This process is repeated until no population limit is exceeded by the barbarian tokens or no area can legally be chosen. Any barbarian tokens in excess of a population limit are then destroyed."),
		EPIDEMIC(
				"Take 15 damage and select 2 other players that must take 10 damage as well. The beneficiary may not be selected as a secondary victim."),
		CIVIL_DISORDER("Reduce all but 3 of your cities."), ICONOCLASM_AND_HERESY(
				"Reduce 4 of your cities and select 2 other players that must reduce 1 of their cities as well. The beneficiary may not be selected as a secondary victim.") {
			public String toString() {
				return "Iconoclasm and Heresy";
			}
		},
		PIRACY("The beneficiary selects 2 of your coastal cities and you select 1 coastal city from each of 2 other players. All selected cities are replaced by pirate cities. The beneficiary may not be selected as a secondary victim.");

		private String html;

		private MajorNontradableCalamity(String text) {
			String s = "<html><strong>" + this.toString() + "</strong><br/>";
			s = s + "<i>Major Calamity — Non-Tradable</i><BR/>";
			for (String t : text.split("<BR/>")) {
				Pattern regex = Pattern.compile("(.{1," + MAX_HTML_WIDTH + "}(?:\\s|$))", Pattern.DOTALL);
				Matcher matcher = regex.matcher(t);
				while (matcher.find()) {
					s = s + matcher.group() + "<BR/>";
				}
			}
			s = s + "</html>";
			this.html = s;
		}

		public String toHtmlString() {
			return this.html;
		}

		public int sortOrder() {
			return 2;
		}

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

		public String toString() {
			String s = this.name();
			s = s.replace("_", " ");
			s = WordUtils.capitalizeFully(s);
			return s;
		}
	}
}
