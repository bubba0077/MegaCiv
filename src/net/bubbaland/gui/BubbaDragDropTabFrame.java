package net.bubbaland.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class BubbaDragDropTabFrame extends BubbaFrame implements ChangeListener {

	/**
	 * 
	 */
	private static final long					serialVersionUID	= -8926818996029674620L;

	/** List of available tabs and associated descriptions. */
	protected Hashtable<String, TabInformation>	tabInformationHash;
	protected final BubbaDnDTabbedPane			tabbedPane;
	// Field specifying whether we should save which tabs are open when closing
	private boolean								saveTabs;

	/**
	 * @param saveTabs
	 *            the saveTabs to set
	 */
	protected void setSaveTabs(boolean saveTabs) {
		this.saveTabs = saveTabs;
	}

	/**
	 * Creates a new frame with specified tabs.
	 *
	 * @param gui
	 *            The GUI controller for this frame
	 * @param initialTabs
	 *            Tabs to open initially
	 */
	public BubbaDragDropTabFrame(BubbaGuiController controller, String[] initialTabs) {
		this(controller);
		this.addTabs(initialTabs);
		this.tabbedPane.setSelectedIndex(0);
		this.tabbedPane.addChangeListener(this);
		this.loadPosition();
	}

	public BubbaDragDropTabFrame(BubbaGuiController controller) {
		super(controller);
		this.initTabInfoHash();
		this.saveTabs = true;

		// Create drag & drop tabbed pane
		this.tabbedPane = new BubbaDnDTabbedPane(controller, this);
		this.tabbedPane.addChangeListener(this);
		this.tabbedPane.setName("Tabbed Pane");
		this.tabbedPane.setDoubleBuffered(true);

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

	public BubbaDragDropTabFrame deriveNewFrame() {
		return new BubbaDragDropTabFrame(this.controller);
	}

	public void addTab(String tabName) {
		try {
			// Remove (#) from tab name duplicates
			BubbaMainPanel newTab = this.tabFactory(tabName.replaceFirst(" \\([0-9]*\\)", ""));
			this.tabbedPane.addTab(tabName, newTab);
			this.tabbedPane.setSelectedComponent(newTab);
			newTab.updateGui();
		} catch (IllegalArgumentException | SecurityException exception) {
			exception.printStackTrace();
		}
	}

	public void addTabs(String[] tabs) {
		for (final String tabName : tabs) {
			this.addTab(tabName);
		}
	}

	/**
	 * Initializes the tab information hash, which maps tab names to tab descriptions.
	 */
	protected void initTabInfoHash() {
		this.tabInformationHash = new Hashtable<String, TabInformation>();
	}

	/**
	 * Get a list of available tab names.
	 *
	 * @return The available tab names
	 */
	public ArrayList<String> getTabNames() {
		return new ArrayList<String>(this.tabInformationHash.keySet());
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

	/**
	 * Creates a new tab that contains a BubbaMainPanel and returns the panel.
	 * 
	 * @param tabType
	 * 
	 * @return
	 */
	private BubbaMainPanel tabFactory(String tabType) {
		TabInformation tabInfo = this.tabInformationHash.get(tabType);
		try {
			return tabInfo.getTabClass().getConstructor(tabInfo.getArgumentClasses())
					.newInstance(tabInfo.getArguments());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException exception) {
			exception.printStackTrace();
		}
		return null;
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
		private final Class<?>[]			argumentClasses;
		private final Object[]				arguments;

		@SuppressWarnings("unchecked")
		public TabInformation(String tabDescription, Class<? extends BubbaMainPanel> tabClass,
				Class<?>[] argumentClasses, Object[] arguments) {
			this.tabClass = (Class<BubbaMainPanel>) tabClass;
			this.tabDescription = tabDescription;
			this.argumentClasses = argumentClasses;
			this.arguments = arguments;
		}

		public Class<?>[] getArgumentClasses() {
			return this.argumentClasses;
		}

		public Object[] getArguments() {
			return this.arguments;
		}

		public Class<BubbaMainPanel> getTabClass() {
			return tabClass;
		}

		public String getTabDescription() {
			return tabDescription;
		}
	}

	public void updateGui() {
		super.updateGui();

		// Propagate update to current tab
		while (this.tabbedPane == null) {
			System.out.println(this.getClass().getSimpleName() + "Can't update null tabbedPane!");
			try {
				Thread.sleep(50);
			} catch (final InterruptedException exception) {}
		}
		Component tab = this.tabbedPane.getSelectedComponent();
		if (tab instanceof BubbaMainPanel) {
			( (BubbaMainPanel) tab ).updateGui();
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
		if (this.saveTabs) {
			this.controller.getProperties().setProperty("Window." + id + ".OpenTabs",
					String.join(", ", ( (BubbaDragDropTabFrame) this ).tabbedPane.getTabNames()));
		}
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
				this.updateGui();
				break;
			default:
				this.controller.setStatusBarText("Unknown state change registered in TriviaFrame");
		}
	}

	public void windowClosing(WindowEvent e) {
		super.windowClosing(e);
	}

}
