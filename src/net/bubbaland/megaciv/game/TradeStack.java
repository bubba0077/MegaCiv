package net.bubbaland.megaciv.game;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.commons.lang3.text.WordUtils;

import net.bubbaland.megaciv.game.Civilization.Region;

public class TradeStack {
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
	}

	public enum MinorCalamity implements Calamity {
		TEMPEST, SQUANDERED_WEALTH, CITY_RIOTS, CITY_IN_FLAMES, TRIBAL_CONFLICT, MINOR_UPRISING, BANDITRY,
		COASTAL_MIGRATION;

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
		VOLCANIC_ERUPTION_EARTHQUAKE() {
			public String toString() {
				return "Volcano/Earthquake";
			}
		},
		FAMINE, FLOOD, CIVIL_WAR, CYCLONE, TYRANNY, CORRUPTION, REGRESSION;

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
		TREACHERY, SLAVE_REVOLT, SUPERSTITION, BARBARIAN_HORDES, EPIDEMIC, CIVIL_DISORDER, ICONOCLASM_AND_HERESY() {
			public String toString() {
				return "Iconoclasm and Heresy";
			}
		},
		PIRACY;

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
