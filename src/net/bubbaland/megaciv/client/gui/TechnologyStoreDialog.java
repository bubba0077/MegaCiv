package net.bubbaland.megaciv.client.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.bubbaland.gui.BubbaDialog;
import net.bubbaland.gui.BubbaDialogPanel;
import net.bubbaland.gui.BubbaGuiController;
import net.bubbaland.gui.BubbaPanel;
import net.bubbaland.megaciv.client.messages.AdditionalCreditMessage;
import net.bubbaland.megaciv.client.messages.TechPurchaseMessage;
import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Civilization.Name;
import net.bubbaland.megaciv.game.Technology.Type;
import net.bubbaland.megaciv.game.Game;
import net.bubbaland.megaciv.game.Technology;

public class TechnologyStoreDialog extends BubbaPanel implements ActionListener, ChangeListener {

	private static final long						serialVersionUID	= 6388871064256668085L;

	private static final int						N_ROWS				= 13;

	private final JComboBox<Civilization.Name>		civComboBox;
	private final HashMap<Technology, JCheckBox>	techCheckboxes;
	private final GuiClient							client;
	private final JFrame							frame;
	private final JButton							buyNextButton, nextButton, resetButton;

	public TechnologyStoreDialog(GuiClient client, BubbaGuiController controller) {
		super(controller, new GridBagLayout());
		this.frame = new JFrame();
		this.client = client;

		Game game = this.client.getGame();
		ArrayList<Civilization.Name> civNames = Civilization.sortByToName(game.getCivilizations(),
				Civilization.SortOption.AST, Civilization.SortDirection.ASCENDING);
		Civilization.Name[] civNameArray = new Civilization.Name[civNames.size()];
		civNameArray = civNames.toArray(civNameArray);

		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;

		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.gridx = 0;
		constraints.gridy = 0;
		this.civComboBox = new JComboBox<Civilization.Name>(civNameArray);
		this.civComboBox.setActionCommand("Civ Changed");
		this.civComboBox.addActionListener(this);
		this.add(this.civComboBox, constraints);
		constraints.gridwidth = 1;

		constraints.weighty = 0.0;

		this.techCheckboxes = new HashMap<Technology, JCheckBox>();

		for (Technology tech : EnumSet.allOf(Technology.class)) {
			JCheckBox checkbox = new JCheckBox(Game.capitalizeFirst(tech.toString()));
			checkbox.addChangeListener(this);

			constraints.gridx = 0 + tech.ordinal() / N_ROWS;
			constraints.gridy = 1 + tech.ordinal() % N_ROWS;

			this.techCheckboxes.put(tech, checkbox);
			this.add(checkbox, constraints);
		}

		constraints.gridx = 3;
		constraints.gridy = 0;
		this.buyNextButton = new JButton("Buy");
		this.buyNextButton.setActionCommand("Buy");
		this.buyNextButton.addActionListener(this);
		this.add(this.buyNextButton, constraints);

		constraints.gridx = 1;
		constraints.gridy = 2 + N_ROWS;

		this.nextButton = new JButton("Next");
		this.nextButton.setActionCommand("Next");
		this.nextButton.addActionListener(this);
		this.add(this.nextButton, constraints);

		constraints.gridx = 0;
		constraints.gridy = 2 + N_ROWS;
		this.resetButton = new JButton("Reset");
		this.resetButton.setActionCommand("Reset");
		this.resetButton.addActionListener(this);
		this.add(this.resetButton, constraints);

		resetCheckboxes();

		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.gridx = 0;
		constraints.gridy = 0;

		this.updateTotalCost();

		this.loadProperties();

		this.frame.add(this);
		this.frame.pack();
		this.frame.setResizable(false);
		this.frame.setVisible(true);
	}

	public void loadProperties() {
		Properties props = this.controller.getProperties();


	}

	private void resetCheckboxes() {
		Civilization civ = this.client.getGame().getCivilization((Name) this.civComboBox.getSelectedItem());
		ArrayList<Technology> ownedTechs = civ.getTechs();
		for (Technology tech : EnumSet.allOf(Technology.class)) {
			JCheckBox checkbox = this.techCheckboxes.get(tech);
			boolean isOwned = ownedTechs.contains(tech);
			checkbox.setSelected(isOwned);
			checkbox.setEnabled(!isOwned);
			String techString = "<html>" + Game.capitalizeFirst(tech.toString());
			if (!isOwned) {
				if (tech == Technology.LIBRARY || tech == Technology.ANATOMY) {
					techString = techString + " (" + tech.getCost(civ) + "*) ";
				} else {
					techString = techString + " (" + tech.getCost(civ) + ") ";
				}
				for (Type type : tech.getTypes()) {
					techString = techString + "<span color=\"" + type.getHtmlColor() + "\">•</span>";
				}
			}
			techString = techString + "</html>";
			checkbox.setText(techString);
		}
	}

