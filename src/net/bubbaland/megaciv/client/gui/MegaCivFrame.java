package net.bubbaland.megaciv.client.gui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

import net.bubbaland.gui.BubbaDragDropTabFrame;
import net.bubbaland.gui.BubbaFrame;
import net.bubbaland.gui.BubbaMainPanel;
import net.bubbaland.megaciv.client.messages.AddCivilizationMessage;
import net.bubbaland.megaciv.client.messages.AssignPlayerMessage;
import net.bubbaland.megaciv.client.messages.NewGameMessage;
import net.bubbaland.megaciv.game.Civilization;

public class MegaCivFrame extends BubbaDragDropTabFrame {

	private static final long	serialVersionUID	= -8995125745966985308L;

	private static String[]		START_TABS			= { "AST" };

	private final GuiController	controller;
	private final GuiClient		client;

	protected MegaCivFrame(GuiClient client, GuiController controller) {
		super(controller);
		this.client = client;
		this.controller = controller;
		this.addTabs(START_TABS);
		this.tabbedPane.setSelectedIndex(0);

		// TODO Test code
		ArrayList<Civilization.Name> civs = new ArrayList<Civilization.Name>(Arrays.asList(Civilization.Name.values()));

		this.client.sendMessage(new NewGameMessage());
		this.client.sendMessage(new AddCivilizationMessage(civs));
		this.client.sendMessage(new AssignPlayerMessage(Civilization.Name.ASSYRIA, "Player A"));
		this.client.sendMessage(new AssignPlayerMessage(Civilization.Name.CARTHAGE, "Player C"));
		this.client.sendMessage(new AssignPlayerMessage(Civilization.Name.EGYPT, "Player E"));
	}

	@Override
	protected void initTabInfoHash() {
		super.initTabInfoHash();
		this.tabInformationHash.put("AST", new TabInformation("Panel showing AST", AstPanel.class));
	}

	public BubbaMainPanel tabFactory(BubbaFrame frame, String tabType)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		return this.tabInformationHash.get(tabType).getTabClass()
				.getConstructor(GuiClient.class, GuiController.class, BubbaFrame.class)
				.newInstance(this.client, this.controller, this);
	}

	public void updateGui(boolean forceUpdate) {
		// this.client.log("Updating " + this.getClass().getSimpleName());
		super.updateGui(forceUpdate);
	}

}
