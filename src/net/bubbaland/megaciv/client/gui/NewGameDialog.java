package net.bubbaland.megaciv.client.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.bubbaland.gui.BubbaDialog;
import net.bubbaland.gui.BubbaDialogPanel;
import net.bubbaland.gui.BubbaPanel;
import net.bubbaland.megaciv.client.messages.NewGameMessage;
import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Game;
import net.bubbaland.megaciv.game.Civilization.Region;
import net.bubbaland.megaciv.game.Game.Difficulty;

public class NewGameDialog extends BubbaDialogPanel implements ActionListener, ChangeListener {

	private static final long							serialVersionUID	= -2854507573608809889L;

	private final JToggleButton							customButton;
	private final JSpinner								nCivSpinner;
	private final JRadioButton							eastRadioButton, westRadioButton;
	private final JRadioButton							basicRadioButton, expertRadioButton;
	private final HashMap<Civilization.Name, CivPanel>	civPanels;
	private final GuiClient								client;

	public NewGameDialog(GuiClient client, GuiController controller) {
		super(controller);
		this.client = client;

		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 3.0;
		constraints.weighty = 0.0;

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridheight = 2;
		JLabel label = new JLabel("Number of civilizations:");
		label.setFont(label.getFont().deriveFont(18.0f));
		this.add(label, constraints);

		constraints.weightx = 0.0;
		constraints.gridx = 1;
		constraints.gridy = 0;
		this.nCivSpinner = new JSpinner(new SpinnerNumberModel(5, 5, 18, 1));
		this.nCivSpinner.setFont(this.nCivSpinner.getFont().deriveFont(24.0f));
		this.nCivSpinner.addChangeListener(this);
		this.add(this.nCivSpinner, constraints);
		constraints.gridheight = 1;

		ButtonGroup regionGroup = new ButtonGroup();

		constraints.gridx = 3;
		constraints.gridy = 1;
		this.westRadioButton = new JRadioButton("West");
		this.westRadioButton.setSelected(true);
		this.westRadioButton.setActionCommand("West");
		this.westRadioButton.addActionListener(this);
		this.add(this.westRadioButton, constraints);

		constraints.gridx = 4;
		constraints.gridy = 1;
		this.eastRadioButton = new JRadioButton("East");
		this.eastRadioButton.setActionCommand("East");
		this.eastRadioButton.addActionListener(this);
		this.add(this.eastRadioButton, constraints);

		regionGroup.add(this.eastRadioButton);
		regionGroup.add(this.westRadioButton);

		ButtonGroup difficultyGroup = new ButtonGroup();

		constraints.weightx = 1.0;
		constraints.gridx = 3;
		constraints.gridy = 0;
		this.basicRadioButton = new JRadioButton("Basic");
		this.basicRadioButton.setActionCommand("Basic");
		this.basicRadioButton.setSelected(true);
		this.basicRadioButton.addActionListener(this);
		this.add(this.basicRadioButton, constraints);

		constraints.gridx = 4;
		constraints.gridy = 0;
		this.expertRadioButton = new JRadioButton("Expert");
		this.expertRadioButton.setActionCommand("Expert");
		this.expertRadioButton.addActionListener(this);
		this.add(this.expertRadioButton, constraints);

		difficultyGroup.add(this.basicRadioButton);
		difficultyGroup.add(this.expertRadioButton);

		constraints.gridx = 5;
		constraints.gridy = 1;
		this.customButton = new JToggleButton("Custom Setup");
		this.customButton.setActionCommand("Custom");
		this.customButton.addActionListener(this);
		this.add(this.customButton, constraints);

		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 6;
		JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
		separator.setPreferredSize(new Dimension(10, 10));
		this.add(separator, constraints);
		constraints.gridwidth = 1;

		constraints.gridwidth = 3;
		constraints.weightx = 0.5;
		constraints.weighty = 1.0;
		this.civPanels = new HashMap<Civilization.Name, CivPanel>();
		for (Civilization.Name name : EnumSet.allOf(Civilization.Name.class)) {
			constraints.gridx = name.ordinal() % 2 * constraints.gridwidth;
			constraints.gridy = 4 + name.ordinal() / 2;
			CivPanel panel = new CivPanel(controller, name);
			this.civPanels.put(name, panel);
			this.add(panel, constraints);
		}

		this.setDefaultCivs();

		this.dialog = new BubbaDialog(this.controller, "New Game Setup", this, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		this.dialog.setModal(true);
		this.dialog.setVisible(true);
	}

	public void setDefaultCivs() {
		int nCivs = (int) this.nCivSpinner.getValue();

		this.eastRadioButton.setEnabled(Civilization.DEFAULT_STARTING_CIVS.get(nCivs).get(Region.EAST) != null);
		this.westRadioButton.setEnabled(Civilization.DEFAULT_STARTING_CIVS.get(nCivs).get(Region.WEST) != null);

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

		ArrayList<Civilization.Name> startingCivs = Civilization.DEFAULT_STARTING_CIVS.get(nCivs).get(region);

		for (Civilization.Name name : EnumSet.allOf(Civilization.Name.class)) {
			CivPanel panel = this.civPanels.get(name);
			panel.setSelected(startingCivs.contains(name));
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();
		switch (command) {
			case "East":
			case "West":
				this.setDefaultCivs();
				break;
			case "Custom":
				boolean isCustom = ( (JToggleButton) event.getSource() ).isSelected();
				for (Civilization.Name name : EnumSet.allOf(Civilization.Name.class)) {
					this.civPanels.get(name).setEnabled(isCustom);
				}
				this.nCivSpinner.setEnabled(!isCustom);
				this.eastRadioButton.setEnabled(!isCustom);
				this.westRadioButton.setEnabled(!isCustom);
				if (!isCustom) {
					this.setDefaultCivs();
				}
				break;
		}
	}

	@Override
	public void stateChanged(ChangeEvent event) {
		this.setDefaultCivs();
	}

	public void windowClosed(WindowEvent event) {
		super.windowClosed(event);

		// If the OK button was pressed, open the question
		final int option = ( (Integer) this.dialog.getValue() ).intValue();

		if (option == JOptionPane.OK_OPTION) {
			HashMap<Civilization.Name, String> startingCivs = new HashMap<Civilization.Name, String>();
			for (CivPanel panel : this.civPanels.values()) {
				if (panel.isSelected()) {
					Civilization.Name name = panel.getCivName();
					startingCivs.put(name, panel.getPlayerName());
				}
			}
			Difficulty difficulty = this.basicRadioButton.isSelected() ? Difficulty.BASIC : Difficulty.EXPERT;

			this.client.log("Starting new game with the following civilizations: " + startingCivs);
			this.client.sendMessage(new NewGameMessage(startingCivs, difficulty));
		}
	}

	private class CivPanel extends BubbaPanel implements ActionListener {

		private static final long		serialVersionUID	= -6238439277458238770L;

		private final JCheckBox			checkbox;
		private final JTextField		textField;
		private final Civilization.Name	name;

		public CivPanel(GuiController controller, Civilization.Name name) {
			super(controller, new GridBagLayout());
			this.name = name;

			Properties props = controller.getProperties();
			int civHeight = Integer.parseInt(props.getProperty("CensusDialog.Civ.Height"));
			int civWidth = Integer.parseInt(props.getProperty("CensusDialog.Civ.Width"));
			float fontSize = Float.parseFloat(props.getProperty("CensusDialog.FontSize"));

			Color foreground = Civilization.FOREGROUND_COLORS.get(name);
			Color background = Civilization.BACKGROUND_COLORS.get(name);
			foreground = null;
			background = null;

			this.setBackground(background);

			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.WEST;
			constraints.weightx = 0.0;
			constraints.weighty = 0.0;

			constraints.gridx = 0;
			constraints.gridy = 0;
			this.checkbox = new JCheckBox(Game.capitalizeFirst(name.toString()));
			BubbaPanel.setButtonProperties(this.checkbox, civWidth, civHeight, foreground, background, fontSize);
			this.checkbox.addActionListener(this);
			this.add(this.checkbox, constraints);

			constraints.weightx = 1.0;
			constraints.gridx = 1;
			constraints.gridy = 0;
			this.textField = new JTextField(Game.capitalizeFirst(name.toString()) + " player", 20);
			this.textField.setForeground(foreground);
			this.textField.setBackground(background);
			this.add(this.textField, constraints);

			this.setEnabled(false);
		}

		public Civilization.Name getCivName() {
			return this.name;
		}

		public String getPlayerName() {
			return this.textField.getText();
		}

		public void setSelected(boolean selected) {
			this.checkbox.setSelected(selected);
			this.textField.setEnabled(selected);
		}

		public void setEnabled(boolean enabled) {
			this.checkbox.setEnabled(enabled);
		}

		public boolean isSelected() {
			return this.checkbox.isSelected();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			this.setSelected(this.isSelected());
		}

	}

}
