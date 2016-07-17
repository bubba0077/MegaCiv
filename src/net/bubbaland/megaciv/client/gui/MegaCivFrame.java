package net.bubbaland.megaciv.client.gui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.bubbaland.gui.BubbaDragDropTabFrame;
import net.bubbaland.gui.BubbaFrame;
import net.bubbaland.gui.BubbaMainPanel;
import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Civilization.Name;
import net.bubbaland.megaciv.game.Game;

public class MegaCivFrame extends BubbaDragDropTabFrame {

	private static final long	serialVersionUID	= -8995125745966985308L;

	private static String[]		START_TABS			= { "AST", "Minoa" };

	private final GuiController	controller;
	private final GuiClient		client;

	protected MegaCivFrame(GuiClient client, GuiController controller) {
		super(controller);
		this.client = client;
		this.controller = controller;
		this.initTabInfoHash();
		this.addTabs(START_TABS);
		this.tabbedPane.setSelectedIndex(0);
	}

	@Override
	public Set<String> getTabNames() {
		Set<String> tabNames = super.getTabNames();
		ArrayList<Civilization.Name> civNames = this.client.getGame().getCivilizationNames();
		for (Iterator<String> iterator = tabNames.iterator(); iterator.hasNext();) {
			String tabName = iterator.next();
			if (Civilization.Name.contains(tabName)
					&& !civNames.contains(Civilization.Name.valueOf(tabName.toUpperCase()))) {
				iterator.remove();
			}
		}
		return tabNames;
	}

	@Override
	protected void initTabInfoHash() {
		super.initTabInfoHash();
		this.tabInformationHash.put("AST",
				new TabInformation("Panel showing AST", AstTabPanel.class,
						new Class<?>[] { GuiClient.class, GuiController.class, BubbaFrame.class },
						new Object[] { this.client, this.controller, this }));
		for (Civilization.Name name : EnumSet.allOf(Civilization.Name.class)) {
			this.tabInformationHash.put(Game.capitalizeFirst(name.toString()),
					new TabInformation(name.toString() + " Information", CivInfoPanel.class,
							new Class<?>[] { GuiClient.class, GuiController.class, BubbaFrame.class,
									Civilization.Name.class },
							new Object[] { this.client, this.controller, this, name }));
		}
	}

	public void updateGui(boolean forceUpdate) {
		// this.client.log("Updating " + this.getClass().getSimpleName());
		super.updateGui(forceUpdate);
	}

}
