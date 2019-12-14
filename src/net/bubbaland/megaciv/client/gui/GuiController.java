package net.bubbaland.megaciv.client.gui;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.EnumSet;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;

import net.bubbaland.gui.BubbaGuiController;
import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.User;

public class GuiController extends BubbaGuiController {

	// File name to store window positions
	private final static String					DEFAULTS_FILENAME	= ".net.bubbaland.megaciv.client.gui.defaults";
	// File name to store window positions
	protected final static String				SETTINGS_FILENAME	= ".net.bubbaland.megaciv.client.gui.settings";
	// Settings version to force reloading defaults
	private final static String					SETTINGS_VERSION	= "10";

	private final GuiClient						client;

	private HashMap<Civilization.Age, Color>	astForegroundColors, astBackgroundColors;

	public GuiController(final String serverUrl) {
		super(DEFAULTS_FILENAME, SETTINGS_FILENAME, SETTINGS_VERSION);
		this.client = new GuiClient(serverUrl, this);
		this.client.run();

		while (!this.client.isConnected()) {
			this.setStatusBarText("Awaiting connection...");
			try {
				Thread.sleep(50);
			} catch (final InterruptedException exception) {
				exception.printStackTrace();
				System.exit(2);
			}
		}

		final String userName = this.properties.getProperty("UserName");
		if (userName == null) {
			SwingUtilities.invokeLater(() -> new UserDialog(GuiController.this, GuiController.this.client, true));
		} else {
			final User user = this.client.getUser();
			user.setUserName(userName);
			this.client.setUser(user);
		}

		try {
			SwingUtilities.invokeAndWait(() -> GuiController.this.updateGui());
		} catch (InvocationTargetException | InterruptedException exception) {
			exception.printStackTrace();
		}

		ToolTipManager.sharedInstance().setDismissDelay(5 * 60000); // Make tooltip timeout 5 minutes

		// this.log("Welcome " + this.client.getUser().getUserName());

	}

	@Override
	public void createFrame(final String frameName) {
		new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() {
				while (GuiController.this.client == null) {
					try {
						Thread.sleep(10);
					} catch (final InterruptedException exception) {}
				}
				return null;
			}

			@Override
			public void done() {
				SwingUtilities.invokeLater(() -> {
					final String[] tabs =
							GuiController.this.properties.getProperty("Window." + frameName + ".OpenTabs", "[AST]")
									.replaceAll("[\\[\\]]", "").split(", ");
					// System.out.println(tabs);
					new MegaCivFrame(GuiController.this.client, GuiController.this).addTabs(tabs);
				});
			}
		}.execute();
	}

	public Color getAstForegroundColor(final Civilization.Age age) {
		return this.astForegroundColors.get(age);
	}

	public Color getAstBackgroundColor(final Civilization.Age age) {
		return this.astBackgroundColors.get(age);
	}

	@Override
	public void loadProperties(final File file) {
		super.loadProperties(file);
		this.astForegroundColors = new HashMap<Civilization.Age, Color>();
		this.astBackgroundColors = new HashMap<Civilization.Age, Color>();
		for (final Civilization.Age age : EnumSet.allOf(Civilization.Age.class)) {
			this.astForegroundColors.put(age, new Color(
					new BigInteger(this.getProperties().getProperty("AstTable." + age.name() + ".ForegroundColor"), 16)
							.intValue()));
			this.astBackgroundColors.put(age, new Color(
					new BigInteger(this.getProperties().getProperty("AstTable." + age.name() + ".BackgroundColor"), 16)
							.intValue()));
		}

	}

	/**
	 * Add the current window contents to properties, then save the properties to the settings file and exit.
	 */
	@Override
	public void endProgram() {
		this.saveProperties();
		if (client.getSession() != null) {
			try {
				this.client.getSession().close();
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
		System.exit(0);
	}

	@Override
	public void saveProperties() {
		super.saveProperties();
		final String userName = this.client.getUser().getUserName();
		if (userName != null && this.client.getSession() != null
				&& !userName.equals(this.client.getSession().getId().substring(0, 7))) {
			this.properties.setProperty("UserName", this.client.getUser().getUserName());
		}
		this.savePropertyFile();
	}

	public static void main(final String[] args) {
		final String serverURL;
		if (args.length > 0) {
			serverURL = args[0];
		} else {
			final JTextField hostname = new JTextField("localhost");
			final JSpinner port = new JSpinner(new SpinnerNumberModel(1099, 0, 65535, 1));
			final Object[] message = { "Hostname:", hostname, "Port:", port };
			JOptionPane.showMessageDialog(null, message, "Server Configuration", JOptionPane.PLAIN_MESSAGE);
			serverURL = "ws://" + hostname.getText() + ":" + (int) port.getValue();
		}
		new GuiController(serverURL);
	}

	@Override
	public void updateGui() {
		// this.client.log("Updating " + this.getClass().getSimpleName());
		super.updateGui();
	}

}
