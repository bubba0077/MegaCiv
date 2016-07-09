package net.bubbaland.megaciv.client.gui;

import net.bubbaland.gui.BubbaDragDropTabFrame;

public class MegaCivFrame extends BubbaDragDropTabFrame {

	private static final long	serialVersionUID	= -8995125745966985308L;

	private static String[]		START_TABS			= { "AST" };

	protected MegaCivFrame(GuiController gui) {
		super(gui, START_TABS);
	}

	@Override
	protected void initTabInfoHash() {
		super.initTabInfoHash();
		this.tabInformationHash.put("AST", new TabInformation("Panel showing AST", AstPanel.class));
	}

}
