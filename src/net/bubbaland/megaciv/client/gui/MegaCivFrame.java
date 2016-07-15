package net.bubbaland.megaciv.client.gui;

import java.lang.reflect.InvocationTargetException;
import net.bubbaland.gui.BubbaDragDropTabFrame;
import net.bubbaland.gui.BubbaFrame;
import net.bubbaland.gui.BubbaMainPanel;

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
