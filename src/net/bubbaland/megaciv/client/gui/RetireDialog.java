package net.bubbaland.megaciv.client.gui;

import java.awt.GridBagConstraints;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import net.bubbaland.gui.BubbaDialog;
import net.bubbaland.gui.BubbaDialogPanel;
import net.bubbaland.gui.BubbaGuiController;
import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Civilization.Name;
import net.bubbaland.megaciv.game.Game;
import net.bubbaland.megaciv.messages.RetireMessage;

public class RetireDialog extends BubbaDialogPanel {

	private static final long			serialVersionUID	= 2699726022812408819L;

	private final GuiClient				client;
	private final CivilizationComboBox	civComboBox;


	RetireDialog(GuiClient client, GuiController contoller) {
		super(contoller);
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
		this.enclosedLabelFactory("Choose civilization that is retiring", constraints, JLabel.CENTER, JLabel.CENTER);

		constraints.gridx = 0;
		constraints.gridy = 1;
		this.civComboBox = new CivilizationComboBox(civNameArray);
		this.add(this.civComboBox, constraints);

		this.dialog = new BubbaDialog(this.controller, "Retire", this, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		this.dialog.setVisible(true);
	}

	public void windowClosed(WindowEvent event) {
		super.windowClosed(event);

		// If the OK button was pressed, open the question
		final int option = ( (Integer) this.dialog.getValue() ).intValue();

		if (option == JOptionPane.OK_OPTION) {
			Civilization.Name name = (Name) this.civComboBox.getSelectedItem();
			new ConfirmRetireDialog(this.client, this.controller, name);
		}
	}

	private class ConfirmRetireDialog extends BubbaDialogPanel {

		private static final long		serialVersionUID	= 4075309902192150187L;

		private final GuiClient			client;
		private final Civilization.Name	name;

		public ConfirmRetireDialog(GuiClient client, BubbaGuiController controller, Civilization.Name name) {
			super(controller);
			this.client = client;
			this.name = name;

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
			this.enclosedLabelFactory("WARNING: Retiring can not be reversed! " + name.toString()
					+ " will be permanently removed from the game!", constraints, JLabel.CENTER, JLabel.CENTER);

			this.dialog = new BubbaDialog(this.controller, "Confirm retirement of " + name.toString(), this,
					JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
			this.dialog.setVisible(true);
		}

		public void windowClosed(WindowEvent event) {
			super.windowClosed(event);

			// If the OK button was pressed, open the question
			final int option = ( (Integer) this.dialog.getValue() ).intValue();

			if (option == JOptionPane.OK_OPTION) {
				this.client.log(name.toString() + " has retired!");
				new RetireMessage(this.name);
			}
		}

	}


}
