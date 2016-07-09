package net.bubbaland.megaciv.client.gui;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import net.bubbaland.gui.*;

public class GuiController extends BubbaGuiController {

	// File name of font
	// final static private String FONT_FILENAME = "fonts/tahoma.ttf";

	// File name to store window positions
	private final static String		DEFAULTS_FILENAME	= ".net.bubbaland.megaciv.client.defaults";
	// File name to store window positions
	protected final static String	SETTINGS_FILENAME	= ".net.bubbaland.megaciv.client.settings";
	// Settings version to force reloading defaults
	private final static String		SETTINGS_VERSION	= "3";

	private final GuiClient			client;
	private WaitDialog				waitDialog;

	public GuiController(GuiClient client) {
		super(DEFAULTS_FILENAME, SETTINGS_FILENAME, SETTINGS_VERSION);
		this.client = client;

		this.waitDialog = new WaitDialog(this);

		while (!this.client.isConnected()) {
			log("Awaiting data...");
			try {
				Thread.sleep(50);
			} catch (final InterruptedException exception) {
				exception.printStackTrace();
				System.exit(2);
			}
		}

		if (this.waitDialog != null) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					GuiController.this.waitDialog.dispose();
				}
			});
		}

		// Create startup frames
		new MegaCivFrame(this);

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					GuiController.this.updateGui(true);
				}
			});
		} catch (InvocationTargetException | InterruptedException exception) {
			exception.printStackTrace();
		}
		// this.log("Welcome " + this.client.getUser().getUserName());

	}

	public void endProgram() {
		System.exit(0);
	}
}
