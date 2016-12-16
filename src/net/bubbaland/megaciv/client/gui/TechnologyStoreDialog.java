package net.bubbaland.megaciv.client.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.bubbaland.gui.BubbaGuiController;
import net.bubbaland.gui.BubbaPanel;
import net.bubbaland.megaciv.client.GameClient;
import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Technology.Type;
import net.bubbaland.megaciv.messages.AdditionalCreditMessage;
import net.bubbaland.megaciv.messages.TechPurchaseMessage;
import net.bubbaland.megaciv.game.Game;
import net.bubbaland.megaciv.game.Technology;

public class TechnologyStoreDialog extends BubbaPanel implements ActionListener, ChangeListener {

	private static final long						serialVersionUID	= 6388871064256668085L;

	private static final int						N_ROWS				= 17;

	private final JLabel							civLabel, sortLabel;
	private final JComboBox<String>					sortComboBox;
	private final ArrayList<TechnologyCheckBox>		techCheckboxes;
	private final GameClient						client;
	private final JFrame							frame;
	private final JButton							buyNextButton, resetButton;
	private final ArrayList<TechnologyTypeComboBox>	writtenRecordComboboxes, monumentComboboxes;
	private final JLabel							writtenRecordLabel, monumentLabel;
	private final Civilization.Name					civName;

	public TechnologyStoreDialog(GameClient client, BubbaGuiController controller, Civilization.Name civName) {
		super(controller, new GridBagLayout());
		this.frame = new JFrame();
		this.client = client;
		this.civName = civName;

		Game game = this.client.getGame();
		ArrayList<Civilization.Name> civNames = Civilization.sortByToName(game.getCivilizations(),
				Civilization.SortOption.AST, Civilization.SortDirection.ASCENDING);
		Civilization.Name[] civNameArray = new Civilization.Name[civNames.size()];
		civNameArray = civNames.toArray(civNameArray);

		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;

		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.gridwidth = 3;
		this.civLabel = this.enclosedLabelFactory(civName.toString(), constraints, JLabel.LEFT, JLabel.CENTER);

		constraints.gridwidth = 1;
		constraints.gridx = 4;
		this.sortLabel = this.enclosedLabelFactory("Sort by:", constraints, JLabel.RIGHT, JLabel.CENTER);

		constraints.gridx = 5;
		this.sortComboBox = new JComboBox<String>();
		this.sortComboBox.addItem("Alphabetical");
		this.sortComboBox.addItem("Base Cost");
		this.sortComboBox.addItem("Current Cost");
		this.sortComboBox.setActionCommand("Sort");
		this.sortComboBox.addActionListener(this);
		this.add(this.sortComboBox, constraints);

		constraints.gridwidth = 2;

		this.techCheckboxes = new ArrayList<TechnologyCheckBox>();

		for (int i = 0; i < EnumSet.allOf(Technology.class).size(); i++) {
			TechnologyCheckBox checkbox = new TechnologyCheckBox();
			checkbox.addChangeListener(this);
			constraints.gridx = ( 0 + i / N_ROWS ) * constraints.gridwidth;
			constraints.gridy = 1 + i % N_ROWS;

			this.techCheckboxes.add(checkbox);
			this.add(checkbox, constraints);
		}

		this.writtenRecordComboboxes = new ArrayList<TechnologyTypeComboBox>();
		constraints.gridx = 4;
		constraints.gridwidth = 2;
		constraints.gridy = N_ROWS + 2;
		this.writtenRecordLabel =
				this.enclosedLabelFactory("Written Record Credits", constraints, JLabel.CENTER, JLabel.CENTER);
		this.writtenRecordLabel.setEnabled(false);
		constraints.gridwidth = 1;

		constraints.gridy = N_ROWS + 3;
		for (int i = 0; i < 2; i++) {
			constraints.gridx = 4 + i;
			TechnologyTypeComboBox combobox = new TechnologyTypeComboBox(Technology.Type.values());
			combobox.setEnabled(false);
			this.add(combobox, constraints);
			this.writtenRecordComboboxes.add(combobox);
		}

		this.monumentComboboxes = new ArrayList<TechnologyTypeComboBox>();

		constraints.gridx = 2;
		constraints.gridy = N_ROWS + 2;
		constraints.gridwidth = 2;
		this.monumentLabel = this.enclosedLabelFactory("Monument Credits", constraints, JLabel.CENTER, JLabel.CENTER);
		this.monumentLabel.setEnabled(false);
		constraints.gridwidth = 1;

		for (int i = 0; i < 4; i++) {
			constraints.gridx = 2 + i / 2;
			constraints.gridy = N_ROWS + 3 + i % 2;
			TechnologyTypeComboBox combobox = new TechnologyTypeComboBox(Technology.Type.values());
			combobox.setEnabled(false);
			this.add(combobox, constraints);
			this.monumentComboboxes.add(combobox);
		}

		constraints.gridx = 4;
		constraints.gridy = 5 + N_ROWS;
		constraints.weighty = 1.0;
		constraints.gridwidth = 2;
		this.buyNextButton = new JButton("Buy");
		this.buyNextButton.setActionCommand("Buy");
		this.buyNextButton.addActionListener(this);
		this.add(this.buyNextButton, constraints);
		constraints.gridheight = 1;

		constraints.gridx = 1;
		constraints.gridy = 5 + N_ROWS;

		constraints.gridx = 0;
		constraints.gridy = 5 + N_ROWS;
		this.resetButton = new JButton("Reset");
		this.resetButton.setActionCommand("Reset");
		this.resetButton.addActionListener(this);
		this.add(this.resetButton, constraints);

		this.setCheckboxTechs();

		this.resetCheckboxes();

		this.updateTotalCost();

		this.loadProperties();

		this.frame.add(this);
		this.frame.setTitle("Purchase Technologies");
		this.frame.pack();
		this.frame.setResizable(false);
		this.frame.setVisible(true);
	}

