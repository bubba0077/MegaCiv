package net.bubbaland.megaciv.client.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang3.text.WordUtils;

import net.bubbaland.gui.BubbaDialog;
import net.bubbaland.gui.BubbaDialogPanel;
import net.bubbaland.gui.BubbaPanel;
import net.bubbaland.megaciv.client.GameClient;
import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Civilization.Region;
import net.bubbaland.megaciv.game.Game;
import net.bubbaland.megaciv.game.Game.Difficulty;
import net.bubbaland.megaciv.messages.NewGameMessage;

public class NewGameDialog extends BubbaDialogPanel implements ActionListener, ChangeListener {

	private static final long							serialVersionUID	= -2854507573608809889L;

	private final JToggleButton							customButton;
	private final JSpinner								nCivSpinner;
	private final JButton								randomizeButton;
	private final JRadioButton							eastRadioButton, westRadioButton;
	private final JRadioButton							basicRadioButton, expertRadioButton;
	private final JCheckBox								useCreditsCheckbox;
	private final HashMap<Civilization.Name, CivPanel>	civPanels;
	private final GameClient							client;

	public NewGameDialog(final GameClient client, final GuiController controller) {
		super(controller);
		this.client = client;

		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 3.0;
		constraints.weighty = 0.0;

		final Properties props = controller.getProperties();
		float fontSize = Float.parseFloat(props.getProperty("NewGameDialog.Number.FontSize"));

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridheight = 1;
		final JLabel label = new JLabel("Number of civilizations:");
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		constraints.weightx = 0.0;
		constraints.gridx = 1;
		constraints.gridy = 0;
		this.nCivSpinner = new JSpinner(new SpinnerNumberModel(5, 5, 18, 1));
		this.nCivSpinner.setFont(this.nCivSpinner.getFont().deriveFont(fontSize));
		this.nCivSpinner.addChangeListener(this);
		this.add(this.nCivSpinner, constraints);
		constraints.gridheight = 1;

		constraints.gridx = 0;
		constraints.gridy = 1;
		this.useCreditsCheckbox = new JCheckBox("Use start game credits");
		this.useCreditsCheckbox.setSelected(true);
		this.add(this.useCreditsCheckbox, constraints);

		fontSize = Float.parseFloat(props.getProperty("NewGameDialog.Option.FontSize"));
		final ButtonGroup regionGroup = new ButtonGroup();

		constraints.gridx = 3;
		constraints.gridy = 1;
		this.westRadioButton = new JRadioButton("West");
		this.westRadioButton.setSelected(true);
		this.westRadioButton.setActionCommand("West");
		this.westRadioButton.addActionListener(this);
		this.westRadioButton.setFont(this.westRadioButton.getFont().deriveFont(fontSize));
		this.add(this.westRadioButton, constraints);

		constraints.gridx = 4;
		constraints.gridy = 1;
		this.eastRadioButton = new JRadioButton("East");
		this.eastRadioButton.setActionCommand("East");
		this.eastRadioButton.addActionListener(this);
		this.eastRadioButton.setFont(this.eastRadioButton.getFont().deriveFont(fontSize));
		this.add(this.eastRadioButton, constraints);

		regionGroup.add(this.eastRadioButton);
		regionGroup.add(this.westRadioButton);

		final ButtonGroup difficultyGroup = new ButtonGroup();

		constraints.weightx = 1.0;
		constraints.gridx = 3;
		constraints.gridy = 0;
		this.basicRadioButton = new JRadioButton("Basic");
		this.basicRadioButton.setActionCommand("Basic");
		this.basicRadioButton.setSelected(true);
		this.basicRadioButton.addActionListener(this);
		this.basicRadioButton.setFont(this.basicRadioButton.getFont().deriveFont(fontSize));
		this.add(this.basicRadioButton, constraints);

		constraints.gridx = 4;
		constraints.gridy = 0;
		this.expertRadioButton = new JRadioButton("Expert");
		this.expertRadioButton.setActionCommand("Expert");
		this.expertRadioButton.addActionListener(this);
		this.expertRadioButton.setFont(this.expertRadioButton.getFont().deriveFont(fontSize));
		this.add(this.expertRadioButton, constraints);

		difficultyGroup.add(this.basicRadioButton);
		difficultyGroup.add(this.expertRadioButton);

		fontSize = Float.parseFloat(props.getProperty("NewGameDialog.Button.FontSize"));

		constraints.gridx = 5;
		constraints.gridy = 0;
		this.randomizeButton = new JButton("Randomize Players");
		this.randomizeButton.setActionCommand("Randomize");
		this.randomizeButton.addActionListener(this);
		this.randomizeButton.setFont(this.randomizeButton.getFont().deriveFont(fontSize));
		this.add(this.randomizeButton, constraints);

		constraints.gridx = 5;
		constraints.gridy = 1;
		this.customButton = new JToggleButton("Custom Setup");
		this.customButton.setActionCommand("Custom");
		this.customButton.addActionListener(this);
		this.customButton.setFont(this.customButton.getFont().deriveFont(fontSize));
		this.add(this.customButton, constraints);

		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 6;
		final JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
		separator.setPreferredSize(new Dimension(10, 10));
		this.add(separator, constraints);
		constraints.gridwidth = 1;

		constraints.gridwidth = 3;
		constraints.weightx = 0.5;
		constraints.weighty = 1.0;
		this.civPanels = new HashMap<Civilization.Name, CivPanel>();
		for (final Civilization.Name name : EnumSet.allOf(Civilization.Name.class)) {
			constraints.gridx = name.ordinal() % 2 * constraints.gridwidth;
			constraints.gridy = 4 + name.ordinal() / 2;
			final CivPanel panel = new CivPanel(controller, name);
			this.civPanels.put(name, panel);
			this.add(panel, constraints);
		}

		this.setDefaultCivs();

		this.dialog = new BubbaDialog(this.controller, "New Game Setup", this, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		this.dialog.setModal(true);
		this.dialog.setVisible(true);
	}


	public Region getRegion() {
		Region region = null;

		if (this.eastRadioButton.isEnabled()) {
			if (this.westRadioButton.isEnabled()) {
				region = this.eastRadioButton.isSelected() ? Region.EAST : Region.WEST;
			} else {
				this.eastRadioButton.setSelected(true);
				this.westRadioButton.setSelected(false);
				region = Region.EAST;
			}
		} else {
			if (this.westRadioButton.isEnabled()) {
				this.eastRadioButton.setSelected(false);
				this.westRadioButton.setSelected(true);
				region = Region.WEST;
			} else {
				region = Region.BOTH;
			}
		}

		return region;
	}

	public void setDefaultCivs() {
		final int nCivs = (int) this.nCivSpinner.getValue();

		final int startCredits = Game.SMALL_GAME_CREDITS.get(nCivs);

		this.useCreditsCheckbox.setText("Use start game credits (" + startCredits + ")");
		this.useCreditsCheckbox.setEnabled(startCredits > 0);

		this.eastRadioButton.setEnabled(Game.DEFAULT_STARTING_CIVS.get(nCivs).get(Region.EAST) != null);
		this.westRadioButton.setEnabled(Game.DEFAULT_STARTING_CIVS.get(nCivs).get(Region.WEST) != null);

		final Region region = this.getRegion();

		final ArrayList<Civilization.Name> startingCivs = Game.DEFAULT_STARTING_CIVS.get(nCivs).get(region);

		for (final Civilization.Name name : EnumSet.allOf(Civilization.Name.class)) {
			final CivPanel panel = this.civPanels.get(name);
			panel.setSelected(startingCivs.contains(name));
		}
	}

	@Override
	public void actionPerformed(final ActionEvent event) {
		final String command = event.getActionCommand();
		switch (command) {
			case "East":
			case "West":
				this.setDefaultCivs();
				break;
			case "Custom":
				final boolean isCustom = ( (JToggleButton) event.getSource() ).isSelected();
				for (final Civilization.Name name : EnumSet.allOf(Civilization.Name.class)) {
					this.civPanels.get(name).setEnabled(isCustom);
				}
				this.nCivSpinner.setEnabled(!isCustom);
				this.eastRadioButton.setEnabled(!isCustom);
				this.westRadioButton.setEnabled(!isCustom);
				if (!isCustom) {
					this.setDefaultCivs();
				}
				break;
			case "Randomize":
				final ArrayList<String> players = new ArrayList<String>();
				for (final CivPanel panel : this.civPanels.values()) {
					if (panel.isSelected()) {
						players.add(panel.getPlayerName());
					}
				}
				Collections.shuffle(players);
				final Iterator<String> iterator = players.iterator();
				for (final CivPanel panel : this.civPanels.values()) {
					if (panel.isSelected()) {
						panel.setPlayerName(iterator.next());
					}
				}

		}
	}

	@Override
	public void stateChanged(final ChangeEvent event) {
		this.setDefaultCivs();
	}

	@Override
	public void windowClosed(final WindowEvent event) {
		super.windowClosed(event);

		// If the OK button was pressed, open the question
		final int option = ( (Integer) this.dialog.getValue() ).intValue();

		if (option == JOptionPane.OK_OPTION) {
			final HashMap<Civilization.Name, String> startingCivs = new HashMap<Civilization.Name, String>();
			for (final CivPanel panel : this.civPanels.values()) {
				if (panel.isSelected()) {
					final Civilization.Name name = panel.getCivName();
					startingCivs.put(name, panel.getPlayerName());
				}
			}
			final Difficulty difficulty = this.basicRadioButton.isSelected() ? Difficulty.BASIC : Difficulty.EXPERT;
			this.client.log("Starting new game with the following civilizations: " + startingCivs);
			this.client.sendMessage(new NewGameMessage(this.getRegion(), startingCivs, difficulty,
					this.useCreditsCheckbox.isSelected()));
		}
	}

	private class CivPanel extends BubbaPanel implements ActionListener {

		private static final long		serialVersionUID	= -6238439277458238770L;

		private final JCheckBox			checkbox;
		private final JTextField		textField;
		private final Civilization.Name	name;

		public CivPanel(final GuiController controller, final Civilization.Name name) {
			super(controller, new GridBagLayout());
			this.name = name;

			final Properties props = controller.getProperties();
			final int civHeight = Integer.parseInt(props.getProperty("NewGameDialog.Civ.Height"));
			final int civWidth = Integer.parseInt(props.getProperty("NewGameDialog.Civ.Width"));
			final float fontSize = Float.parseFloat(props.getProperty("NewGameDialog.Civ.FontSize"));

			final Color foreground = Game.FOREGROUND_COLORS.get(name);
			final Color background = Game.BACKGROUND_COLORS.get(name);

			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.WEST;
			constraints.weightx = 0.0;
			constraints.weighty = 0.0;

			constraints.gridx = 0;
			constraints.gridy = 0;
			this.checkbox = new JCheckBox(WordUtils.capitalizeFully(name.toString()));
			BubbaPanel.setButtonProperties(this.checkbox, civWidth, civHeight, foreground, background, fontSize);
			this.checkbox.addActionListener(this);
			this.add(this.checkbox, constraints);

			constraints.weightx = 1.0;
			constraints.gridx = 1;
			constraints.gridy = 0;
			this.textField = new JTextField(WordUtils.capitalizeFully(name.toString()) + " player", 20);
			this.add(this.textField, constraints);

			this.setEnabled(false);
		}

		public Civilization.Name getCivName() {
			return this.name;
		}

		public String getPlayerName() {
			return this.textField.getText();
		}

		public void setPlayerName(final String player) {
			this.textField.setText(player);
		}

		public void setSelected(final boolean selected) {
			this.checkbox.setSelected(selected);
			this.textField.setEnabled(selected);
		}

		@Override
		public void setEnabled(final boolean enabled) {
			this.checkbox.setEnabled(enabled);
		}

		public boolean isSelected() {
			return this.checkbox.isSelected();
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			this.setSelected(this.isSelected());
		}

	}

}
