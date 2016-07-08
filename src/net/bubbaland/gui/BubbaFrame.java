package net.bubbaland.gui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JSpinner;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SpinnerModel;
import javax.swing.SwingConstants;
import javazoom.jl.player.Player;

/**
 * Creates a top-level window for displaying the GUI.
 *
 * @author Walter Kolczynski
 *
 */
public class BubbaFrame extends JFrame implements WindowListener {

	private static final long	serialVersionUID	= -3639363131235278472L;

	// The status bar at the bottom
	final private JLabel		statusBar;

	protected final BubbaGuiController	gui;

	private boolean				initComplete;

	protected BubbaMainPanel	mainPanel;

	/**
	 * Internal constructor containing code common to the public constructors.
	 *
	 * @param client
	 *            The root client
	 */
	protected BubbaFrame(BubbaGuiController gui) {
		super();
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		this.gui = gui;

		final String title = this.gui.nextWindowName();
		this.setTitle(title);
		this.setName(title);
		this.loadPosition();
		this.addWindowListener(this);

		// Create a new panel to hold all GUI elements for the frame
		this.mainPanel = new BubbaMainPanel(this.gui, this) {
			private static final long serialVersionUID = -3431542881790392652L;

			@Override
			public void updateGUI(boolean forceUpdate) {
			}

			@Override
			protected void loadProperties(Properties properties) {
			}
		};

		/**
		 * Setup the menus
		 */
		{
			final JMenuBar menuBar = new JMenuBar();
			this.setJMenuBar(menuBar);
		}

		// Set up layout constraints
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;

		// Put the status bar at the bottom and do not adjust the size of the status bar
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;

		/**
		 * Setup status bar at bottom
		 */
		// Create status bar
		this.statusBar = mainPanel.enclosedLabelFactory("", 0, 0, this.getForeground(), this.getBackground(),
				constraints, 0, SwingConstants.LEFT, SwingConstants.CENTER);

		// Setup layout constraints
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;

		// Add the panel to the frame and display the frame
		this.add(mainPanel);
		this.setVisible(true);

		// Load the properties
		this.loadProperties();

		this.initComplete = true;
	}

	public BubbaGuiController getGui() {
		return this.gui;
	}


	/**
	 * Get the root client.
	 *
	 * @return The root client
	 */
	// public TriviaClient getClient() {
	// return this.client;
	// }

	public BubbaGuiController getGUI() {
		return this.gui;
	}

	/**
	 * Load all of the properties from the client and apply them.
	 */
	public void loadProperties() {
		final String id = this.getTitle();

		// Apply to status bar
		final int height = Integer.parseInt(this.loadProperty(id, "StatusBar.Height"));
		final float fontSize = Float.parseFloat(this.loadProperty(id, "StatusBar.FontSize"));
		this.statusBar.getParent().setPreferredSize(new Dimension(0, height));
		this.statusBar.getParent().setMinimumSize(new Dimension(0, height));
		this.statusBar.setFont(this.statusBar.getFont().deriveFont(fontSize));
	}

	/**
	 * Display message in the status bar and in console
	 *
	 * @param message
	 *            Message to log
	 */
	public void log(String message) {
		// Display message in status bar
		this.statusBar.setText(message);
	}


	public void updateGUI(boolean forceUpdate) {
		while (!this.initComplete) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException exception) {
			}
		}
	}

	/**
	 * Load the saved position and size of the window from file. If none found, use preferred size of components.
	 *
	 */
	protected void loadPosition() {
		try {
			final String frameID = this.getName();

			final int x = Integer.parseInt(this.gui.getProperties().getProperty(frameID + ".X"));
			final int y = Integer.parseInt(this.gui.getProperties().getProperty(frameID + ".Y"));
			final int width = Integer.parseInt(this.gui.getProperties().getProperty(frameID + ".Width"));
			final int height = Integer.parseInt(this.gui.getProperties().getProperty(frameID + ".Height"));

			this.setBounds(x, y, width, height);

		} catch (final NumberFormatException e) {
			this.setSize(800, 800);
			this.setLocationRelativeTo(null);
		}
	}

	/**
	 * Save properties.
	 */
	public void saveProperties() {
		final String id = this.getTitle();
		final int height = this.statusBar.getPreferredSize().getSize().height;
		final float fontSize = this.statusBar.getFont().getSize2D();
		this.gui.getProperties().setProperty(id + "." + "StatusBar.Height", height + "");
		this.gui.getProperties().setProperty(id + "." + "StatusBar.FontSize", fontSize + "");
	}

	/**
	 * Load property for this window name. First looks for property specific to this iteration of TriviaFrame, then
	 * looks to the default version.
	 *
	 * @param id
	 *            The frame's name
	 * @param propertyName
	 *            The property name
	 * @return The property requested
	 */
	private String loadProperty(String id, String propertyName) {
		return this.gui.getProperties().getProperty(id + "." + propertyName,
				this.gui.getProperties().getProperty(propertyName));
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
		this.gui.savePosition(window);
		if (window instanceof BubbaFrame) {
			( (BubbaFrame) window ).saveProperties();

			if (this.gui.getNWindows() == 1) {
				// This is the last window, go through exit procedures
				this.gui.endProgram();
			} else {
				// Remove window from the list
				this.gui.unregisterWindow((BubbaFrame) window);
			}
		}
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

	// Inner class that defines our special slider menu item
	protected class SpinnerMenuItem extends JSpinner implements MenuElement {

		private static final long serialVersionUID = 7803892810923109389L;

		private SpinnerMenuItem() {
			super();
		}

		private SpinnerMenuItem(SpinnerModel model) {
			super(model);
		}

		@Override
		public Component getComponent() {
			return this;
		}

		@Override
		public MenuElement[] getSubElements() {
			return new MenuElement[0];
		}

		@Override
		public void menuSelectionChanged(boolean isIncluded) {
		}

		@Override
		public void processKeyEvent(KeyEvent e, MenuElement path[], MenuSelectionManager manager) {
		}

		@Override
		public void processMouseEvent(MouseEvent e, MenuElement path[], MenuSelectionManager manager) {
		}
	}

	protected static class BubbaAudio {
		private final String filename;

		public BubbaAudio(String filename) {
			this.filename = filename;
		}

		public void play() {
			try {
				final Player player = new Player(BubbaGuiController.class.getResourceAsStream(this.filename));
				new Thread() {
					@Override
					public void run() {
						try {
							player.play();
						} catch (final Exception e) {

						} finally {
							player.close();
						}
					}
				}.start();
			} catch (final Exception e) {
				System.out.println("Couldn't open audio file");
				e.printStackTrace();
			}
		}
	}


}
