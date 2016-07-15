package net.bubbaland.megaciv.client.gui;

import java.awt.GridBagConstraints;
import java.awt.event.WindowEvent;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import net.bubbaland.gui.BubbaDialog;
import net.bubbaland.gui.BubbaDialogPanel;

public class WaitDialog extends BubbaDialogPanel {

	private static final long	serialVersionUID	= 6237850645754945230L;

	private final GuiController	controller;

	public WaitDialog(GuiController controller) {
		super(controller);

		this.controller = controller;

		// Set up layout constraints
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;

		constraints.gridx = 0;
		constraints.gridy = 0;
		final JLabel label = new JLabel("Awaiting data from server...", SwingConstants.LEFT);
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		final Object[] options = { "Exit" };
		this.dialog = new BubbaDialog(controller, "Awaiting Data", this, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_OPTION, null, options);
		// this.dialog.setModal(false);
		this.dialog.pack();
		// this.setVisible(true);
		this.dialog.setVisible(true);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		this.dialog.setVisible(visible);
	}

	public void dispose() {
		this.dialog.dispose();
	}

	@Override
	public void windowClosed(WindowEvent event) {
		super.windowClosed(event);
		if (this.dialog.getValue().equals("Exit") || this.dialog.getValue().equals(JOptionPane.CLOSED_OPTION)) {
			this.controller.endProgram();
		}
	}
}