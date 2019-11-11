package net.bubbaland.megaciv.client.gui;

import java.awt.GridBagConstraints;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import net.bubbaland.gui.BubbaDialog;
import net.bubbaland.gui.BubbaDialogPanel;
import net.bubbaland.gui.BubbaGuiController;
import net.bubbaland.megaciv.client.GameClient;
import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Civilization.Name;
import net.bubbaland.megaciv.game.Game;
import net.bubbaland.megaciv.messages.RetireMessage;

public class RetireDialog extends BubbaDialogPanel {

	private static final long			serialVersionUID	= 2699726022812408819L;

	private final GameClient			client;
	private final CivilizationComboBox	civComboBox;


	RetireDialog(final GameClient client, final GuiController contoller) {
		super(contoller);
		this.client = client;

		final Game game = this.client.getGame();
		final ArrayList<Civilization.Name> civNames = Civilization.sortByToName(game.getCivilizations(),
				Civilization.SortOption.AST, Civilization.SortDirection.DESCENDING);
		Civilization.Name[] civNameArray = new Civilization.Name[civNames.size()];
		civNameArray = civNames.toArray(civNameArray);

		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;

		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.gridx = 0;
		constraints.gridy = 0;
		this.enclosedLabelFactory("Choose civilization that is retiring", constraints, SwingConstants.CENTER,
				SwingConstants.CENTER);

		constraints.gridx = 0;
		constraints.gridy = 1;
		this.civComboBox = new CivilizationComboBox(civNameArray);
		this.add(this.civComboBox, constraints);

		this.dialog = new BubbaDialog(this.controller, "Retire", this, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		this.dialog.setVisible(true);
	}

	@Override
	public void windowClosed(final WindowEvent event) {
		super.windowClosed(event);

		// If the OK button was pressed, open the question
		final int option = ( (Integer) this.dialog.getValue() ).intValue();

		if (option == JOptionPane.OK_OPTION) {
			final Civilization.Name name = (Name) this.civComboBox.getSelectedItem();
			new ConfirmRetireDialog(this.client, this.controller, name);
		}
	}

	private class ConfirmRetireDialog extends BubbaDialogPanel {

		private static final long		serialVersionUID	= 4075309902192150187L;

		private final GameClient		client;
		private final Civilization.Name	name;

		public ConfirmRetireDialog(final GameClient client, final BubbaGuiController controller,
				final Civilization.Name name) {
			super(controller);
			this.client = client;
			this.name = name;

			final Game game = this.client.getGame();
			final ArrayList<Civilization.Name> civNames = Civilization.sortByToName(game.getCivilizations(),
					Civilization.SortOption.AST, Civilization.SortDirection.DESCENDING);
			Civilization.Name[] civNameArray = new Civilization.Name[civNames.size()];
			civNameArray = civNames.toArray(civNameArray);

			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.CENTER;

			constraints.weightx = 1.0;
			constraints.weighty = 1.0;
			constraints.gridx = 0;
			constraints.gridy = 0;
			this.enclosedLabelFactory(
					"WARNING: Retiring can not be reversed! " + name.toString()
							+ " will be permanently removed from the game!",
					constraints, SwingConstants.CENTER, SwingConstants.CENTER);

			this.dialog = new BubbaDialog(this.controller, "Confirm retirement of " + name.toString(), this,
					JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
			this.dialog.setVisible(true);
		}

		@Override
		public void windowClosed(final WindowEvent event) {
			super.windowClosed(event);

			// If the OK button was pressed, open the question
			final int option = ( (Integer) this.dialog.getValue() ).intValue();

			if (option == JOptionPane.OK_OPTION) {
				this.client.log(this.name.toString() + " has retired!");
				this.client.sendMessage(new RetireMessage(this.name));
			}
		}

	}


}
