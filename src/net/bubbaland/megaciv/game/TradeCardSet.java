package net.bubbaland.megaciv.game;

import java.util.HashMap;

public class TradeCardSet {

	public static int				N_STACKS	= 9;

	HashMap<Integer, TradeStack>	stacks;

	public TradeCardSet() {
		this.stacks = new HashMap<Integer, TradeStack>();
	}

	public TradeStack getStack(final int stackNumber) {
		return this.stacks.get(stackNumber);
	}

	public void setStack(final int stackNumber, final TradeStack stack) {
		this.stacks.put(stackNumber, stack);
	}

	@Override
	public String toString() {
		String s = "Trade Card Stack:\n";
		for (final int i : this.stacks.keySet()) {
			final TradeStack stack = this.stacks.get(i);
			s = s + "  " + stack + "\n";
		}
		return s;
	}

}

