package net.bubbaland.megaciv.client.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import javax.swing.BorderFactory;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.bubbaland.gui.BubbaMainPanel;

public class TradeTabPanel extends BubbaMainPanel {

	private static final long	serialVersionUID	= 8852651927672497952L;

	private StopwatchPanel		stopwatchPanel;
	private TradeCardPanel		tradeCardPanel;

	public TradeTabPanel(GuiClient client, GuiController controller, MegaCivFrame frame) {
		super(controller, frame);

		this.stopwatchPanel = new StopwatchPanel(client, controller);
		this.stopwatchPanel.setMinimumSize(new Dimension(0, 0));
		this.tradeCardPanel = new TradeCardPanel(client, controller);
		this.tradeCardPanel.setMinimumSize(new Dimension(0, 0));

		frame.getTabbedPane().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				// stopwatchPanel.resizeFonts();
				tradeCardPanel.resizeFonts();
			}
		});

		// Set up layout constraints
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.gridx = 0;
		constraints.gridy = 0;

		final JSplitPane splitPane =
				new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.stopwatchPanel, this.tradeCardPanel);
		splitPane.setResizeWeight(0.0);
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		this.add(splitPane, constraints);
	}

	@Override
	public void updateGui(boolean forceUpdate) {
		this.stopwatchPanel.updateGui();
		this.tradeCardPanel.updateGui(forceUpdate);
	}

	@Override
	protected void loadProperties() {}

}
