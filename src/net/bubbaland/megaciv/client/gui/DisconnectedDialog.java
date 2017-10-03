package net.bubbaland.megaciv.client.gui;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import net.bubbaland.gui.BubbaDialog;
import net.bubbaland.gui.BubbaDialogPanel;

public class DisconnectedDialog extends BubbaDialogPanel implements ActionListener {

	private static final long	serialVersionUID	= 6237850645754945230L;

	private final GuiController	controller;
	private final GuiClient		client;
	private final JLabel		waitingLabel;
	private int					secs;
	private final static int	RETRY_TIME			= 60;

	public DisconnectedDialog(GuiController controller, GuiClient client) {
		super(controller);

		this.controller = controller;
		this.client = client;
		this.secs = RETRY_TIME;

		// Set up layout constraints
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;

		constraints.gridx = 0;
		constraints.gridy = 0;
		final JLabel label = new JLabel("Disconnected from server!", JLabel.CENTER);
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		this.waitingLabel = new JLabel("Retrying in 60s", JLabel.CENTER);
		this.add(this.waitingLabel, constraints);

		final Object[] options = { "Retry Now", "Exit" };
		this.dialog = new BubbaDialog(controller, "Disconnected", this, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_OPTION, null, options);
		this.dialog.pack();
		this.dialog.setVisible(true);

		Timer timer = new Timer(1000, this);
		timer.start();

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
		this.client.run();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		this.secs--;
		if (this.secs == 0) {
			this.dispose();
			return;
		}
		this.waitingLabel.setText("Retrying in " + this.secs + "s");
	}
}
