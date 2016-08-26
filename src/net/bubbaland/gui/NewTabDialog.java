package net.bubbaland.gui;

import java.awt.GridBagConstraints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Hashtable;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import org.apache.commons.lang3.text.WordUtils;

import net.bubbaland.megaciv.game.Civilization;

/**
 * Creates a dialog that allows the user to select a tab to be added to the tabbed pane.
 *
 * The dialog provides a combo box, a text area to describe the selected tab, and three option buttons: "Add", "Add All"
 * , and "Cancel". "Add" will add the selected tab. "Add All" will add all non-starred items to the pane that are not
 * already open on the pane. "Cancel" will close the dialog with no further action.
 *
 * @author Walter Kolczynski
 *
 */
public class NewTabDialog extends BubbaDialogPanel implements ItemListener {

	private static final long			serialVersionUID	= -6388311089354721920L;

	// GUI elements to monitor/update
	private final JComboBox<String>		tabSelector;
	private final JTextArea				descriptionLabel;
	private final BubbaDragDropTabFrame	parent;

	// Get the list of tab names and sort them
	private final ArrayList<String>		tabNameSet;
	private final String[]				tabNames;

	public NewTabDialog(BubbaGuiController controller, BubbaDragDropTabFrame frame) {
		super(controller);

		this.parent = frame;

		this.tabNameSet = this.parent.getTabNames();
		this.tabNames = new String[this.tabNameSet.size()];
		tabNameSet.toArray(this.tabNames);
		Arrays.sort(this.tabNames, new TabComparator());

		// Set up layout constraints
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;

		// Create the tab selector
		constraints.gridx = 0;
		constraints.gridy = 0;
		this.tabSelector = new JComboBox<String>(this.tabNames);
		this.tabSelector.setFont(this.tabSelector.getFont().deriveFont(textAreaFontSize));
		this.add(this.tabSelector, constraints);
		this.tabSelector.addItemListener(this);

		// Create the description area
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.gridx = 0;
		constraints.gridy = 1;
		this.descriptionLabel = this.scrollableTextAreaFactory(this.parent.getTabDescription(tabNames[0]), 300, 200,
				this.getForeground(), this.getBackground(), constraints, textAreaFontSize,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.descriptionLabel.setEditable(false);

		// Options
		final String[] options = { "Add", "Add All", "Cancel" };

		// Display the dialog box
		this.dialog = new BubbaDialog(this.parent.getGui(), "Add tab", this, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.YES_NO_CANCEL_OPTION, null, options);
		this.dialog.setName("Add Tab");
		this.dialog.setVisible(true);
	}

	/**
	 * Selection in combo box changed, update the description.
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		final String tabName = (String) this.tabSelector.getSelectedItem();
		final String description = this.parent.getTabDescription(tabName);
		this.descriptionLabel.setText(description);
	}

	/**
	 * Customer comparator to sort tabs in the appropriate order.
	 *
	 * @author Walter Kolczynski
	 *
	 */
	public static class TabComparator implements Comparator<String> {

		final static private Hashtable<String, Integer> SORT_ORDER;

		static {
			SORT_ORDER = new Hashtable<String, Integer>(0);
			SORT_ORDER.put("AST", 0);
			SORT_ORDER.put("Trade", 1);
			for (Civilization.Name name : EnumSet.allOf(Civilization.Name.class)) {
				SORT_ORDER.put(WordUtils.capitalizeFully(name.toString()), name.ordinal() + 1);
			}
		}

		@Override
		public int compare(String o1, String o2) {
			return SORT_ORDER.get(o1).compareTo(SORT_ORDER.get(o2));
		}
	}

	@Override
	public void windowClosed(WindowEvent event) {
		super.windowClosed(event);
		final BubbaDnDTabbedPane pane = this.parent.getTabbedPane();

		// If a button was not pressed (option isn't a string), do nothing
		if (!( this.dialog.getValue() instanceof String )) return;
		final String option = (String) this.dialog.getValue();
		// A list of tab names to add
		final ArrayList<String> newTabs = new ArrayList<String>(0);
		switch (option) {
			// Add the selected tab to the list
			case "Add": {
				newTabs.add((String) this.tabSelector.getSelectedItem());
				break;
			}
			// Add all tabs that don't start with a * and are not already in the tabbed pane
			case "Add All":
				for (final String tabName : tabNameSet) {
					if (!tabName.startsWith("*") && pane.indexOfTab(tabName) == -1) {
						newTabs.add(tabName);
					}
				}
				break;
			default:
				return;
		}
		// Add all the tabs in the list to the tabbed pane
		newTabs.sort(new TabComparator());
		for (String tabName : newTabs) {
			// Remove leading star now, since we don't want it in the tab name
			if (tabName.startsWith("*")) {
				tabName = tabName.replaceFirst("\\*", "");
			}
			// If there is already a copy of the tab, iterate the tab name
			String altName = tabName;
			int i = 1;
			while (pane.indexOfTab(altName) > -1) {
				altName = tabName + " (" + i + ")";
				i++;
			}
			// Add the tab to the tabbed pane
			BubbaMainPanel newTab = null;
			try {
				newTab = this.parent.tabFactory(this.parent, tabName);
			} catch (IllegalArgumentException | SecurityException exception) {
				// TODO Auto-generated catch block
				exception.printStackTrace();
			}
			this.parent.getTabbedPane().addTab(altName, newTab);
			newTab.updateGui(true);
			// Make the new tab the selected one
			final int tabLocation = pane.indexOfTab(altName);
			pane.setSelectedIndex(tabLocation);
		}


	}


}
