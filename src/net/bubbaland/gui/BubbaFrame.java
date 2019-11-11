package net.bubbaland.gui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JSpinner;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SpinnerModel;
import javax.swing.SwingConstants;

/**
 * Creates a top-level window for displaying the GUI.
 *
 * @author Walter Kolczynski
 *
 */
public class BubbaFrame extends JFrame implements WindowListener {

	private static final long			serialVersionUID	= -3639363131235278472L;

	// The status bar at the bottom
	final protected JLabel				statusBar;

	protected final BubbaGuiController	controller;

	private final boolean				initComplete;

	protected BubbaMainPanel			mainPanel;

	private final int					MAX_HTML_WIDTH		= 50;

	/**
	 * Internal constructor containing code common to the public constructors.
	 *
	 * @param controller
	 *            The root GUI controller for this frame
	 */
	protected BubbaFrame(final BubbaGuiController controller) {
		super();
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		this.controller = controller;

		final String title = this.controller.nextWindowName();
		this.setTitle(title);
		this.setName(title);
		this.loadPosition();
		this.addWindowListener(this);

		// Create a new panel to hold all GUI elements for the frame
		this.mainPanel = new BubbaMainPanel(this.controller, this) {
			private static final long serialVersionUID = -3431542881790392652L;

			@Override
			public void updateGui() {}

			@Override
			protected void loadProperties() {}
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
		this.statusBar = this.mainPanel.enclosedLabelFactory("", 0, 0, this.getForeground(), this.getBackground(),
				constraints, 0, SwingConstants.LEFT, SwingConstants.CENTER);

		// Setup layout constraints
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;

		// Add the panel to the frame and display the frame
		this.add(this.mainPanel);
		this.setVisible(true);

		// Load the properties
		this.loadProperties();

		this.initComplete = true;
		this.setCursor(null);
	}

	public BubbaGuiController getGui() {
		return this.controller;
	}

	/**
	 * Load all of the properties from the client and apply them.
	 */
	public void loadProperties() {
		final String id = this.getTitle();

		// Apply to status bar
		final int height = Integer.parseInt(this.loadProperty(id, "StatusBar.Height"));
		final float fontSize = Float.parseFloat(this.loadProperty(id, "StatusBar.FontSize"));

		BubbaPanel.setLabelProperties(this.statusBar, 1, height, null, null, fontSize);
	}

	/**
	 * Display message in the status bar and in console
	 *
	 * @param message
	 *            Message to log
	 */
	public void setStatusBarMessage(final String message) {
		// Display message in status bar
		this.statusBar.setText(message);
		this.statusBar.setToolTipText(message);

		String maxWidthText = "<html>";
		final Pattern regex = Pattern.compile("(.{1," + this.MAX_HTML_WIDTH + "}(?:\\s|$))", Pattern.DOTALL);
		final Matcher matcher = regex.matcher(message);
		while (matcher.find()) {
			maxWidthText = maxWidthText + matcher.group() + "<BR/>";
		}
		maxWidthText = maxWidthText + "</html>";
		this.statusBar.setToolTipText(maxWidthText);
	}

	public void updateGui() {
		while (!this.initComplete) {
			try {
				Thread.sleep(10);
			} catch (final InterruptedException exception) {}
		}
		this.mainPanel.updateGui();
	}

	/**
	 * Load the saved position and size of the window from file. If none found, use preferred size of components.
	 *
	 */
	protected void loadPosition() {
		try {
			final String frameID = this.getName();

			final int x = Integer.parseInt(this.controller.getProperties().getProperty(frameID + ".X"));
			final int y = Integer.parseInt(this.controller.getProperties().getProperty(frameID + ".Y"));
			final int width = Integer.parseInt(this.controller.getProperties().getProperty(frameID + ".Width"));
			final int height = Integer.parseInt(this.controller.getProperties().getProperty(frameID + ".Height"));

			this.setBounds(x, y, width, height);

		} catch (final NumberFormatException e) {
			this.setSize(1360, 768);
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
		this.controller.getProperties().setProperty(id + "." + "StatusBar.Height", height + "");
		this.controller.getProperties().setProperty(id + "." + "StatusBar.FontSize", fontSize + "");
	}

	/**
	 * Load property for this window name. First looks for property specific to this iteration of Frame, then looks to
	 * the default version.
	 *
	 * @param id
	 *            The frame's name
	 * @param propertyName
	 *            The property name
	 * @return The property requested
	 */
	private String loadProperty(final String id, final String propertyName) {
		return this.controller.getProperties().getProperty(id + "." + propertyName,
				this.controller.getProperties().getProperty(propertyName));
	}

	@Override
	public void windowActivated(final WindowEvent e) {}

	@Override
	public void windowClosed(final WindowEvent e) {}

	/**
	 * When one of the windows tries to close, save the properties and position of the window first. Then exit the
	 * program if there are no open windows left.
	 */
	@Override
	public void windowClosing(final WindowEvent e) {
		final Window window = e.getWindow();
		// Save the window position
		this.controller.savePositionAndSize(window);
		if (window instanceof BubbaFrame) {
			( (BubbaFrame) window ).saveProperties();
			this.controller.unregisterWindow((BubbaFrame) window);
		}
	}

	@Override
	public void windowDeactivated(final WindowEvent e) {}

	@Override
	public void windowDeiconified(final WindowEvent e) {}

	@Override
	public void windowIconified(final WindowEvent e) {}

	@Override
	public void windowOpened(final WindowEvent e) {}

	// Inner class that defines our special slider menu item
	protected class SpinnerMenuItem extends JSpinner implements MenuElement {

		private static final long serialVersionUID = 7803892810923109389L;

		private SpinnerMenuItem() {
			super();
		}

		private SpinnerMenuItem(final SpinnerModel model) {
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
		public void menuSelectionChanged(final boolean isIncluded) {}

		@Override
		public void processKeyEvent(final KeyEvent e, final MenuElement path[], final MenuSelectionManager manager) {}

		@Override
		public void processMouseEvent(final MouseEvent e, final MenuElement path[],
				final MenuSelectionManager manager) {}
	}
}