	public void loadProperties() {
		Properties props = this.controller.getProperties();

		int colWidth = Integer.parseInt(props.getProperty("TechStoreDialog.Tech.Width"));
		int rowHeight = Integer.parseInt(props.getProperty("TechStoreDialog.Tech.Height"));
		float fontSize = Float.parseFloat(props.getProperty("TechStoreDialog.Tech.FontSize"));

		for (JCheckBox checkbox : this.techCheckboxes) {
			BubbaPanel.setButtonProperties(checkbox, colWidth, rowHeight, null, null, fontSize);
		}

		BubbaPanel.setLabelProperties(this.civLabel, Integer.parseInt(props.getProperty("TechStoreDialog.Civ.Width")),
				Integer.parseInt(props.getProperty("TechStoreDialog.Civ.Height")),
				Game.FOREGROUND_COLORS.get(this.civName), Game.BACKGROUND_COLORS.get(this.civName),
				Float.parseFloat(props.getProperty("TechStoreDialog.Civ.FontSize")));

		BubbaPanel.setLabelProperties(this.sortLabel, 0,
				Integer.parseInt(props.getProperty("TechStoreDialog.Civ.Height")),
				Game.FOREGROUND_COLORS.get(this.civName), Game.BACKGROUND_COLORS.get(this.civName),
				Float.parseFloat(props.getProperty("TechStoreDialog.Civ.FontSize")));

		BubbaPanel.setButtonProperties(this.buyNextButton,
				Integer.parseInt(props.getProperty("TechStoreDialog.BuyButton.Width")),
				Integer.parseInt(props.getProperty("TechStoreDialog.BuyButton.Height")), null, null,
				Float.parseFloat(props.getProperty("TechStoreDialog.BuyButton.FontSize")));

		BubbaPanel.setButtonProperties(this.resetButton,
				Integer.parseInt(props.getProperty("TechStoreDialog.ResetButton.Width")),
				Integer.parseInt(props.getProperty("TechStoreDialog.ResetButton.Height")), null, null,
				Float.parseFloat(props.getProperty("TechStoreDialog.ResetButton.FontSize")));
	}

	private void setCheckboxTechs() {
		Civilization civ = this.client.getGame().getCivilization(this.civName);
		ArrayList<Technology> ownedTechs = civ.getTechs();
		ArrayList<Technology> checkedTechs = new ArrayList<Technology>();
		ArrayList<Technology> allTechs = new ArrayList<Technology>(EnumSet.allOf(Technology.class));

		for (TechnologyCheckBox checkbox : this.techCheckboxes) {
			if (checkbox.isSelected()) {
				checkedTechs.add(checkbox.getTechnology());
			}
		}

		switch ((String) this.sortComboBox.getSelectedItem()) {
			case "Alphabetical":
				break;
			case "Base Cost":
				allTechs.sort(new Technology.techCostComparator());
				break;
			case "Current Cost":
				allTechs.sort(new Technology.techCostComparator(civ));
				break;
		}

		for (int i = 0; i < this.techCheckboxes.size(); i++) {
			TechnologyCheckBox checkbox = this.techCheckboxes.get(i);
			Technology tech = allTechs.get(i);
			checkbox.setTechnology(tech);

			boolean isOwned = ownedTechs.contains(tech);
			checkbox.setSelected(isOwned || checkedTechs.contains(tech));
			checkbox.setEnabled(!isOwned);
			String techString = "<html>" + tech.getName();
			if (!isOwned) {
				if (tech == Technology.LIBRARY || tech == Technology.ANATOMY) {
					techString = techString + " (" + civ.getCost(tech) + "*/" + tech.getBaseCost() + ") ";
				} else {
					techString = techString + " (" + civ.getCost(tech) + "/" + tech.getBaseCost() + ") ";
				}
				for (Type type : tech.getTypes()) {
					techString = techString + "<span color=\"" + type.getHtmlColor() + "\">•</span>";
				}
			}
			techString = techString + "</html>";
			checkbox.setText(techString);
		}
	}

