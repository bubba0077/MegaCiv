package net.bubbaland.megaciv.game;

import java.util.HashMap;

public class TradeCardSet {

	public static int				N_STACKS	= 9;

	HashMap<Integer, TradeStack>	stacks;

	public TradeCardSet() {
		this.stacks = new HashMap<Integer, TradeStack>();
	}

	public TradeStack getStack(int stackNumber) {
		return this.stacks.get(stackNumber);
	}

	public void setStack(int stackNumber, TradeStack stack) {
		this.stacks.put(stackNumber, stack);
	}

	public String toString() {
		String s = "Trade Card Stack:\n";
		for (int i : stacks.keySet()) {
			TradeStack stack = this.stacks.get(i);
			s = s + "  " + stack + "\n";
		}
		return s;
	}

}

