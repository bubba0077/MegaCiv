package net.bubbaland.megaciv.client.gui;

import java.awt.Color;
import java.io.File;
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

import net.bubbaland.gui.*;
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

	public GuiController(String serverUrl) {
		super(DEFAULTS_FILENAME, SETTINGS_FILENAME, SETTINGS_VERSION);
		this.client = new GuiClient(serverUrl, this);
		this.client.run();

		while (!this.client.isConnected()) {
			setStatusBarText("Awaiting connection...");
			try {
				Thread.sleep(50);
			} catch (final InterruptedException exception) {
				exception.printStackTrace();
				System.exit(2);
			}
		}

		String userName = this.properties.getProperty("UserName");
		if (userName == null) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					new UserDialog(GuiController.this, GuiController.this.client, true);
				}
			});
		} else {
			User user = this.client.getUser();
			user.setUserName(userName);
			this.client.setUser(user);
		}

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					GuiController.this.updateGui();
				}
			});
		} catch (InvocationTargetException | InterruptedException exception) {
			exception.printStackTrace();
		}

		ToolTipManager.sharedInstance().setDismissDelay(5 * 60000); // Make tooltip timeout 5 minutes

		// this.log("Welcome " + this.client.getUser().getUserName());

	}

	public void createFrame(String frameName) {
		new SwingWorker<Void, Void>() {
			public Void doInBackground() {
				while (GuiController.this.client == null) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException exception) {}
				}
				return null;
			}

			public void done() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						String[] tabs =
								GuiController.this.properties.getProperty("Window." + frameName + ".OpenTabs", "[AST]")
										.replaceAll("[\\[\\]]", "").split(", ");
						// System.out.println(tabs);
						new MegaCivFrame(GuiController.this.client, GuiController.this).addTabs(tabs);
					}
				});
			}
		}.execute();
	}

	public Color getAstForegroundColor(Civilization.Age age) {
		return this.astForegroundColors.get(age);
	}

	public Color getAstBackgroundColor(Civilization.Age age) {
		return this.astBackgroundColors.get(age);
	}

	public void loadProperties(File file) {
		super.loadProperties(file);
		this.astForegroundColors = new HashMap<Civilization.Age, Color>();
		this.astBackgroundColors = new HashMap<Civilization.Age, Color>();
		for (Civilization.Age age : EnumSet.allOf(Civilization.Age.class)) {
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
	public void endProgram() {
		this.saveProperties();
		System.exit(0);
	}

	public void saveProperties() {
		super.saveProperties();
		String userName = this.client.getUser().getUserName();
		if (userName != null && this.client.getSession() != null
				&& !userName.equals(this.client.getSession().getId().substring(0, 7))) {
			this.properties.setProperty("UserName", this.client.getUser().getUserName());
		}
		this.savePropertyFile();
	}

	public static void main(String[] args) {
		final String serverURL;
		if (args.length > 0) {
			serverURL = args[0];
		} else {
			JTextField hostname = new JTextField("localhost");
			JSpinner port = new JSpinner(new SpinnerNumberModel(1100, 0, 65535, 1));
			Object[] message = { "Hostname:", hostname, "Port:", port };
			JOptionPane.showMessageDialog(null, message, "Server Configuration", JOptionPane.PLAIN_MESSAGE);
			serverURL = "ws://" + hostname.getText() + ":" + (int) port.getValue();
		}
		new GuiController(serverURL);
	}

	public void updateGui() {
		// this.client.log("Updating " + this.getClass().getSimpleName());
		super.updateGui();
	}

}