	private void resetCheckboxes() {
		Civilization civ = this.client.getGame().getCivilization(this.civName);
		ArrayList<Technology> ownedTechs = civ.getTechs();
		for (TechnologyCheckBox checkbox : techCheckboxes) {
			Technology tech = checkbox.getTechnology();
			boolean isOwned = ownedTechs.contains(tech);
			checkbox.setSelected(isOwned);
			checkbox.setEnabled(!isOwned);
			String techString = "<html>" + tech.getName();
			if (!isOwned) {
				if (tech == Technology.LIBRARY || tech == Technology.ANATOMY) {
					techString = techString + " (" + civ.getCost(tech) + "*/" + tech.getBaseCost() + ") ";
				} else {
					techString = techString + " (" + civ.getCost(tech) + "/" + tech.getBaseCost() + ") ";
				}
				for (Type type : tech.getTypes()) {
					techString = techString + "<span color=\"" + type.getHtmlColor() + "\">•</span>";
				}
			}
			techString = techString + "</html>";
			checkbox.setText(techString);
		}

		ArrayList<Technology.Type> credits = civ.getTypeCredits(Technology.WRITTEN_RECORD);
		for (int i = 0; i < 2; i++) {
			this.writtenRecordComboboxes.get(i).setSelectedItem(credits.get(i));
		}
		credits = civ.getTypeCredits(Technology.MONUMENT);
		for (int i = 0; i < 4; i++) {
			this.monumentComboboxes.get(i).setSelectedItem(credits.get(i));
		}

		this.updateTotalCost();

	}

	private void updateTotalCost() {
		Civilization civ = this.client.getGame().getCivilization(this.civName);

		HashMap<Technology, Integer> costs = new HashMap<Technology, Integer>();
		for (TechnologyCheckBox checkbox : this.techCheckboxes) {
			Technology tech = checkbox.getTechnology();
			if (checkbox.isSelected() && checkbox.isEnabled()) {
				costs.put(tech, civ.getCost(tech));
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
			costs.put(Technology.ANATOMY, civ.getCost(Technology.ANATOMY));
		}

		if (costs.containsKey(Technology.LIBRARY)) {
			costs.remove(Technology.LIBRARY);
			Technology discountedTech = null;
			int discount = 0;
			for (Technology tech : costs.keySet()) {
				if (discountedTech == null || civ.getCost(tech) > discount) {
					discountedTech = tech;
					discount = Math.min(civ.getCost(discountedTech), 40);
				}
				if (discount == 40) {
					break;
				}
			}
			if (discountedTech != null) {
				costs.put(discountedTech, civ.getCost(discountedTech) - discount);
			}
			costs.put(Technology.LIBRARY, civ.getCost(Technology.LIBRARY));
		}

		for (TechnologyTypeComboBox combobox : this.writtenRecordComboboxes) {
			combobox.setEnabled(costs.containsKey(Technology.WRITTEN_RECORD));
		}

		for (TechnologyTypeComboBox combobox : this.monumentComboboxes) {
			combobox.setEnabled(costs.containsKey(Technology.MONUMENT));
		}

		int cost = 0;
		for (int c : costs.values()) {
			cost = cost + c;
		}
		this.buyNextButton.setText("Buy (" + String.format("%04d", cost) + ")");
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
				ArrayList<Technology> newTechs = new ArrayList<Technology>();
				for (TechnologyCheckBox checkbox : this.techCheckboxes) {
					Technology tech = checkbox.getTechnology();
					if (checkbox.isSelected() && checkbox.isEnabled()) {
						newTechs.add(tech);
					}
				}
				this.client.sendMessage(new TechPurchaseMessage(civName, newTechs));
				if (newTechs.contains(Technology.WRITTEN_RECORD)) {
					ArrayList<Technology.Type> credits = new ArrayList<Technology.Type>() {
						private static final long serialVersionUID = -3048516348338206499L;

						{
							for (TechnologyTypeComboBox combobox : TechnologyStoreDialog.this.writtenRecordComboboxes) {
								add((Type) combobox.getSelectedItem());
							}
						}
					};
					this.client
							.sendMessage(new AdditionalCreditMessage(this.civName, Technology.WRITTEN_RECORD, credits));
				}
				if (newTechs.contains(Technology.MONUMENT)) {
					ArrayList<Technology.Type> credits = new ArrayList<Technology.Type>() {
						private static final long serialVersionUID = -3048516348338206499L;

						{
							for (TechnologyTypeComboBox combobox : TechnologyStoreDialog.this.monumentComboboxes) {
								add((Type) combobox.getSelectedItem());
							}
						}
					};
					this.client.sendMessage(new AdditionalCreditMessage(this.civName, Technology.MONUMENT, credits));
				}
				this.frame.dispose();
			case "Reset":
				this.resetCheckboxes();
				break;
			case "Sort":
				this.setCheckboxTechs();
				break;
		}
	}

	private class TechnologyCheckBox extends JCheckBox {

		private static final long	serialVersionUID	= 7910433610896447288L;

		private Technology			tech				= null;

		public void setTechnology(Technology tech) {
			this.tech = tech;
			this.setToolTipText("<html><img src=\""
					+ CivInfoPanel.class.getResource("images/advances/" + tech.toString() + ".png") + "\"></html>");
		}

		public Technology getTechnology() {
			return this.tech;
		}

	}
}
