package net.bubbaland.megaciv.client.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import org.apache.commons.lang3.text.WordUtils;

import net.bubbaland.gui.AutoFocusSpinner;
import net.bubbaland.gui.BubbaDialog;
import net.bubbaland.gui.BubbaDialogPanel;
import net.bubbaland.gui.BubbaGuiController;
import net.bubbaland.gui.BubbaPanel;
import net.bubbaland.megaciv.client.GameClient;
import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Game;
import net.bubbaland.megaciv.messages.CensusMessage;

public class CensusDialog extends BubbaDialogPanel {

	private static final long							serialVersionUID	= 6704150091226095594L;

	private final GameClient							client;
	private final HashMap<Civilization.Name, CivPanel>	civPanels;

	private final static int							N_COLUMNS			= 2;

	public CensusDialog(final GameClient client, final BubbaGuiController controller) {
		super(controller);
		this.client = client;

		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.gridx = 0;

		this.civPanels = new HashMap<Civilization.Name, CivPanel>();
		final ArrayList<Civilization.Name> civNames =
				Civilization.sortByToName(this.client.getGame().getCivilizations(), Civilization.SortOption.AST,
						Civilization.SortDirection.DESCENDING);
		for (final Civilization.Name name : civNames) {
			final CivPanel panel = new CivPanel(controller, name);
			constraints.gridx = name.ordinal() % N_COLUMNS;
			constraints.gridy = name.ordinal() / N_COLUMNS;
			this.add(panel, constraints);
			this.civPanels.put(name, panel);
		}

		this.dialog = new BubbaDialog(this.controller, "Take Census", this, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		this.dialog.setVisible(true);

	}

	@Override
	public void windowClosed(final WindowEvent event) {
		super.windowClosed(event);

		// If the OK button was pressed, open the question
		final int option = ( (Integer) this.dialog.getValue() ).intValue();

		if (option == JOptionPane.OK_OPTION) {
			final HashMap<Civilization.Name, Integer> census = new HashMap<Civilization.Name, Integer>();
			for (final CivPanel panel : this.civPanels.values()) {
				final Civilization.Name name = panel.getCivName();
				final int newPop = panel.getPopulation();
				census.put(name, newPop);
			}
			this.client.log("Sending new census: " + census);
			this.client.sendMessage(new CensusMessage(census));
		}
	}

	private class CivPanel extends BubbaPanel {

		private static final long		serialVersionUID	= -487711727769927447L;

		private final AutoFocusSpinner	spinner;
		private final Civilization.Name	name;

		public CivPanel(final BubbaGuiController controller, final Civilization.Name name) {
			super(controller);
			this.name = name;

			final Properties props = controller.getProperties();
			final int civHeight = Integer.parseInt(props.getProperty("CensusDialog.Civ.Height"));
			final int civWidth = Integer.parseInt(props.getProperty("CensusDialog.Civ.Width"));
			final float fontSize = Float.parseFloat(props.getProperty("CensusDialog.FontSize"));

			final Color foreground = Game.FOREGROUND_COLORS.get(name);
			final Color background = Game.BACKGROUND_COLORS.get(name);

			this.setBackground(background);

			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.CENTER;
			constraints.weightx = 1.0;
			constraints.weighty = 1.0;

			constraints.gridx = 0;
			constraints.gridy = 0;
			this.enclosedLabelFactory(WordUtils.capitalizeFully(name.toString()), civWidth, civHeight, foreground,
					background, constraints, fontSize, SwingConstants.LEFT, SwingConstants.CENTER);
			constraints.weightx = 0.0;

			constraints.gridx = 1;
			constraints.gridy = 0;
			this.spinner = new AutoFocusSpinner(
					new SpinnerNumberModel(CensusDialog.this.client.getGame().getCivilization(name).getPopulation(), 1,
							Game.MAX_POPULATION, 1));
			this.spinner.setFont(this.spinner.getFont().deriveFont(fontSize));
			this.add(this.spinner, constraints);
		}

		public Civilization.Name getCivName() {
			return this.name;
		}

		public int getPopulation() {
			return (int) this.spinner.getValue();
		}

	}

}
