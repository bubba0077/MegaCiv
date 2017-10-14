package net.bubbaland.megaciv.client.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import org.apache.commons.lang3.text.WordUtils;

import net.bubbaland.gui.BubbaDnDTabbedPane;
import net.bubbaland.gui.BubbaPanel;
import net.bubbaland.gui.LinkedLabelGroup;
import net.bubbaland.megaciv.client.GameClient;
import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Game;
import net.bubbaland.megaciv.messages.AdvanceAstMessage;
import net.bubbaland.megaciv.messages.UndoPurchaseMessage;

/**
 * Panel providing UI for civilization information and purchases. Displays summary information and AST progression for
 * each civilization. Allows sorting of table by AST, AST progression, population, cities, or victory points. Control to
 * open purchase dialog for each civilization. Context menus to undo purchases and show/edit civilization information.
 * Tooltips provide additional information.
 * 
 * @author Walter Kolczynski
 *
 */
public class AstTablePanel extends BubbaPanel {

	private static final long serialVersionUID = -1197287409680075891L;

	// Column names
	private enum Column {
		BUY, CIV, POPULATION, CITIES, TECHS, VP, AST01, AST02, AST03, AST04, AST05, AST06, AST07, AST08, AST09, AST10,
		AST11, AST12, AST13, AST14, AST15, AST16
	}

	// Map relating column names to the appropriate sort method
	private static final HashMap<Column, Civilization.SortOption>	sortHash	=
			new HashMap<Column, Civilization.SortOption>() {
																							private static final long serialVersionUID =
																									-3473350095491262976L;

																							{
																								put(Column.CIV,
																										Civilization.SortOption.AST);
																								put(Column.POPULATION,
																										Civilization.SortOption.MOVEMENT);
																								put(Column.CITIES,
																										Civilization.SortOption.CITIES);
																								// put(null,
																								// Civilization.SortOption.AST_POSITION
																								// );
																								put(Column.VP,
																										Civilization.SortOption.VP);
																								put(Column.AST01,
																										Civilization.SortOption.AST_POSITION);
																							}
																						};

	/** Sort icons */
	private static final ImageIcon									UP_ARROW	=
			new ImageIcon(BubbaPanel.class.getResource("images/upArrow.png"));
	private static final ImageIcon									DOWN_ARROW	=
			new ImageIcon(BubbaPanel.class.getResource("images/downArrow.png"));


	/**
	 * A filler image to fill the space when there is no game data.
	 * 
	 */
	private static BufferedImage									FILLER_IMAGE;
	static {
		try {
			FILLER_IMAGE = ImageIO.read(AstTablePanel.class.getResource("images/filler.png"));
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	private static Icon					X_ICON	=
			new ImageIcon(new ImageIcon(GuiClient.class.getResource("images/x.png")).getImage().getScaledInstance(20,
					20, Image.SCALE_SMOOTH));

	// Panel with column headers
	private Header						header;
	// Mapping between the row number and the panel holding civilization UI for that row
	private ArrayList<CivRow>			civRows;
	// Panel with an image to fill space when there is no game data
	private FillerPanel					fillerPanel;

	// Label groups restricted to using the same font size
	private final LinkedLabelGroup		civNameGroup, statGroup, astGroup;

	// Client with game data responsible for communication with server
	private final GameClient			client;

	// Default sizes
	private HashMap<Column, Integer>	width;
	private HashMap<Column, Float>		fontSize;
	private int							rowHeight;
	private boolean						isGameOver;

	// Master GUI controller
	private final GuiController			controller;

	// Fields specifying the current sort order
	private Civilization.SortOption		sortOption;
	private Civilization.SortDirection	sortDirection;

	/**
	 * Create a new AST Table panel.
	 * 
	 * @param client
	 *            Client with game data handling communication to game server.
	 * @param controller
	 *            Master GUI controller for this panel.
	 */
	public AstTablePanel(GameClient client, GuiController controller) {
		super(controller, new GridBagLayout());
		this.client = client;
		this.controller = controller;
		this.sortOption = Civilization.SortOption.AST;
		this.sortDirection = Civilization.SortDirection.DESCENDING;
		this.civRows = null;
		this.isGameOver = false;

		this.civNameGroup = new LinkedLabelGroup();
		this.statGroup = new LinkedLabelGroup();
		this.astGroup = new LinkedLabelGroup();

		// When the panel is resized, rescale fonts
		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				resizeFonts();
			}
		});

