package net.bubbaland.megaciv.client.gui;

import net.bubbaland.gui.*;

public class GuiMain extends BubbaGuiController {

	// File name of font
	// final static private String FONT_FILENAME = "fonts/tahoma.ttf";

	// File name to store window positions
	private final static String		DEFAULTS_FILENAME	= ".net.bubbaland.megaciv.client.defaults";
	// File name to store window positions
	protected final static String	SETTINGS_FILENAME	= ".net.bubbaland.megaciv.client.settings";
	// Settings version to force reloading defaults
	private final static String		SETTINGS_VERSION	= "1";

	static {
		// Add tabs & descriptions
	}

	public GuiMain(final String serverURL) {
		super(DEFAULTS_FILENAME, SETTINGS_FILENAME, SETTINGS_VERSION);
	}

	public static void main(String[] args) {
		// Schedule a job to create and show the GUI
		final String serverURL;
		if (args.length > 0) {
			serverURL = args[0];
		} else {
			serverURL = "ws://localhost:1100";
		}
		new GuiMain(serverURL);
	}

}
