package net.bubbaland.megaciv.client.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.WindowEvent;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import net.bubbaland.gui.BubbaDialog;
import net.bubbaland.gui.BubbaDialogPanel;
import net.bubbaland.gui.BubbaGuiController;
import net.bubbaland.gui.BubbaPanel;
import net.bubbaland.megaciv.client.messages.CensusMessage;
import net.bubbaland.megaciv.client.messages.CityUpdateMessage;
import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Game;

public class CityUpdateDialog extends BubbaDialogPanel {

	private static final long							serialVersionUID	= 6704150091226095594L;

	private final GuiClient								client;
	private final HashMap<Civilization.Name, CivPanel>	civPanels;

	public CityUpdateDialog(GuiClient client, BubbaGuiController controller) {
		super(controller);
		this.client = client;

		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.gridx = 0;

		this.civPanels = new HashMap<Civilization.Name, CivPanel>();
		for (Civilization.Name name : this.client.getGame().getCivilizationNames()) {
			CivPanel panel = new CivPanel(controller, name);
			constraints.gridy = name.ordinal();
			this.add(panel, constraints);
			this.civPanels.put(name, panel);
		}

		this.dialog = new BubbaDialog(this.controller, "Update City Count", this, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		this.dialog.setVisible(true);

	}

	public void windowClosed(WindowEvent event) {
		super.windowClosed(event);

		// If the OK button was pressed, open the question
		final int option = ( (Integer) this.dialog.getValue() ).intValue();

		if (option == JOptionPane.OK_OPTION) {
			HashMap<Civilization.Name, Integer> cityCount = new HashMap<Civilization.Name, Integer>();
			for (CivPanel panel : this.civPanels.values()) {
				Civilization.Name name = panel.getCivName();
				int newPop = panel.getCityCount();
				cityCount.put(name, newPop);
			}
			this.client.log("Sending new ceity count: " + cityCount);
			this.client.sendMessage(new CityUpdateMessage(cityCount));
		}
	}

	private class CivPanel extends BubbaPanel {

		private static final long		serialVersionUID	= -487711727769927447L;

		private final JLabel			label;
		private final JSpinner			spinner;
		private final Civilization.Name	name;

		public CivPanel(BubbaGuiController controller, Civilization.Name name) {
			super(controller);
			this.name = name;

			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.CENTER;
			constraints.weightx = 1.0;
			constraints.weighty = 1.0;

			constraints.gridx = 0;
			constraints.gridy = 0;
			this.label = new JLabel(Game.capitalizeFirst(name.toString()), JLabel.LEFT);
			this.label.setPreferredSize(new Dimension(120, 20));
			this.add(this.label, constraints);
			constraints.weightx = 0.0;

			constraints.gridx = 1;
			constraints.gridy = 0;
			this.spinner = new JSpinner(
					new SpinnerNumberModel(CityUpdateDialog.this.client.getGame().getCivilization(name).getCityCount(),
							0, Game.MAX_CITIES, 1));
			this.add(this.spinner, constraints);
		}

		public Civilization.Name getCivName() {
			return this.name;
		}

		public int getCityCount() {
			return (int) this.spinner.getValue();
		}

	}

}
