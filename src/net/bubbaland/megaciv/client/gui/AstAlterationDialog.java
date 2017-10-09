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
import javax.swing.BorderFactory;
import org.apache.commons.lang3.text.WordUtils;

import net.bubbaland.gui.BubbaDialog;
import net.bubbaland.gui.BubbaDialogPanel;
import net.bubbaland.gui.BubbaGuiController;
import net.bubbaland.gui.BubbaPanel;
import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Civilization.Age;
import net.bubbaland.megaciv.game.Game;
import net.bubbaland.megaciv.messages.AdvanceAstMessage;

/**
 * A panel and dialog box to provide an interface for updating the AST track. Requirements to advance to the next space
 * on the AST track are shown for each civilization along with an indication whether each requirement is met. Dialog
 * will automatically check the boxes for civilizations that satisfy all requirements to advance (or the regress box
 * when appropriate at advanced difficulty). User can manually change these if necessary and then apply the advance.
 * This will also increment the turn counter for the game.
 * 
 * @author Walter Kolczynski
 *
 */
public class AstAlterationDialog extends BubbaDialogPanel {

	private static final long			serialVersionUID	= -3521456258596945263L;

	// Client handling communication to game server
	private final GuiClient				client;

	// List of panels displaying the information for each civilization
	private final ArrayList<CivPanel>	civPanels;

	// Number of columns to display. Rows & columns without civilizations will automatically be hidden.
	private final static int			N_COLUMNS			= 2;

	/**
	 * Create a new dialog to advance AST.
	 * 
	 * @param client
	 *            Client handling communication to game server.
	 * @param controller
	 *            Master GUI controller for this dialog.
	 */
	public AstAlterationDialog(GuiClient client, BubbaGuiController controller) {
		super(controller);
		this.client = client;

		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;

		this.civPanels = new ArrayList<CivPanel>();

		// Create panels for each civilization
		ArrayList<Civilization> civs = this.client.getGame().getCivilizations();
		for (Civilization civ : civs) {
			constraints.gridx = civ.getName().ordinal() % N_COLUMNS;
			constraints.gridy = civ.getName().ordinal() / N_COLUMNS;
			client.log(civ.getName() + ": (" + constraints.gridx + ", " + constraints.gridy + ")");
			CivPanel panel = new CivPanel(controller, civ);
			this.civPanels.add(panel);
			this.add(panel, constraints);
		}

		// Display this dialog
		this.dialog = new BubbaDialog(this.controller, "Advance AST", this, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		this.dialog.setVisible(true);

	}

	/**
	 * Retrieve information from dialog panel and send to server if OK was selected. This is triggered by the dialog
	 * closing.
	 * 
	 * @param event
	 *            The event closing the dialog.
	 */

	public void windowClosed(WindowEvent event) {
		super.windowClosed(event);

		// If the OK button was pressed, open the question
		final int option = ( (Integer) this.dialog.getValue() ).intValue();

		if (option == JOptionPane.OK_OPTION) {
			HashMap<Civilization.Name, Civilization.AstChange> advanceAst =
					new HashMap<Civilization.Name, Civilization.AstChange>();
			for (CivPanel panel : this.civPanels) {
				advanceAst.put(panel.getCivName(), panel.getAstChange());
			}
			this.client.log("Sending AST advances to server " + advanceAst);
			this.client.sendMessage(new AdvanceAstMessage(advanceAst));
		}
	}

	/**
	 * A panel to show civilization name, the requirements to advance the next AST step and whether each is met, and
	 * allow user input as to whether the civilization should advance (or possibly regress at advanaced difficulty).
	 * 
	 * @author Walter Kolczynski
	 *
	 */
	private class CivPanel extends BubbaPanel {

		private static final long		serialVersionUID	= -487711727769927447L;

		private final JCheckBox			advanceCheckbox, regressCheckbox;
		private final JTextPane			requirementsTextPane;

		private final Civilization.Name	name;

		/**
		 * Create a new panel.
		 * 
		 * @param controller
		 *            Master GUI controller for this panel.
		 * @param civ
		 *            Civilization attached to this panel.
		 */
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

			Color foreground = Game.FOREGROUND_COLORS.get(name); // Secondary color for civilization
			Color background = Game.BACKGROUND_COLORS.get(name); // Primary color for civilization

			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.NORTHWEST;
			constraints.weightx = 0.0;
			constraints.weighty = 0.0;

			this.setBackground(background);

			// Create a button group to hold the advance and regress checkboxes.
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

			// Display advance AST checkbox
			constraints.gridx = 0;
			constraints.gridy = 0;
			this.advanceCheckbox = new JCheckBox(WordUtils.capitalizeFully(name.toString()));
			group.add(this.advanceCheckbox);
			BubbaPanel.setButtonProperties(this.advanceCheckbox, civWidth, civHeight, foreground, background,
					civFontSize);
			this.add(this.advanceCheckbox, constraints);
			// Check the box if civilization meets all requirements
			this.advanceCheckbox.setSelected(civ.passAstRequirements(civ.getCurrentAge()));

			// Display regress checkbox
			constraints.gridx = 0;
			constraints.gridy = 2;
			this.regressCheckbox = new JCheckBox("Regress");
			group.add(this.regressCheckbox);
			BubbaPanel.setButtonProperties(this.regressCheckbox, civWidth, regressHeight, foreground, background,
					regressFontSize);
			if (AstAlterationDialog.this.client.getGame().getDifficulty() == Game.Difficulty.EXPERT) {
				this.add(this.regressCheckbox, constraints);
				// Check the box if civilization should regress
				this.regressCheckbox.setSelected(civ.getCurrentAge() != Age.STONE && civ.getCityCount() == 0);
			} else {
				// Hide regression checkbox if not playing advanced difficulty
				this.regressCheckbox.setSelected(false);
				this.regressCheckbox.setEnabled(false);
			}

			// Display requirements and current status for each
			String astReqText = civ.astRequirementString(civ.getNextStepAge(), false);
			constraints.weightx = 1.0;
			constraints.weighty = 1.0;
			constraints.gridx = 1;
			constraints.gridy = 0;
			constraints.gridheight = 3;
			this.requirementsTextPane = this.scrollableTextPaneFactory("", reqWidth, reqHeight, foreground, Color.WHITE,
					constraints, reqFontSize, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			this.requirementsTextPane.setContentType("text/html");
			this.requirementsTextPane.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, background));
			this.requirementsTextPane.setText(astReqText);
			this.requirementsTextPane.setEditable(false);
		}

		/**
		 * Get the civilization name attached to this panel.
		 * 
		 * @return The civilization's name.
		 */
		public Civilization.Name getCivName() {
			return this.name;
		}

		/**
		 * Get the type of AST change selected by this panel.
		 * 
		 * @return The AST change selected.
		 */
		public Civilization.AstChange getAstChange() {
			Civilization.AstChange change = Civilization.AstChange.NONE;
			if (this.advanceCheckbox.isSelected()) {
				change = Civilization.AstChange.ADVANCE;
			} else if (this.regressCheckbox.isSelected()) {
				change = Civilization.AstChange.REGRESS;
			}
			return change;
		}

	}

}
