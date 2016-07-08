package net.bubbaland.megaciv.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.websocket.Session;

import net.bubbaland.megaciv.messages.ClientMessage;

public class GameClient {

	// Format for log timestamps
	private static SimpleDateFormat	TIMESTAMP_FORMAT;

	final static public Properties	PROPERTIES			= new Properties();

	// File name to store window positions
	private final static String		DEFAULTS_FILENAME	= ".net.bubbaland.megaciv.client.defaults";
	// File name to store window positions
	protected final static String	SETTINGS_FILENAME	= ".net.bubbaland.megaciv.client.settings";
	// Settings version to force reloading defaults
	private final static String		SETTINGS_VERSION	= "1";

	static {
		/**
		 * Default properties
		 */
		loadDefaults();

		/**
		 * Load saved properties from file
		 */
		final File file = new File(System.getProperty("user.home") + "/" + SETTINGS_FILENAME);
		try {
			final BufferedReader fileBuffer = new BufferedReader(new FileReader(file));
			PROPERTIES.load(fileBuffer);
		} catch (final IOException e) {
			System.out.println("Couldn't load properties file, may not exist yet.");
		}

		/**
		 * If the version doesn't match, reload defaults
		 */
		final String version = PROPERTIES.getProperty("SettingsVersion");
		if (version == null || !version.equals(SETTINGS_VERSION)) {
			System.out.println("Using defaults");
			loadDefaults();
			PROPERTIES.setProperty("SettingsVersion", SETTINGS_VERSION);
		}

		// Set timestamp format
		TIMESTAMP_FORMAT = new SimpleDateFormat(PROPERTIES.getProperty("TimestampFormat"));
	}

	public void processIncomingMessage(ClientMessage message, Session session) {
		// TODO Auto-generated method stub
	}

	public void communicationsError(Session session, Throwable throwable) {
		// TODO Auto-generated method stub
	}

	public void connectionClosed() {
		// TODO Auto-generated method stub
	}

	/**
	 * Display message in the status bar and in console
	 *
	 * @param message
	 *            Message to log
	 */
	public void log(String message) {
		final String timestamp = TIMESTAMP_FORMAT.format(new Date());
		// Print message to console
		System.out.println(timestamp + " " + message);
	}

	/**
	 * Clear all saved data from file.
	 *
	 */
	public static void loadDefaults() {
		PROPERTIES.clear();
		final InputStream defaults = GameClient.class.getResourceAsStream(DEFAULTS_FILENAME);
		try {
			PROPERTIES.load(defaults);
		} catch (final IOException e) {
			System.out.println("Couldn't load default properties file, aborting!");
			System.exit(-1);
		}
	}

	/**
	 * Save the current properties to the settings file.
	 */
	protected static void savePropertyFile() {
		final File file = new File(System.getProperty("user.home") + "/" + SETTINGS_FILENAME);
		try {
			final BufferedWriter outfileBuffer = new BufferedWriter(new FileWriter(file));
			PROPERTIES.store(outfileBuffer, "MegaCiv");
			outfileBuffer.close();
		} catch (final IOException e) {
			System.out.println("Error saving properties.");
		}
	}
}