		// Set up layout constraints
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		constraints.gridx = 0;
		constraints.gridy = 0;

		// Add header panel
		this.header = new Header();

		// Add filler panel
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = Column.values().length;
		this.fillerPanel = new FillerPanel();
		this.add(this.fillerPanel, constraints);

		// Load and apply properties
		loadProperties();

		this.updateGui();
	}

	/**
	 * Rescale the fonts in each label group based on current label size.
	 */
	public void resizeFonts() {
		this.civNameGroup.resizeFonts();
		this.statGroup.resizeFonts();
		this.astGroup.resizeFonts();
	}

	/**
	 * Add or remove panels to match the current number of civilizations.
	 * 
	 * @param civNames
	 *            A list of civilization names currently in play.
	 */
	public synchronized void redoRows(ArrayList<Civilization.Name> civNames) {
		if (this.civRows != null) {
			// Remove existing panels
			this.civRows.forEach(row -> row.remove());
		}
		this.remove(this.fillerPanel);

		// Create a new panel for each civilization
		this.civRows = new ArrayList<CivRow>();
		for (Civilization.Name name : civNames) {
			int rowNumber = civNames.indexOf(name);
			CivRow row = new CivRow(rowNumber);
			this.civRows.add(row);
			row.loadProperties();
		}

		this.resizeFonts();
	}

	/**
	 * Update elements of the GUI.
	 * 
	 */
	public synchronized void updateGui() {
		Game game = this.client.getGame();
		if (game == null) {
			return;
		}

		// If the number of civs change, reinitialize the row panels
		if (this.civRows == null || game.getNCivilizations() != this.civRows.size()) {
			this.redoRows(game.getCivilizationNames());
		}

		if (!isGameOver && game.isGameOver()) {
			this.sortOption = Civilization.SortOption.VP;
			this.sortDirection = Civilization.SortDirection.DESCENDING;
		}

		this.isGameOver = game.isGameOver();

		// Sort civilization based on the current sort method
		ArrayList<Civilization> sortedCivs =
				Civilization.sortBy(this.client.getGame().getCivilizations(), this.sortOption, this.sortDirection);

		if (sortedCivs.size() == 0 || this.header == null) {
			return;
		}

		// Update the civ attached to each row
		this.civRows.forEach(row -> row.updateCiv(sortedCivs.get(row.getRowNumber()), sortedCivs));

		// Since how the ages line up on the AST table, need to update header so the ages match the first civ
		this.header.updateGui(sortedCivs.get(0));

		this.validate();
		this.resizeFonts();
	}

	/**
	 * Set properties based on the properties in the master controller.
	 */
	public void loadProperties() {
		Properties props = this.controller.getProperties();

		this.width = new HashMap<Column, Integer>();
		this.fontSize = new HashMap<Column, Float>();

		for (Column col : EnumSet.allOf(Column.class)) {
			int width;
			float fontSize;

			switch (col) {
				case CIV:
				case POPULATION:
				case CITIES:
				case TECHS:
				case BUY:
					// case AST:
				case VP:
					width = Integer.parseInt(props.getProperty("AstTable." + col + ".Width"));
					fontSize = Float.parseFloat(props.getProperty("AstTable." + col + ".FontSize"));
					break;
				default:
					width = Integer.parseInt(props.getProperty("AstTable.AstStep.Width"));
					fontSize = Float.parseFloat(props.getProperty("AstTable.AstStep.FontSize"));
			}

			this.width.put(col, width);
			this.fontSize.put(col, fontSize);
		}

		this.rowHeight = Integer.parseInt(props.getProperty("AstTable.Row.Height"));

		if (this.civRows != null) {
			this.civRows.forEach(row -> row.loadProperties());
		}

		this.header.loadProperties();

		this.updateGui();
	}

	/**
	 * Class to hold GUI elements that make up the header and listen for mouse clicks that will change the sorting.
	 * 
	 * @author Walter Kolczynski
	 *
	 */
	private class Header implements MouseListener {

		private final HashMap<Column, JLabel> colLabels;

		/**
		 * Create a new Header.
		 */
		public Header() {

			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.FIRST_LINE_START;
			constraints.weighty = 0.0;
			constraints.gridy = 0;

			this.colLabels = new HashMap<Column, JLabel>();

			for (Column col : EnumSet.allOf(Column.class)) {
				constraints.weightx = 0.0;
				constraints.gridx = col.ordinal();

				String string = WordUtils.capitalizeFully(col.toString());

				int justification = JLabel.CENTER;

				// Determine label text based on the column
				switch (col) {
					// case AST:
					// string = "AST";
					// break;
					case CIV:
						string = "Civilization (Player)";
						justification = JLabel.LEFT;
						constraints.weightx = 1.0;
						break;
					case POPULATION:
						string = "Pop";
						break;
					case CITIES:
						string = "Cities";
						break;
					case TECHS:
						string = "Adv";
						break;
					case VP:
						string = "VP";
						break;
					default:
						string = "";
						// constraints.weightx = 1.0;
				}

				JLabel label =
						AstTablePanel.this.enclosedLabelFactory(string, constraints, justification, JLabel.BOTTOM);
				this.colLabels.put(col, label);
				label.setName(col.toString());
				label.addMouseListener(this);
			}
		}

		/**
		 * Set properties based on the properties in the master controller.
		 */
		public void loadProperties() {
			Properties props = AstTablePanel.this.controller.getProperties();
			int height = Integer.parseInt(props.getProperty("AstTable.Header.Height"));

			Color foreground =
					new Color(new BigInteger(props.getProperty("AstTable.Header.ForegroundColor"), 16).intValue());
			Color background =
					new Color(new BigInteger(props.getProperty("AstTable.Header.BackgroundColor"), 16).intValue());

			float fontSize = Float.parseFloat(props.getProperty("AstTable.Header.FontSize"));

			for (Column col : EnumSet.allOf(Column.class)) {
				int width = AstTablePanel.this.width.get(col);
				JLabel label = this.colLabels.get(col);
				BubbaPanel.setLabelProperties(label, width, height, foreground, background, fontSize);
			}
		}

		/**
		 * Update elements of the GUI.
		 * 
		 */
		public void updateGui(Civilization firstCiv) {
			for (Column col : this.colLabels.keySet()) {
				JLabel label = this.colLabels.get(col);
				if (AstTablePanel.sortHash.get(col) == AstTablePanel.this.sortOption) {
					switch (AstTablePanel.this.sortDirection) {
						case DESCENDING:
							label.setIcon(DOWN_ARROW);
							break;
						case ASCENDING:
							label.setIcon(UP_ARROW);
							break;
					}
				} else {
					label.setIcon(null);
				}
				switch (col) {
					// case AST:
					case CIV:
					case POPULATION:
					case VP:
					case CITIES:
					case TECHS:
					case BUY:
						// Nothing in these columns need to change other than the sort icon
						continue;
					default:
						// All of the AST columns

				}

				int astStep = Integer.parseInt(col.toString().substring(3));
				Civilization.Age age = firstCiv.getAge(astStep);
				// Set the tooltip showing the requirements and requirement status for this AST step
				label.setToolTipText(age + ": " + Game.AGE_REQUIREMENTS
						.get(AstTablePanel.this.client.getGame().getDifficulty()).get(age).getText());
				// Set the color of this AST step based on the age and whether it has been gained
				label.getParent().setBackground(AstTablePanel.this.controller.getAstBackgroundColor(age));
				// Hide the final column if playing the basic game
				label.getParent().setVisible(astStep <= AstTablePanel.this.client.getGame().lastAstStep());
			}
		}


		@Override
		public void mouseClicked(MouseEvent e) {
			final String source = ( (JComponent) e.getSource() ).getName();
			Column col = Column.valueOf(source);
			if (AstTablePanel.sortHash.get(col) == AstTablePanel.this.sortOption) {
				// If the current sort column is clicked, reverse the sort order
				switch (AstTablePanel.this.sortDirection) {
					case DESCENDING:
						AstTablePanel.this.sortDirection = Civilization.SortDirection.ASCENDING;
						break;
					case ASCENDING:
						AstTablePanel.this.sortDirection = Civilization.SortDirection.DESCENDING;
						break;
				}
			} else {
				// Change the sort method to the clicked column
				Civilization.SortOption newSort = AstTablePanel.sortHash.get(col);
				if (newSort != null) {
					AstTablePanel.this.sortOption = AstTablePanel.sortHash.get(col);
					AstTablePanel.this.sortDirection = Civilization.SortDirection.DESCENDING;
				}
			}
			AstTablePanel.this.updateGui();
		}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseExited(MouseEvent e) {}

		@Override
		public void mousePressed(MouseEvent e) {}

		@Override
		public void mouseReleased(MouseEvent e) {}

	}

	/**
	 * Class to hold GUI elements that make up one row and handle actions triggered by UI elements there.
	 * 
	 * @author Walter Kolczynski
	 *
	 */
	private class CivRow implements ActionListener {

		private final HashMap<Column, JComponent>	components;
		private final JPopupMenu					contextMenu;
		private final JMenuItem						undoPurchaseMenuItem;
		private final JButton						buyButton;
		private final int							rowNumber;

		private Civilization.Name					name;

		/**
		 * Create a new CivRow.
		 * 
		 * @param rowNumber
		 *            Row where these GUI elements appear.
		 */
		public CivRow(int rowNumber) {
			this.rowNumber = rowNumber;

			/**
			 * Create a new context menu
			 */
			this.contextMenu = new JPopupMenu();
			AstTablePanel.this.add(this.contextMenu);

			JMenuItem viewItem = new JMenuItem("View");
			viewItem.setActionCommand("View");
			viewItem.addActionListener(this);
			this.contextMenu.add(viewItem);

			JMenuItem editItem = new JMenuItem("Edit");
			editItem.setActionCommand("Edit");
			editItem.addActionListener(this);
			this.contextMenu.add(editItem);

			JMenuItem regressItem = new JMenuItem("Regress");
			regressItem.setActionCommand("Regress");
			regressItem.addActionListener(this);
			this.contextMenu.add(regressItem);

			this.undoPurchaseMenuItem = new JMenuItem("Undo Purchase");
			this.undoPurchaseMenuItem.setActionCommand("Undo");
			this.undoPurchaseMenuItem.addActionListener(this);
			this.contextMenu.add(this.undoPurchaseMenuItem);

			// Create the buy button
			this.buyButton = new JButton("Buy");
			this.buyButton.setActionCommand("Buy");
			this.buyButton.setMargin(new Insets(0, 0, 0, 0));
			this.buyButton.addActionListener(this);

			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.FIRST_LINE_START;
			constraints.weighty = 1.0;
			constraints.gridy = this.rowNumber + 1;

			this.components = new HashMap<Column, JComponent>();

			/**
			 * Create the component associated with each column.
			 */
			for (Column col : EnumSet.allOf(Column.class)) {
				int justification = JLabel.RIGHT;
				constraints.gridx = col.ordinal();
				constraints.weightx = 0.0;
				switch (col) {
					case BUY:
						AstTablePanel.this.add(this.buyButton, constraints);
						this.components.put(col, this.buyButton);
						continue;
					case CIV:
						justification = JLabel.LEFT;
						constraints.weightx = 1.0;
						break;
					case CITIES:
					case POPULATION:
					case TECHS:
					case VP:
						constraints.weightx = 0.2;
						break;
					default:
						justification = JLabel.CENTER;
						constraints.weightx = 0.1;
				}
				JLabel label = AstTablePanel.this.enclosedLabelFactory("", constraints, justification, JLabel.CENTER);

				/**
				 * Add context menus and place labels in the appropriate label group to keep their fontsizes the same.
				 */
				switch (col) {
					case CIV:
						label.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
						label.addMouseListener(new PopupListener(this.contextMenu));
						AstTablePanel.this.civNameGroup.addLabel(label);
						break;
					case POPULATION:
					case CITIES:
					case TECHS:
					case VP:
						label.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
						label.addMouseListener(new PopupListener(this.contextMenu));
						AstTablePanel.this.statGroup.addLabel(label);
						break;
					default:
						label.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
						AstTablePanel.this.astGroup.addLabel(label);
				}
				this.components.put(col, label);
			}
		}

		/**
		 * Get the row number for this row.
		 * 
		 * @return The row number.
		 */
		private int getRowNumber() {
			return this.rowNumber;
		}


		/**
		 * Set properties based on the properties in the master controller.
		 */
		private void loadProperties() {
			for (Column col : EnumSet.allOf(Column.class)) {
				if (col == Column.BUY) {
					BubbaPanel.setButtonProperties(this.buyButton, AstTablePanel.this.width.get(col),
							AstTablePanel.this.rowHeight, null, null, AstTablePanel.this.fontSize.get(col));
					continue;
				}
			}
		}

		/**
		 * Remove all of the elements in this row from the panel.
		 */
		private void remove() {
			for (JComponent component : this.components.values()) {
				AstTablePanel.this.remove(component);
				if (component instanceof JLabel) {
					AstTablePanel.this.remove(component.getParent());
				}
			}
		}

		/**
		 * Change the civilization associated with this row.
		 * 
		 * @param civ
		 *            The new civilization to be associated with this row.
		 * @param allCivs
		 *            All civilizations in this game.
		 */
		private void updateCiv(Civilization civ, ArrayList<Civilization> allCivs) {
			this.name = civ.getName();

			// Only show the undo purchase item if this civ has made a purchase this turn.
			this.undoPurchaseMenuItem.setVisible(civ.hasPurchased());

			/**
			 * Update all of the information and colors to reflect the new civilization.
			 */
			for (Column col : EnumSet.allOf(Column.class)) {
				JComponent component = this.components.get(col);
				String text = "";
				Icon icon = null;
				Color foregroundColor = Game.FOREGROUND_COLORS.get(name);
				Color backgroundColor = Game.BACKGROUND_COLORS.get(name);
				switch (col) {
					// case AST:
					// text = civ.getAst() + "";
					// break;
					case POPULATION:
						text = String.format("%1$2d", civ.getPopulation());
						break;
					case CITIES:
						text = String.format("%1$2d", civ.getCityCount());
						break;
					case CIV:
						text = WordUtils.capitalizeFully(name.toString()) + " (" + civ.getPlayer() + ")";
						break;
					case TECHS:
						text = String.format("%1$2d", civ.getTechs().size());
						component.setToolTipText(civ.getTechBreakdownString());
						break;
					case VP:
						text = String.format("%1$3d", civ.getVP(allCivs));
						component.setToolTipText(civ.getVpBreakdownString());
						break;
					case BUY:
						this.buyButton
								.setEnabled(!civ.hasPurchased() && !AstTablePanel.this.client.getGame().isGameOver());
						continue;
					default:
						int astStep = Integer.parseInt(col.toString().substring(3));
						Civilization.Age age = civ.getAge(astStep);
						text = String.format("%1$02d", ( astStep * Game.VP_PER_AST_STEP ));
						if (astStep <= civ.getAstPosition()) {
							component.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
							foregroundColor = AstTablePanel.this.controller.getAstBackgroundColor(age);
							component.setToolTipText("");
							if (astStep > 0 && civ.passAstRequirements(civ.getAge(astStep - 1))
									&& !civ.passAstRequirements(age)) {}
						} else {
							component.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
							foregroundColor = AstTablePanel.this.controller.getAstBackgroundColor(age);
							backgroundColor = AstTablePanel.this.controller.getAstBackgroundColor(age);
							if (!civ.passAstRequirements(age) && ( civ.passAstRequirements(civ.getAge(astStep - 1))
									|| ( astStep - civ.getAstPosition() ) == 1 )) {
								foregroundColor = AstTablePanel.this.controller.getAstForegroundColor(age);
								text = "";
								icon = X_ICON;
							}
							component.setToolTipText(civ.astRequirementString(age, true));
						}
						component.setVisible(astStep <= AstTablePanel.this.client.getGame().lastAstStep());
						component.getParent().setVisible(astStep <= AstTablePanel.this.client.getGame().lastAstStep());
						component.setName(civ.toString() + " " + astStep);
				}
				JLabel label = (JLabel) component;
				label.setIcon(icon);
				label.setText(text);
				label.setForeground(foregroundColor);
				label.setBackground(backgroundColor);
				label.getParent().setBackground(backgroundColor);
			}
		}

		/**
		 * Handle events triggered by GUI elements in this row.
		 */
		@Override
		public void actionPerformed(ActionEvent event) {
			String command = event.getActionCommand();
			switch (command) {
				case "Buy": {
					// Open a tech purchase dialog
					new TechnologyStoreDialog(AstTablePanel.this.client, AstTablePanel.this.controller, this.name);
				}
					break;
				case "View":
					// Open a civilization view tab
					MegaCivFrame frame = ( (MegaCivFrame) SwingUtilities.getWindowAncestor(AstTablePanel.this) );
					BubbaDnDTabbedPane pane = frame.getTabbedPane();
					String tabName = WordUtils.capitalizeFully(this.name.toString());
					int index = pane.indexOfTab(tabName);

					if (index > -1) {
						// Switch to that tab
						pane.setSelectedIndex(index);
					} else {
						// Create that tab (and switch to it)
						( (MegaCivFrame) SwingUtilities.getWindowAncestor(AstTablePanel.this) ).addTab(tabName);
					}
					break;
				case "Edit":
					// Open an edit dialog
					new CivEditPanel(AstTablePanel.this.client, AstTablePanel.this.controller, this.name);
					break;
				case "Regress":
					// Create a dialog to confirm user wants to regress the civilization
					if (JOptionPane.showConfirmDialog(null,
							"Confirm regression of " + WordUtils.capitalizeFully(name.toString()),
							"Confirm regression of " + WordUtils.capitalizeFully(name.toString()),
							JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
						AstTablePanel.this.client.sendMessage(
								new AdvanceAstMessage(new HashMap<Civilization.Name, Civilization.AstChange>() {
									private static final long serialVersionUID = -5493408662716651786L;

									{
										put(CivRow.this.name, Civilization.AstChange.REGRESS);
									}
								}));
					}
					break;
				case "Undo":
					// Undo tech purchase for this round and reactivate button
					AstTablePanel.this.client.sendMessage(new UndoPurchaseMessage(CivRow.this.name));
					break;
				default:
					// This code should never execute
					AstTablePanel.this.client.log("ActionCommand " + command + " not implemented in "
							+ this.getClass().getSimpleName() + " yet!");
			}

		}

		/**
		 * Class to create context menus.
		 * 
		 * @author Walter Kolczynski
		 *
		 */
		private class PopupListener extends MouseAdapter {

			private final JPopupMenu menu;

			public PopupListener(JPopupMenu menu) {
				this.menu = menu;
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				this.checkForPopup(e);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				this.checkForPopup(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				this.checkForPopup(e);
			}

			private void checkForPopup(MouseEvent event) {
				final JComponent source = (JComponent) event.getSource();
				if (event.isPopupTrigger()) {
					this.menu.show(source, event.getX(), event.getY());
				}
			}

		}
	}

	/**
	 * A panel that contains a single image that is scaled to fill the entire width of the panel.
	 * 
	 * @author Walter Kolczynski
	 *
	 */
	private class FillerPanel extends BubbaPanel {

		private static final long	serialVersionUID	= 1832956209036106492L;

		private int					width;
		private Image				scaledImage;

		/**
		 * Create a new panel
		 */
		public FillerPanel() {
			super(AstTablePanel.this.controller);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (width != this.getWidth()) {
				// Rescale the image to fill the entire width of the panel
				this.width = this.getWidth();
				double scale = ( width + 0.0 ) / FILLER_IMAGE.getWidth();
				this.scaledImage = FILLER_IMAGE.getScaledInstance(width, (int) ( FILLER_IMAGE.getHeight() * scale ),
						Image.SCALE_SMOOTH);
			}
			g.drawImage(this.scaledImage, 0, 0, this);
		}

	}
}
