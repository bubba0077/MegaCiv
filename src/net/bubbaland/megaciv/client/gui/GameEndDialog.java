package net.bubbaland.megaciv.client.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import org.apache.commons.lang3.text.WordUtils;

import net.bubbaland.gui.BubbaDialog;
import net.bubbaland.gui.BubbaDialogPanel;
import net.bubbaland.gui.BubbaGuiController;
import net.bubbaland.gui.BubbaPanel;
import net.bubbaland.megaciv.client.GameClient;
import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Game;
import net.bubbaland.megaciv.messages.CivEditMessage;

public class GameEndDialog extends BubbaDialogPanel {

	private static final long							serialVersionUID	= 6704150091226095594L;

	private final GameClient							client;
	private final HashMap<Civilization.Name, CivPanel>	civPanels;

	private final static int							N_COLUMNS			= 2;

	public GameEndDialog(final GameClient client, final BubbaGuiController controller) {
		super(controller);
		this.client = client;

		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.gridx = 0;

		this.civPanels = new HashMap<Civilization.Name, CivPanel>();
		final ArrayList<Civilization.Name> civNames = this.client.getGame().getCivilizationNames();
		for (final Civilization.Name name : civNames) {
			final CivPanel panel = new CivPanel(controller, name);
			constraints.gridx = name.ordinal() % N_COLUMNS;
			constraints.gridy = name.ordinal() / N_COLUMNS;
			this.add(panel, constraints);
			this.civPanels.put(name, panel);
		}

		this.dialog = new BubbaDialog(this.controller, "Update City Count", this, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		this.dialog.setVisible(true);

	}

	@Override
	public void windowClosed(final WindowEvent event) {
		super.windowClosed(event);

		// If the OK button was pressed, open the question
		final int option = ( (Integer) this.dialog.getValue() ).intValue();

		if (option == JOptionPane.OK_OPTION) {
			for (final CivPanel panel : this.civPanels.values()) {
				final Civilization.Name name = panel.getCivName();
				final Civilization civ = this.client.getGame().getCivilization(name);
				civ.setBuildingOwned(panel.isBuildingOwned());
				civ.setBuildingControlled(panel.isBuildingControlled());
				this.client.sendMessage(new CivEditMessage(civ));
			}
		}
	}

	private class CivPanel extends BubbaPanel implements ActionListener {

		private static final long		serialVersionUID	= -487711727769927447L;

		private final Civilization.Name	name;
		private final JCheckBox			buildingOwnedCheckbox, buildingControlledCheckbox;

		public CivPanel(final BubbaGuiController controller, final Civilization.Name name) {
			super(controller);
			this.name = name;

			final Properties props = controller.getProperties();
			final int civHeight = Integer.parseInt(props.getProperty("CityUpdateDialog.Civ.Height"));
			final int civWidth = Integer.parseInt(props.getProperty("CityUpdateDialog.Civ.Width"));
			final float fontSize = Float.parseFloat(props.getProperty("CityUpdateDialog.FontSize"));

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
			this.buildingOwnedCheckbox = new JCheckBox("Own Civ Building");
			this.buildingOwnedCheckbox.addActionListener(this);
			this.add(this.buildingOwnedCheckbox, constraints);


			constraints.gridx = 2;
			constraints.gridy = 0;
			this.buildingControlledCheckbox = new JCheckBox("Control Civ Building");
			this.buildingControlledCheckbox.setEnabled(false);
			this.add(this.buildingControlledCheckbox, constraints);
		}

		public Civilization.Name getCivName() {
			return this.name;
		}

		public boolean isBuildingOwned() {
			return this.buildingOwnedCheckbox.isSelected();
		}

		public boolean isBuildingControlled() {
			return this.buildingControlledCheckbox.isSelected();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (!( e.getSource() instanceof JCheckBox )) {
				return;
			}
			JCheckBox source = (JCheckBox) e.getSource();
			if (!source.isSelected()) {
				this.buildingControlledCheckbox.setSelected(false);
			}
			this.buildingControlledCheckbox.setEnabled(source.isSelected());
		}

	}

}
