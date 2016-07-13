package net.bubbaland.megaciv.client.gui;

import java.util.Properties;

import net.bubbaland.gui.BubbaPanel;

public class ControlsPanel extends BubbaPanel {

	private static final long	serialVersionUID	= 7305427277230101867L;

	private final GuiClient		client;

	public ControlsPanel(GuiClient client, GuiController controller) {
		super(controller);
		this.client = client;
	}

	public void updateGui(boolean forceUpdate) {
		// this.client.log("Updating " + this.getClass().getSimpleName());
		// TODO Auto-generated method stub

	}

	public void loadProperties() {
		Properties props = this.controller.getProperties();
	}

}
