package net.bubbaland.megaciv.client.gui;

import java.awt.GridBagConstraints;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import net.bubbaland.gui.BubbaDialog;
import net.bubbaland.gui.BubbaDialogPanel;
import net.bubbaland.gui.BubbaGuiController;
import net.bubbaland.gui.BubbaPanel;
import net.bubbaland.megaciv.client.messages.AdvanceAstMessage;
import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Game;

public class AdvanceAstDialog extends BubbaDialogPanel {

	private static final long			serialVersionUID	= -3521456258596945263L;

	private final GuiClient				client;
	private final ArrayList<CivPanel>	civPanels;

	private final int					N_ROWS				= 9;

	public AdvanceAstDialog(GuiClient client, BubbaGuiController controller) {
		super(controller);
		this.client = client;

		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;

		constraints.gridx = 0;
		constraints.gridy = 0;

		this.civPanels = new ArrayList<CivPanel>();

		constraints.weightx = 0.0;
		constraints.weighty = 0.0;

		for (Civilization.Name name : this.client.getGame().getCivilizationNames()) {
			constraints.gridx = name.ordinal() / N_ROWS;
			constraints.gridy = name.ordinal() % N_ROWS;

			CivPanel panel = new CivPanel(controller, name);
			this.civPanels.add(panel);
			this.add(panel, constraints);
		}
		this.setAutoAdvance();

		this.dialog = new BubbaDialog(this.controller, "Advance AST", this, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		this.dialog.setVisible(true);

	}

	private void setAutoAdvance() {
		for (CivPanel panel : this.civPanels) {
			Civilization.Name name = panel.getCivName();
			Civilization civ = this.client.getGame().getCivilization(name);
			panel.checkbox.setSelected(civ.passAstReqirements());
		}
	}

	public void windowClosed(WindowEvent event) {
		super.windowClosed(event);

		// If the OK button was pressed, open the question
		final int option = ( (Integer) this.dialog.getValue() ).intValue();

		if (option == JOptionPane.OK_OPTION) {
			HashMap<Civilization.Name, Boolean> advanceAst = new HashMap<Civilization.Name, Boolean>();
			for (CivPanel panel : this.civPanels) {
				advanceAst.put(panel.getCivName(), panel.checkbox.isSelected());
			}
			this.client.log("Sending AST advances to server " + advanceAst);
			this.client.sendMessage(new AdvanceAstMessage(advanceAst));
		}
	}

	private class CivPanel extends BubbaPanel {

		private static final long		serialVersionUID	= -487711727769927447L;

		private final JCheckBox			checkbox;
		private final JTextArea			textArea;

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
			this.checkbox = new JCheckBox(Game.capitalizeFirst(name.toString()));
			this.add(this.checkbox, constraints);

			Game game = AdvanceAstDialog.this.client.getGame();

			Civilization.Age nextAge = game.getCivilization(name).getNextStepAge();

			String astReqText = Civilization.AGE_REQUIREMENTS.get(game.getDifficulty()).get(nextAge).getText();

			constraints.gridx = 1;
			constraints.gridy = 0;
			this.textArea = new JTextArea(astReqText);
			this.textArea.setEditable(false);
			this.add(this.textArea, constraints);
		}

		public Civilization.Name getCivName() {
			return this.name;
		}
	}

}
