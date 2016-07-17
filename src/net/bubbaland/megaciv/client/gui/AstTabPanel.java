package net.bubbaland.megaciv.client.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;

import javax.swing.JPanel;
import javax.swing.JSeparator;
import net.bubbaland.gui.BubbaFrame;
import net.bubbaland.gui.BubbaMainPanel;

public class AstTabPanel extends BubbaMainPanel {

	private static final long		serialVersionUID	= -1864908035328333195L;

	private final ControlsPanel		controlPanel;
	private final AstTablePanel		scrollingPanel;
	private final CountdownPanel	countdownPanel;

	private final GuiClient			client;

	public AstTabPanel(GuiClient client, GuiController controller, BubbaFrame frame) {
		super(controller, frame);
		if (client == null) {
			System.out.println(this.getClass().getSimpleName() + "Creating " + this.getClass().getSimpleName()
					+ " with null client!");
		}
		this.client = client;

		this.countdownPanel = new CountdownPanel(client, controller);
		this.controlPanel = new ControlsPanel(client, controller);
		this.scrollingPanel = new AstTablePanel(client, controller);

		// Set up layout constraints
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;
		constraints.gridx = 0;
		constraints.gridy = 0;

		this.add(this.controlPanel, constraints);

		constraints.weightx = 1.0;
		constraints.gridx = 1;
		this.add(new JPanel(), constraints);
		constraints.weightx = 0.0;

		constraints.gridx = 2;
		constraints.gridy = 0;
		this.add(this.countdownPanel, constraints);

		constraints.gridwidth = 3;
		constraints.gridx = 0;
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
		this.countdownPanel.updateGui();
	}

	@Override
	protected void loadProperties() {
		this.controlPanel.loadProperties();
		this.scrollingPanel.loadProperties();
		this.countdownPanel.loadProperties();
	}

}
