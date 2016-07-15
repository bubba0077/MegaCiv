package net.bubbaland.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Point;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Set;

import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class BubbaDragDropTabFrame extends BubbaFrame implements ActionListener, ChangeListener {

	/**
	 * 
	 */
	private static final long					serialVersionUID	= -8926818996029674620L;

	/** List of available tabs and associated descriptions. */
	protected Hashtable<String, TabInformation>	tabInformationHash;
	protected final BubbaDnDTabbedPane			tabbedPane;

	/**
	 * Creates a new frame based on a drag-drop event from the tabbed pane in another frame. This is done when a tab is
	 * dragged outside of all other TriviaFrames.
	 *
	 * @param client
	 *            The root client
	 * @param a_event
	 *            The drag-drop event
	 */
	public BubbaDragDropTabFrame(BubbaGuiController gui, DropTargetDropEvent a_event, Point location) {
		this(gui);
		this.tabbedPane.convertTab(this.tabbedPane.getTabTransferData(a_event),
				this.tabbedPane.getTargetTabIndex(a_event.getLocation()));
		this.tabbedPane.setSelectedIndex(0);
		this.pack();
		this.setLocation(location);
		this.tabbedPane.addChangeListener(this);
		this.setCursor(null);
	}

	/**
	 * Creates a new frame with specified tabs.
	 *
	 * @param client
	 *            The root client
	 * @param initialTabs
	 *            Tabs to open initially
	 */
	public BubbaDragDropTabFrame(BubbaGuiController gui, String[] initialTabs) {
		this(gui);
		this.addTabs(initialTabs);
		this.tabbedPane.setSelectedIndex(0);
		this.tabbedPane.addChangeListener(this);
		this.loadPosition();
	}

	public BubbaDragDropTabFrame(BubbaGuiController controller) {
		super(controller);
		this.initTabInfoHash();

		// Create drag & drop tabbed pane
		this.tabbedPane = new BubbaDnDTabbedPane(controller, this);
		this.tabbedPane.setName("Tabbed Pane");

		// Set up layout constraints
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;

		// Add the tabbed pane to the panel
		this.mainPanel.add(this.tabbedPane, constraints);

		this.controller.registerWindow(this);

		// Load the properties
		this.loadProperties();

	}

	protected void addTab(String tabName) {
		try {
			BubbaMainPanel newTab = this.tabFactory(this, tabName.replaceFirst(" \\([0-9]*\\)", ""));
			this.tabbedPane.addTab(tabName, newTab);
			newTab.updateGui(true);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		}
	}

	protected void addTabs(String[] tabs) {
		for (final String tabName : tabs) {
			this.addTab(tabName);
		}
	}

	protected void initTabInfoHash() {
		this.tabInformationHash = new Hashtable<String, TabInformation>();
	}

	/**
	 * Get a list of available tab names.
	 *
	 * @return The available tab names
	 */
	public Set<String> getTabNames() {
		return tabInformationHash.keySet();
	}

	/**
	 * Get the description associated with a tab name
	 *
	 * @param tabName
	 *            The tab name
	 * @return The description associated with the tab name
	 */
	public String getTabDescription(String tabName) {
		if (tabInformationHash.get(tabName) == null) {
			return "";
		}
		return tabInformationHash.get(tabName).getTabDescription();
	}

	public BubbaMainPanel tabFactory(BubbaFrame frame, String tabType)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		return this.tabInformationHash.get(tabType).getTabClass()
				.getConstructor(BubbaGuiController.class, BubbaFrame.class).newInstance(this.controller, this);
	}

	/**
	 * Get the tabbed content pane.
	 *
	 * @return The tabbed content pane
	 */
	public BubbaDnDTabbedPane getTabbedPane() {
		return this.tabbedPane;
	}

	protected class TabInformation {

		private final Class<BubbaMainPanel>	tabClass;
		private final String				tabDescription;

		@SuppressWarnings("unchecked")
		public TabInformation(String tabDescription, Class<?> tabClass) {
			this.tabClass = (Class<BubbaMainPanel>) tabClass;
			this.tabDescription = tabDescription;
		}

		public Class<BubbaMainPanel> getTabClass() {
			return tabClass;
		}

		public String getTabDescription() {
			return tabDescription;
		}
	}

	public void newWindow(DropTargetDropEvent a_event, Point location) {
		this.controller.registerWindow(this);
	}

	public void updateGui(boolean forceUpdate) {
		super.updateGui(forceUpdate);

		// Propagate update to tabs
		while (this.tabbedPane == null) {
			System.out.println(this.getClass().getSimpleName() + "Can't update null tabbedPane!");
			try {
				Thread.sleep(50);
			} catch (final InterruptedException exception) {
			}
		}
		for (final String tabName : this.tabbedPane.getTabNames()) {
			final int index = this.tabbedPane.indexOfTab(tabName);
			final Component component = this.tabbedPane.getComponentAt(index);
			if (component instanceof BubbaMainPanel) {
				( (BubbaMainPanel) this.tabbedPane.getComponentAt(index) ).updateGui(forceUpdate);
			}
		}
	}

	/**
	 * Load all of the properties from the client and apply them.
	 */
	public void loadProperties() {
		super.loadProperties();

		if (this.tabbedPane == null) {
			return;
		}

		// Tell all of the tabs to reload the properties
		for (final String tabName : this.tabbedPane.getTabNames()) {
			final int index = this.tabbedPane.indexOfTab(tabName);
			final Component component = this.tabbedPane.getComponentAt(index);
			if (component instanceof BubbaMainPanel) {
				( (BubbaMainPanel) this.tabbedPane.getComponentAt(index) ).loadProperties();
			}
		}
	}

	/**
	 * Save properties.
	 */
	public void saveProperties() {
		super.saveProperties();
		final String id = this.getTitle();
		final String[] tabNames = this.tabbedPane.getTabNames();
		this.controller.getProperties().setProperty(id + "." + "OpenTabs", this.tabbedPane.getTabNames().toString());
		this.controller.getProperties().setProperty("Window" + id, Arrays.toString(tabNames));
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		String name = ( (Component) e.getSource() ).getName();

		switch (name) {
			case "Tabbed Pane":
				if (this.tabbedPane.getTabCount() == 1) {
					// If there are no tabs left, hide the frame
					this.setVisible(false);
					// Wait 100 ms to see if the tab is added back, then close if there are still no tabs
					final Timer timer = new Timer(100, new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if (!BubbaDragDropTabFrame.this.isVisible()) {
								BubbaDnDTabbedPane.unregisterTabbedPane(BubbaDragDropTabFrame.this.tabbedPane);
								BubbaDragDropTabFrame.this.controller.unregisterWindow(BubbaDragDropTabFrame.this);
								BubbaDragDropTabFrame.this.dispose();
							}
						}
					});
					timer.setRepeats(false);
					timer.start();
				} else {
					this.setVisible(true);
				}
				break;
			default:
				this.controller.log("Unknown state change registered in TriviaFrame");
		}
	}

	public void windowClosing(WindowEvent e) {
		super.windowClosing(e);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
	}


}
