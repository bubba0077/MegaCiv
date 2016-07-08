package net.bubbaland.megaciv.client.gui;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import net.bubbaland.megaciv.client.*;

public class GuiMain extends GameClient implements WindowListener {

	// File name of font
	final static private String FONT_FILENAME = "fonts/tahoma.ttf";

	static {
		/**
		 * Load Nimbus
		 */
		for (final LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			if ("Nimbus".equals(info.getName())) {
				try {
					UIManager.setLookAndFeel(new NimbusLookAndFeel() {
						private static final long serialVersionUID = -4162111942682867066L;

						@Override
						public UIDefaults getDefaults() {
							final UIDefaults ret = super.getDefaults();
							Font font;
							try {
								font = Font.createFont(Font.TRUETYPE_FONT,
										GuiMain.class.getResourceAsStream(FONT_FILENAME));
								ret.put("defaultFont", font.deriveFont(12f));
							} catch (FontFormatException | IOException exception) {
								exception.printStackTrace();
							}
							return ret;
						}

					});
				} catch (final UnsupportedLookAndFeelException exception) {
					exception.printStackTrace();
				}
			}


		}
	}

	public GuiMain(final String serverURL) {
		super();
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

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	/**
	 * When one of the windows tries to close, save the properties and position of the window first. Then exit the
	 * program if there are no open windows left.
	 */
	@Override
	public void windowClosing(WindowEvent e) {
		final Window window = e.getWindow();
		// Save the window position
		savePosition(window);
		// if (window instanceof TriviaFrame) {
		// ( (TriviaFrame) window ).saveProperties();
		// DnDTabbedPane.unregisterTabbedPane(( (TriviaFrame) window ).getTabbedPane());
		//
		// if (this.windowList.size() == 1) {
		// // This is the last window, go through exit procedures
		// this.endProgram();
		// } else {
		// // Remove window from the list
		// this.unregisterWindow((TriviaFrame) window);
		// }
		// }
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	/**
	 * Save the position and size of the window to file.
	 *
	 * @param window
	 *            The window whose size and position is to be saved
	 *
	 */
	public static void savePosition(Window window) {
		final Rectangle r = window.getBounds();
		final int x = (int) r.getX();
		final int y = (int) r.getY();
		final int width = (int) r.getWidth();
		final int height = (int) r.getHeight();

		final String frameID = window.getName();

		PROPERTIES.setProperty(frameID + ".X", x + "");
		PROPERTIES.setProperty(frameID + ".Y", y + "");
		PROPERTIES.setProperty(frameID + ".Width", width + "");
		PROPERTIES.setProperty(frameID + ".Height", height + "");
	}


}
