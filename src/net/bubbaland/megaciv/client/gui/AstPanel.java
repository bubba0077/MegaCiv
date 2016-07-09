package net.bubbaland.megaciv.client.gui;

import java.awt.GridBagConstraints;
import java.util.Properties;

import net.bubbaland.gui.BubbaFrame;
import net.bubbaland.gui.BubbaGuiController;
import net.bubbaland.gui.BubbaMainPanel;

public class AstPanel extends BubbaMainPanel {

	private static final long	serialVersionUID	= -1864908035328333195L;

	private ControlsPanel		controlPanel;
	private FixedPanel			fixedPanel;
	private ScrollingAstPanel	scrollingPanel;

	public AstPanel(BubbaGuiController bubbaGui, BubbaFrame frame) {
		super(bubbaGui, frame);

		// Set up layout constraints
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;

		constraints.gridx = 0;
		constraints.gridy = 0;
		this.controlPanel = new ControlsPanel();
		this.add(this.controlPanel, constraints);

		constraints.weightx = 0.0;
		constraints.weighty = 0.0;
		constraints.gridx = 0;
		constraints.gridy = 1;
		this.fixedPanel = new FixedPanel();
		this.add(this.fixedPanel, constraints);

		constraints.weightx = 0.0;
		constraints.weighty = 1.0;
		constraints.gridx = 1;
		constraints.gridy = 1;
		this.scrollingPanel = new ScrollingAstPanel();
		this.add(this.scrollingPanel, constraints);
	}

	@Override
	public void updateGui(boolean forceUpdate) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void loadProperties(Properties properties) {
		// TODO Auto-generated method stub

	}

}
