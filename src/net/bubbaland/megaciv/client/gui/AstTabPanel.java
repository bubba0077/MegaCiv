package net.bubbaland.megaciv.client.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;

import net.bubbaland.gui.BubbaMainPanel;

public class AstTabPanel extends BubbaMainPanel {

	private static final long		serialVersionUID	= -1864908035328333195L;

	private final ControlsPanel		controlPanel;
	private final AstTablePanel		astTablePanel;
	private final StopwatchPanel	countdownPanel;

	public AstTabPanel(GuiClient client, GuiController controller, MegaCivFrame frame) {
		super(controller, frame);
		if (client == null) {
			System.out.println(this.getClass().getSimpleName() + "Creating " + this.getClass().getSimpleName()
					+ " with null client!");
		}
		this.countdownPanel = new StopwatchPanel(client, controller);
		this.controlPanel = new ControlsPanel(client, controller);
		this.astTablePanel = new AstTablePanel(client, controller);

		// Set up layout constraints
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 0.0;
		constraints.weighty = 1.0;
		constraints.gridx = 0;
		constraints.gridy = 0;

		JPanel panel = new JPanel(new GridBagLayout());

		panel.add(this.controlPanel, constraints);

		constraints.weightx = 1.0;
		constraints.gridx = 1;
		panel.add(new JPanel(), constraints);
		constraints.weightx = 0.0;

		constraints.gridx = 2;
		constraints.gridy = 0;
		panel.add(this.countdownPanel, constraints);

		panel.setMinimumSize(new Dimension(0, 0));
		this.astTablePanel.setMinimumSize(new Dimension(0, 0));

		final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panel, this.astTablePanel);
		splitPane.setResizeWeight(0.0);
		splitPane.setBorder(BorderFactory.createEmptyBorder());

		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.gridx = 0;
		constraints.gridy = 0;
		this.add(splitPane, constraints);

		this.loadProperties();
	}

	@Override
	public void updateGui(boolean forceUpdate) {
		this.controlPanel.updateGui(forceUpdate);
		this.astTablePanel.updateGui(forceUpdate);
		this.countdownPanel.updateGui();
	}

	@Override
	public void loadProperties() {
		this.controlPanel.loadProperties();
		this.astTablePanel.loadProperties();
		this.countdownPanel.loadProperties();
	}

}
