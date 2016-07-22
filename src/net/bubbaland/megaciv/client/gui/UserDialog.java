package net.bubbaland.megaciv.client.gui;

import java.awt.GridBagConstraints;
import java.awt.event.WindowEvent;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import net.bubbaland.gui.BubbaDialog;
import net.bubbaland.gui.BubbaDialogPanel;
import net.bubbaland.gui.BubbaGuiController;
import net.bubbaland.megaciv.game.User;
import net.bubbaland.megaciv.messages.SetUserMessage;

/**
 * Creates prompt for user name.
 *
 * @author Walter Kolczynski
 *
 */
public class UserDialog extends BubbaDialogPanel {

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= 7708693892976942384L;

	private final GuiClient		client;
	private final JTextField	userTextField;

	/**
	 * Instantiates a new user login.
	 *
	 * @param client
	 *            the client
	 * @param role
	 */
	public UserDialog(BubbaGuiController controller, GuiClient client) {
		super(controller);

		this.client = client;

		// Set up layout constraints
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.0;
		c.weighty = 0.0;

		// Prompt for user name
		c.gridx = 0;
		c.gridy = 0;
		final JLabel label = new JLabel("Enter user name: ");
		label.setFont(label.getFont().deriveFont(fontSize));
		this.add(label, c);

		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1.0;
		this.userTextField = new JTextField("", 15);
		this.userTextField.setFont(this.userTextField.getFont().deriveFont(fontSize));
		this.add(this.userTextField, c);
		this.userTextField.addAncestorListener(this);

		final String userName = client.getUser().getUserName();
		int options;
		if (userName == null) {
			options = JOptionPane.DEFAULT_OPTION;
		} else {
			this.userTextField.setText(userName);
			options = JOptionPane.OK_CANCEL_OPTION;
		}

		// Display the dialog box
		this.dialog = new BubbaDialog(this.controller, "User Login", this, JOptionPane.PLAIN_MESSAGE, options);
		this.dialog.setVisible(true);
	}

	@Override
	public void windowClosed(WindowEvent event) {
		super.windowClosed(event);

		// Set the user name to input value
		final String userName = this.userTextField.getText();

		final int option = ( (Integer) this.dialog.getValue() ).intValue();

		if (option == JOptionPane.OK_OPTION) {
			// Check if name is already being used
			while (UserDialog.this.client.getUserList() == null) {
				try {
					System.out.println("Waiting for user list");
					Thread.sleep(500);
				} catch (final InterruptedException exception) {
					// Nothing to do
				}
			}

			if (UserDialog.this.client.userNameExists(userName)) {
				final int confirm = JOptionPane.showConfirmDialog(null,
						"The name \"" + userName
								+ "\" has been connected recently. Do you still want to use this name?",
						"Name Conflict", JOptionPane.YES_NO_OPTION);
				if (confirm == JOptionPane.NO_OPTION) {
					new UserDialog(UserDialog.this.controller, UserDialog.this.client);
					return;
				}
			}

			if (userName.toCharArray().length != 0) {
				User user = UserDialog.this.client.getUser();
				user.setUserName(userName);
				UserDialog.this.client.log("Changed user name to " + user.getUserName());
				UserDialog.this.client.sendMessage(new SetUserMessage(user));
			} else {
				new UserDialog(UserDialog.this.controller, UserDialog.this.client);
			}
		}
	}
}
