package net.bubbaland.gui;

import java.awt.Rectangle;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.swing.SwingUtilities;

/**
 * GUI controller to coordinate multiple frames and provide access to saved properties.
 *
 *
 *
 * @author Walter Kolczynski
 *
 */
public abstract class BubbaGuiController {

	protected Properties					properties;

	// List of active windows
	protected final ArrayList<BubbaFrame>	windowList;

	// Format for log timestamps
	private SimpleDateFormat				timestampFormat;

	// File name to store window positions
	private final String					defaultsFilename;
	// File name to store window positions
	protected final String					settingsFilename;

	protected BubbaGuiController(final String defaultsFilename, final String settingsFilename,
			final String settingsVersion) {
		this.defaultsFilename = defaultsFilename;
		this.settingsFilename = settingsFilename;

		this.properties = new Properties();
		this.windowList = new ArrayList<BubbaFrame>();

		this.timestampFormat = new SimpleDateFormat("[yyyy MMM dd HH:mm:ss]");

		/**
		 * Default properties
		 */
		this.loadDefaults();

		/**
		 * Load saved properties from file
		 */
		final File file = new File(System.getProperty("user.home") + "/" + settingsFilename);
		this.loadProperties(file);

		/**
		 * If the version doesn't match, reload defaults
		 */
		final String version = this.properties.getProperty("SettingsVersion");
		if (version == null || !version.equals(settingsVersion)) {
			System.out.println(this.getClass().getSimpleName() + "Using defaults");
			this.loadDefaults();
			this.properties.setProperty("SettingsVersion", settingsVersion);
		}

		// Set timestamp format
		this.timestampFormat = new SimpleDateFormat(this.properties.getProperty("TimestampFormat"));

		SwingUtilities.invokeLater(() -> {
			for (final String frameName : BubbaGuiController.this.properties.getProperty("OpenWindows").split(",")) {
				BubbaGuiController.this.createFrame(frameName);
			}
		});
	}

	public abstract void createFrame(String frameName);

	/**
	 * Clear all saved data from file.
	 *
	 */
	public void loadDefaults() {
		this.setStatusBarText("Loading defaults");
		this.properties.clear();
		final InputStream defaults = this.getClass().getResourceAsStream(this.defaultsFilename);
		try {
			this.properties.load(defaults);
		} catch (final IOException e) {
			System.out.println(this.getClass().getSimpleName() + "Couldn't load default properties file, aborting!");
			System.exit(-1);
		}
		BubbaDialogPanel.loadProperties(this.properties);

		for (final BubbaFrame frame : this.windowList) {
			frame.loadProperties();
		}
	}

	public void loadProperties(final File file) {
		try {
			final BufferedReader fileBuffer = new BufferedReader(new FileReader(file));
			this.properties.load(fileBuffer);
		} catch (final IOException e) {
			System.out.println(this.getClass().getSimpleName() + "Couldn't load properties file, may not exist yet.");
		}
		for (final BubbaFrame frame : this.windowList) {
			frame.loadProperties();
		}
	}

	/**
	 * Save the current properties to the settings file.
	 */
	protected void savePropertyFile() {
		final File file = new File(System.getProperty("user.home") + "/" + this.settingsFilename);
		try {
			final BufferedWriter outfileBuffer = new BufferedWriter(new FileWriter(file));
			this.properties.store(outfileBuffer, "MegaCiv");
			outfileBuffer.close();
		} catch (final IOException e) {
			System.out.println(this.getClass().getSimpleName() + "Error saving properties.");
		}
	}


	/**
	 * Save the position and size of the window to file.
	 *
	 * @param window
	 *            The window whose size and position is to be saved
	 *
	 */
	public void savePositionAndSize(final Window window) {
		final Rectangle r = window.getBounds();
		final int x = (int) r.getX();
		final int y = (int) r.getY();
		final int width = (int) r.getWidth();
		final int height = (int) r.getHeight();

		final String frameID = window.getName();

		this.properties.setProperty(frameID + ".X", x + "");
		this.properties.setProperty(frameID + ".Y", y + "");
		this.properties.setProperty(frameID + ".Width", width + "");
		this.properties.setProperty(frameID + ".Height", height + "");
	}

	/**
	 * Save the position and size of the window to file.
	 *
	 * @param window
	 *            The window whose size and position is to be saved
	 *
	 */
	public void savePosition(final Window window) {
		final Rectangle r = window.getBounds();
		final int x = (int) r.getX();
		final int y = (int) r.getY();

		final String frameID = window.getName();

		this.properties.setProperty(frameID + ".X", x + "");
		this.properties.setProperty(frameID + ".Y", y + "");
	}

	/**
	 * Get the name for the next top-level frame.
	 *
	 * @return The frame name
	 */
	public String nextWindowName() {
		final ArrayList<String> windowNames = new ArrayList<String>(0);
		for (final BubbaFrame frame : this.windowList) {
			windowNames.add(frame.getTitle());
		}
		String name = "MegaCiv";
		for (int i = 1; windowNames.contains(name); i++) {
			name = "MegaCiv (" + i + ")";
		}
		return name;
	}

	public BubbaFrame getFirstWindow() {
		return this.windowList.get(0);
	}

	/**
	 * Register a window as a child of the client. New Trivia Frames do this so the client can track events from them.
	 *
	 * @param frame
	 *            The window to track
	 */
	public void registerWindow(final BubbaFrame frame) {
		this.windowList.add(frame);
	}

	/**
	 * Unregister a window as a child of the client. This is done when a window closes.
	 *
	 * @param frame
	 *            The window to stop tracking
	 */
	public void unregisterWindow(final BubbaFrame frame) {
		this.windowList.remove(frame);
		if (this.windowList.size() == 0) {
			this.endProgram();
		}
	}

	/**
	 * Display message in the status bar and in console
	 *
	 * @param message
	 *            Message to log
	 */
	public void setStatusBarText(final String message) {
		final String timestamp = this.timestampFormat.format(new Date());
		for (final BubbaFrame frame : this.windowList) {
			// Display message in status bar
			frame.setStatusBarMessage(timestamp + " " + message);
		}
	}

	public int getNWindows() {
		return this.windowList.size();
	}

	public Properties getProperties() {
		return this.properties;
	}

	/**
	 * Add the current window contents to properties, then save the properties to the settings file and exit.
	 */
	public void endProgram() {
		this.saveProperties();
		System.exit(0);
	}

	public void saveProperties() {
		final ArrayList<String> windowNames = new ArrayList<String>() {
			private static final long serialVersionUID = 6362800947141566082L;

			{
				for (final BubbaFrame window : BubbaGuiController.this.windowList) {
					this.add(window.getTitle());
					BubbaGuiController.this.setStatusBarText(window.getTitle());
				}
			}
		};
		this.properties.setProperty("OpenWindows", String.join(", ", windowNames));

		for (final BubbaFrame window : this.windowList) {
			window.saveProperties();
			this.savePositionAndSize(window);
		}
		this.savePropertyFile();

	}

	public void updateGui() {
		for (final BubbaFrame frame : this.windowList) {
			SwingUtilities.invokeLater(() -> frame.updateGui());
		}
	}

}
