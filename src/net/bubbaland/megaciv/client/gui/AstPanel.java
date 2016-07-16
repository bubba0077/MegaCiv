package net.bubbaland.megaciv.client.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import javax.swing.JSeparator;
import net.bubbaland.gui.BubbaFrame;
import net.bubbaland.gui.BubbaMainPanel;

public class AstPanel extends BubbaMainPanel {

	private static final long	serialVersionUID	= -1864908035328333195L;

	private ControlsPanel		controlPanel;
	private ScrollingAstPanel	scrollingPanel;

	private final GuiClient		client;

	public AstPanel(GuiClient client, GuiController controller, BubbaFrame frame) {
		super(controller, frame);
		if (client == null) {
			System.out.println(this.getClass().getSimpleName() + "Creating " + this.getClass().getSimpleName()
					+ " with null client!");
		}
		this.client = client;

		this.controlPanel = new ControlsPanel(client, controller);
		this.scrollingPanel = new ScrollingAstPanel(client, controller);

		// Set up layout constraints
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		constraints.gridx = 0;
		constraints.gridy = 0;

		this.add(this.controlPanel, constraints);

		constraints.gridy = 1;
		JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
		separator.setPreferredSize(new Dimension(1, 5));
		this.add(separator, constraints);

		constraints.weighty = 1.0;
		constraints.gridy = 2;
		this.add(this.scrollingPanel, constraints);
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