	private void updateTotalCost() {
		Civilization civ = this.client.getGame().getCivilization((Name) this.civComboBox.getSelectedItem());

		HashMap<Technology, Integer> costs = new HashMap<Technology, Integer>();
		for (Technology tech : EnumSet.allOf(Technology.class)) {
			JCheckBox checkbox = this.techCheckboxes.get(tech);
			if (checkbox.isSelected() && checkbox.isEnabled()) {
				costs.put(tech, tech.getCost(civ));
			}
		}

		if (costs.containsKey(Technology.ANATOMY)) {
			costs.remove(Technology.ANATOMY);
			Technology freeTech = null;
			for (Technology tech : costs.keySet()) {
				if (tech.getTypes().contains(Type.SCIENCE) && tech.getBaseCost() < 100
						&& ( freeTech == null || tech.getBaseCost() > freeTech.getBaseCost() )) {
					freeTech = tech;
				}
			}
			if (freeTech != null) {
				costs.remove(freeTech);
			}
			costs.put(Technology.ANATOMY, Technology.ANATOMY.getCost(civ));
		}

		if (costs.containsKey(Technology.LIBRARY)) {
			costs.remove(Technology.LIBRARY);
			Technology discountedTech = null;
			int discount = 0;
			for (Technology tech : costs.keySet()) {
				if (discountedTech == null || tech.getCost(civ) > discount) {
					discountedTech = tech;
					discount = Math.min(discountedTech.getCost(civ), 40);
				}
				if (discount == 40) {
					break;
				}
			}
			if (discountedTech != null) {
				costs.put(discountedTech, discountedTech.getCost(civ) - discount);
			}
			costs.put(Technology.LIBRARY, Technology.LIBRARY.getCost(civ));
		}

		int cost = 0;
		for (int c : costs.values()) {
			cost = cost + c;
		}
		this.buyNextButton.setText("Buy   ( " + String.format("%04d", cost) + " )");
		if (cost > 0) {
			this.buyNextButton.setForeground(new Color(new BigInteger("00b300", 16).intValue()));
		} else {
			this.buyNextButton.setForeground(null);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		this.updateTotalCost();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		switch (command) {
			case "Buy":
				Civilization.Name civName = (Name) this.civComboBox.getSelectedItem();
				ArrayList<Technology> newTechs = new ArrayList<Technology>();
				for (Technology tech : EnumSet.allOf(Technology.class)) {
					JCheckBox checkbox = this.techCheckboxes.get(tech);
					if (checkbox.isSelected() && checkbox.isEnabled()) {
						newTechs.add(tech);
					}
				}
				if (newTechs.contains(Technology.WRITTEN_RECORD)) {
					new AdditionalCreditDialog(controller, civName, Technology.WRITTEN_RECORD.toString(), 2);
				}
				if (newTechs.contains(Technology.MONUMENT)) {
					new AdditionalCreditDialog(controller, civName, Technology.MONUMENT.toString(), 4);
				}
				this.client.sendMessage(new TechPurchaseMessage(civName, newTechs));
			case "Next":
				int nextIndex = this.civComboBox.getSelectedIndex() + 1;
				if (nextIndex == this.civComboBox.getItemCount()) {
					this.frame.dispose();
					return;
				}
				this.civComboBox.setSelectedIndex(nextIndex);
			case "Civ Changed":
			case "Reset":
				this.resetCheckboxes();
		}
	}

	private class AdditionalCreditDialog extends BubbaDialogPanel {

		private static final long			serialVersionUID	= 1L;

		private final String				techName;
		private final Civilization.Name		civName;
		private final ArrayList<CreditRow>	creditRows;

		public AdditionalCreditDialog(BubbaGuiController controller, Civilization.Name civName, String techName,
				int nCreditsIn5s) {
			super(controller);

			this.civName = civName;
			this.techName = techName;

			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.CENTER;

			constraints.weightx = 1.0;
			constraints.weighty = 1.0;
			constraints.gridx = 0;

			this.creditRows = new ArrayList<CreditRow>();
			for (int y = 0; y < nCreditsIn5s; y++) {
				constraints.gridy = y;
				CreditRow row = new CreditRow(controller);
				this.creditRows.add(row);
				this.add(row, constraints);
			}

			this.dialog = new BubbaDialog(this.controller,
					"Select Additional Credits for " + Game.capitalizeFirst(techName), this, JOptionPane.PLAIN_MESSAGE);
			this.dialog.setModal(true);
			this.dialog.setVisible(true);
		}

		public void windowClosed(WindowEvent event) {
			HashMap<Type, Integer> credits = new HashMap<Type, Integer>();
			for (Type type : EnumSet.allOf(Type.class)) {
				credits.put(type, 0);
			}
			for (CreditRow row : this.creditRows) {
				Type type = row.getSelectedType();
				credits.put(type, credits.get(type) + 5);
			}
			TechnologyStoreDialog.this.client.sendMessage(new AdditionalCreditMessage(this.civName, credits));
			TechnologyStoreDialog.this.client.log("Selected additional credits for " + this.techName + ": " + credits);
		}

		private class CreditRow extends BubbaPanel {

			private static final long					serialVersionUID	= 1L;

			private final HashMap<JRadioButton, Type>	buttons;

			public CreditRow(BubbaGuiController controller) {
				super(controller, new GridBagLayout());
				final GridBagConstraints constraints = new GridBagConstraints();
				constraints.fill = GridBagConstraints.BOTH;
				constraints.anchor = GridBagConstraints.CENTER;

				constraints.weightx = 1.0;
				constraints.weighty = 1.0;
				constraints.gridy = 0;

				ButtonGroup group = new ButtonGroup();
				this.buttons = new HashMap<JRadioButton, Type>();
				for (Type type : EnumSet.allOf(Type.class)) {
					constraints.gridx = type.ordinal();
					JRadioButton button = new JRadioButton(Game.capitalizeFirst(type.toString()));
					group.add(button);
					this.buttons.put(button, type);
					this.add(button, constraints);
				}
			}

			public Type getSelectedType() {
				for (JRadioButton button : this.buttons.keySet()) {
					if (button.isSelected()) {
						return this.buttons.get(button);
					}
				}
				return null;
			}

		}


	}
}
