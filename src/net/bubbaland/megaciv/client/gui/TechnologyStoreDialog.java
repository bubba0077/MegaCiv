package net.bubbaland.megaciv.client.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.bubbaland.gui.BubbaGuiController;
import net.bubbaland.gui.BubbaPanel;
import net.bubbaland.megaciv.client.messages.TechPurchaseMessage;
import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Civilization.Name;
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
		ArrayList<Civilization.Name> civNames = game.getCivilizationNames();
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

		for (Technology tech : Technology.values()) {
			JCheckBox checkbox = new JCheckBox(Game.capitalizeFirst(tech.toString()));
			checkbox.addChangeListener(this);

			// ArrayList<Color> colors = tech.getColors();
			// checkbox.setBackground(colors.get(0));

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

		this.frame.add(this);
		this.frame.pack();
		this.frame.setVisible(true);
	}

	private void resetCheckboxes() {
		Civilization civ = this.client.getGame().getCivilization((Name) this.civComboBox.getSelectedItem());
		ArrayList<Technology> ownedTechs = civ.getTechs();
		for (Technology tech : Technology.values()) {
			JCheckBox checkbox = this.techCheckboxes.get(tech);
			boolean isOwned = ownedTechs.contains(tech);
			checkbox.setSelected(isOwned);
			checkbox.setEnabled(!isOwned);
			String techString = "<html>" + Game.capitalizeFirst(tech.toString());
			if (!isOwned) {
				techString = techString + " (" + tech.getCost(civ) + ") ";
				for (String color : tech.getColors()) {
					techString = techString + "<span color=\"" + color + "\">•</span>";
				}
			}
			techString = techString + "</html>";
			checkbox.setText(techString);
		}
	}

	private void updateTotalCost() {
		Civilization civ = this.client.getGame().getCivilization((Name) this.civComboBox.getSelectedItem());
		int cost = 0;
		for (Technology tech : Technology.values()) {
			JCheckBox checkbox = this.techCheckboxes.get(tech);
			if (checkbox.isSelected() && checkbox.isEnabled()) {
				cost = cost + tech.getCost(civ);
			}
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
				for (Technology tech : Technology.values()) {
					JCheckBox checkbox = this.techCheckboxes.get(tech);
					if (checkbox.isSelected() && checkbox.isEnabled()) {
						newTechs.add(tech);
					}
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
}
