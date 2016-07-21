package net.bubbaland.megaciv.client.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import net.bubbaland.gui.BubbaDialog;
import net.bubbaland.gui.BubbaDialogPanel;
import net.bubbaland.gui.BubbaGuiController;
import net.bubbaland.gui.BubbaPanel;
import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Civilization.Age;
import net.bubbaland.megaciv.game.Game;
import net.bubbaland.megaciv.messages.AdvanceAstMessage;

public class AdvanceAstDialog extends BubbaDialogPanel {

	private static final long			serialVersionUID	= -3521456258596945263L;

	private final GuiClient				client;
	private final ArrayList<CivPanel>	civPanels;

	private final static int			N_COLUMNS			= 2;

	public AdvanceAstDialog(GuiClient client, BubbaGuiController controller) {
		super(controller);
		this.client = client;

		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;

		this.civPanels = new ArrayList<CivPanel>();


		ArrayList<Civilization> civs = this.client.getGame().getCivilizations();
		for (Civilization civ : civs) {
			constraints.gridx = civ.getName().ordinal() % N_COLUMNS;
			constraints.gridy = civ.getName().ordinal() / N_COLUMNS;
			CivPanel panel = new CivPanel(controller, civ);
			this.civPanels.add(panel);
			this.add(panel, constraints);
		}
		this.dialog = new BubbaDialog(this.controller, "Advance AST", this, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		this.dialog.setVisible(true);

	}

	public void windowClosed(WindowEvent event) {
		super.windowClosed(event);

		// If the OK button was pressed, open the question
		final int option = ( (Integer) this.dialog.getValue() ).intValue();

		if (option == JOptionPane.OK_OPTION) {
			HashMap<Civilization.Name, Civilization.AstChange> advanceAst = new HashMap<Civilization.Name, Civilization.AstChange>();
			for (CivPanel panel : this.civPanels) {
				advanceAst.put(panel.getCivName(), panel.getAstChange());
			}
			this.client.log("Sending AST advances to server " + advanceAst);
			this.client.sendMessage(new AdvanceAstMessage(advanceAst));
		}
	}

	private class CivPanel extends BubbaPanel {

		private static final long		serialVersionUID	= -487711727769927447L;

		private final JCheckBox			checkbox, regressCheckbox;
		private final JTextPane			textArea;

		private final Civilization.Name	name;

		public CivPanel(BubbaGuiController controller, Civilization civ) {
			super(controller, new GridBagLayout());
			this.name = civ.getName();

			Properties props = controller.getProperties();
			int civHeight = Integer.parseInt(props.getProperty("AdvanceAstDialog.Civ.Height"));
			int civWidth = Integer.parseInt(props.getProperty("AdvanceAstDialog.Civ.Width"));
			float civFontSize = Float.parseFloat(props.getProperty("AdvanceAstDialog.Civ.FontSize"));

			int regressHeight = Integer.parseInt(props.getProperty("AdvanceAstDialog.Regress.Height"));
			float regressFontSize = Float.parseFloat(props.getProperty("AdvanceAstDialog.Regress.FontSize"));

			int reqHeight = Integer.parseInt(props.getProperty("AdvanceAstDialog.Req.Height"));
			int reqWidth = Integer.parseInt(props.getProperty("AdvanceAstDialog.Req.Width"));
			float reqFontSize = Float.parseFloat(props.getProperty("AdvanceAstDialog.Req.FontSize"));

			Color foreground = Civilization.FOREGROUND_COLORS.get(name);
			Color background = Civilization.BACKGROUND_COLORS.get(name);

			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.NORTHWEST;
			constraints.weightx = 0.0;
			constraints.weighty = 0.0;

			ButtonGroup group = new ButtonGroup() {
				private static final long serialVersionUID = 8206407025138465937L;

				@Override
				public void setSelected(ButtonModel model, boolean selected) {
					if (selected) {
						super.setSelected(model, selected);
					} else {
						if (this.getSelection() == model) {
							clearSelection();
						}
					}
				}
			};

			constraints.gridx = 0;
			constraints.gridy = 0;
			this.checkbox = new JCheckBox(Game.capitalizeFirst(name.toString()));
			group.add(this.checkbox);
			BubbaPanel.setButtonProperties(this.checkbox, civWidth, civHeight, foreground, background, civFontSize);
			this.add(this.checkbox, constraints);

			constraints.gridx = 0;
			constraints.gridy = 2;
			this.regressCheckbox = new JCheckBox("Regress");
			group.add(this.regressCheckbox);
			BubbaPanel.setButtonProperties(this.regressCheckbox, civWidth, regressHeight, foreground, background,
					regressFontSize);
			this.add(this.regressCheckbox, constraints);

			this.checkbox.setSelected(civ.passAstReqirements());
			this.regressCheckbox.setSelected(civ.getCurrentAge() != Age.STONE && civ.getCityCount() == 0);

			Game game = AdvanceAstDialog.this.client.getGame();
			Civilization.Age nextAge = game.getCivilization(name).getNextStepAge();

			String astReqText = Civilization.AGE_REQUIREMENTS.get(game.getDifficulty()).get(nextAge).getText();

			constraints.weightx = 1.0;
			constraints.weighty = 1.0;
			constraints.gridx = 1;
			constraints.gridy = 0;
			constraints.gridheight = 3;
			this.textArea = this.scrollableTextPaneFactory(astReqText, reqWidth, reqHeight, foreground, background,
					constraints, reqFontSize, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			this.textArea.setText(astReqText);
			this.textArea.setEditable(false);


		}

		public Civilization.Name getCivName() {
			return this.name;
		}

		public Civilization.AstChange getAstChange() {
			Civilization.AstChange change = Civilization.AstChange.NONE;
			if (this.checkbox.isSelected()) {
				change = Civilization.AstChange.ADVANCE;
			} else if (this.regressCheckbox.isSelected()) {
				change = Civilization.AstChange.REGRESS;
			}
			return change;
		}

	}

}
