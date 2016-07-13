package net.bubbaland.megaciv.client.gui;

import java.awt.GridBagConstraints;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JSplitPane;

import net.bubbaland.gui.BubbaFrame;
import net.bubbaland.gui.BubbaGuiController;
import net.bubbaland.gui.BubbaMainPanel;
import net.bubbaland.megaciv.client.GameClient;

public class AstPanel extends BubbaMainPanel {

	private static final long	serialVersionUID	= -1864908035328333195L;

	private ControlsPanel		controlPanel;
	private ScrollingAstPanel	scrollingPanel;

	private final GuiClient		client;

	public AstPanel(GuiClient client, GuiController controller, BubbaFrame frame) {
		super(controller, frame);
		if (client == null) {
			System.out.println("Creating " + this.getClass().getSimpleName() + " with null client!");
		}
		this.client = client;

		this.controlPanel = new ControlsPanel(client, controller);
		this.scrollingPanel = new ScrollingAstPanel(client, controller);

		// Set up layout constraints
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;

		// this.add(this.scrollingPanel, constraints);

		final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.controlPanel, this.scrollingPanel);
		splitPane.setResizeWeight(0.0);
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		this.add(splitPane, constraints);
	}

	@Override
	public void updateGui(boolean forceUpdate) {
		// this.client.log("Updating " + this.getClass().getSimpleName());
		this.controlPanel.updateGui(forceUpdate);
		this.scrollingPanel.updateGui(forceUpdate);
	}

	@Override
	protected void loadProperties() {
		this.controlPanel.loadProperties();
		this.scrollingPanel.loadProperties();
	}

}
