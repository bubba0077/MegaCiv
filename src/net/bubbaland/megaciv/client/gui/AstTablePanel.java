package net.bubbaland.megaciv.client.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import net.bubbaland.gui.BubbaGuiController;
import net.bubbaland.gui.BubbaPanel;
import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Game;

public class AstTablePanel extends BubbaPanel {

	private static final long serialVersionUID = -1197287409680075891L;

	private enum Column {
		CIV, POPULATION, CITIES, VP, AST01, AST02, AST03, AST04, AST05, AST06, AST07, AST08, AST09, AST10, AST11, AST12, AST13, AST14, AST15, AST16
	}

	private static final HashMap<Column, Civilization.SortOption>	sortHash	= new HashMap<Column, Civilization.SortOption>() {
																					private static final long serialVersionUID = -3473350095491262976L;

																					{
																						put(Column.CIV,
																								Civilization.SortOption.AST);
																						put(Column.POPULATION,
																								Civilization.SortOption.POPULATION);
																						put(Column.CITIES,
																								Civilization.SortOption.CITIES);
																						// put(null,
																						// Civilization.SortOption.AST_POSITION
																						// );
																						put(Column.VP,
																								Civilization.SortOption.VP);
																					}
																				};

	/** Sort icons */
	private static final ImageIcon									UP_ARROW	= new ImageIcon(
			BubbaPanel.class.getResource("images/upArrow.png"));
	private static final ImageIcon									DOWN_ARROW	= new ImageIcon(
			BubbaPanel.class.getResource("images/downArrow.png"));

	private HashMap<Integer, RowPanel>								civRows;
	private HeaderPanel												headerPanel;

	private final GuiClient											client;

	private HashMap<Column, Integer>								width;
	private HashMap<Column, Float>									fontSize;
	private int														rowHeight;

	private final GuiController										controller;

	private Civilization.SortOption									sortOption;
	private Civilization.SortDirection								sortDirection;

	public AstTablePanel(GuiClient client, GuiController controller) {
		super(controller, new GridBagLayout());
		this.client = client;
		this.controller = controller;
		this.sortOption = Civilization.SortOption.AST;
		this.sortDirection = Civilization.SortDirection.ASCENDING;
		this.civRows = null;

		// Set up layout constraints
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;

		loadProperties();

		this.headerPanel = new HeaderPanel(this.controller);
		this.add(this.headerPanel, constraints);

		this.updateGui(true);
	}

	public synchronized void redoRows(ArrayList<Civilization.Name> civNames) {
		if (this.civRows != null) {
			for (RowPanel panel : this.civRows.values()) {
				this.remove(panel);
			}
		}
		// Set up layout constraints
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		constraints.gridx = 0;

		this.civRows = new HashMap<Integer, RowPanel>();
		for (Civilization.Name name : civNames) {
			RowPanel panel = new RowPanel(this.controller);
			constraints.gridy = civNames.indexOf(name) + 1;
			this.civRows.put(civNames.indexOf(name), panel);
			this.add(panel, constraints);
		}

		constraints.weighty = 1.0;
		constraints.gridy = civNames.size() + 2;
		this.add(new JPanel(), constraints);
	}

	public synchronized void updateGui(boolean forceUpdate) {
		Game game = this.client.getGame();
		if (game == null) {
			return;
		}

		if (this.civRows == null || game.getNCivilizations() != this.civRows.size()) {
			this.redoRows(game.getCivilizationNames());
		}

		ArrayList<Civilization> sortedCivs = Civilization.sortBy(this.client.getGame().getCivilizations(),
				this.sortOption, this.sortDirection);

		if (sortedCivs.size() == 0 || this.headerPanel == null) {
			return;
		}

		for (Civilization civ : sortedCivs) {
			Civilization.Name name = civ.getName();
			RowPanel panel = this.civRows.get(sortedCivs.indexOf(civ));
			for (Column col : EnumSet.allOf(Column.class)) {
				JLabel label = panel.getLabel(col);
				String text = "";
				Color foregroundColor = Civilization.FOREGROUND_COLORS.get(name);
				Color backgroundColor = Civilization.BACKGROUND_COLORS.get(name);
				switch (col) {
					// case AST:
					// text = civ.getAst() + "";
					// break;
					case POPULATION:
						text = civ.getPopulation() + "";
						break;
					case CITIES:
						text = civ.getCityCount() + "";
						break;
					case CIV:
						text = Game.capitalizeFirst(name.toString()) + " (" + civ.getPlayer() + ")";
						break;
					case VP:
						text = civ.getVP() + "";
						break;
					default:
						text = "";
						int astStep = Integer.parseInt(col.toString().substring(3));
						label.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
						if (astStep > civ.getAstPosition()) {
							label.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
							foregroundColor = this.controller.getAstForegroundColor(civ.getAge(astStep));
							backgroundColor = this.controller.getAstBackgroundColor(civ.getAge(astStep));
						}
						label.setVisible(astStep <= this.client.getGame().lastAstStep());
				}
				label.setText(text);
				setLabelProperties(label, this.width.get(col), this.rowHeight, foregroundColor, backgroundColor,
						this.fontSize.get(col));
			}


		}

		this.headerPanel.updateGui(forceUpdate, sortedCivs.get(0));
	}

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

	}

	private class HeaderPanel extends BubbaPanel implements MouseListener {

		private static final long				serialVersionUID	= 810881884756701202L;

		private final HashMap<Column, JLabel>	colLabels;
		// private final HashMap<Civilization.Age, JLabel> ageLabels;

		public HeaderPanel(BubbaGuiController controller) {
			super(controller, new GridBagLayout());
			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			// constraints.anchor = GridBagConstraints.SOUTH;
			constraints.weighty = 1.0;
			constraints.gridy = 0;

			this.colLabels = new HashMap<Column, JLabel>();

			for (Column col : EnumSet.allOf(Column.class)) {
				constraints.weightx = 0.0;
				constraints.gridx = col.ordinal();

				String string = Game.capitalizeFirst(col.toString());

				int width = AstTablePanel.this.width.get(col);
				int height = AstTablePanel.this.rowHeight;

				float fontSize = 14.0f;

				Color foreground = Color.WHITE;
				Color background = Color.BLACK;

				int justification = JLabel.CENTER;

				switch (col) {
					// case AST:
					// string = "AST";
					// break;
					case CIV:
						string = "Civilization (Player)";
						justification = JLabel.LEFT;
						break;
					case POPULATION:
						string = "Pop";
						break;
					case CITIES:
						break;
					case VP:
						string = "VP";
						break;
					default:
						string = "";
						background = Color.RED;
						constraints.weightx = 1.0;
				}

				JLabel label = this.enclosedLabelFactory(string, width, height, foreground, background, constraints,
						fontSize, justification, JLabel.BOTTOM);
				this.colLabels.put(col, label);
				label.setName(col.toString());
				label.addMouseListener(this);
			}
		}

		public void updateGui(boolean forceUpdate, Civilization firstCiv) {
			for (Column col : this.colLabels.keySet()) {
				if (AstTablePanel.sortHash.get(col) == AstTablePanel.this.sortOption) {
					switch (AstTablePanel.this.sortDirection) {
						case ASCENDING:
							this.colLabels.get(col).setIcon(DOWN_ARROW);
							break;
						case DESCENDING:
							this.colLabels.get(col).setIcon(UP_ARROW);
							break;
					}
				} else {
					this.colLabels.get(col).setIcon(null);
				}
				switch (col) {
					// case AST:
					case CIV:
					case POPULATION:
					case VP:
					case CITIES:
						continue;
					default:
				}

				int astStep = Integer.parseInt(col.toString().substring(3));
				Civilization.Age age = firstCiv.getAge(astStep);
				this.colLabels.get(col).getParent()
						.setBackground(AstTablePanel.this.controller.getAstBackgroundColor(age));
			}
		}


		@Override
		public void mouseClicked(MouseEvent e) {
			final String source = ( (JComponent) e.getSource() ).getName();
			Column col = Column.valueOf(source);
			if (AstTablePanel.sortHash.get(col) == AstTablePanel.this.sortOption) {
				switch (AstTablePanel.this.sortDirection) {
					case ASCENDING:
						AstTablePanel.this.sortDirection = Civilization.SortDirection.DESCENDING;
						break;
					case DESCENDING:
						AstTablePanel.this.sortDirection = Civilization.SortDirection.ASCENDING;
						break;
				}
			} else {
				Civilization.SortOption newSort = AstTablePanel.sortHash.get(col);
				if (newSort != null) {
					AstTablePanel.this.sortOption = AstTablePanel.sortHash.get(col);
					AstTablePanel.this.sortDirection = Civilization.SortDirection.ASCENDING;
				}
			}
			AstTablePanel.this.updateGui(true);
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

	}

	private class RowPanel extends BubbaPanel {

		private static final long				serialVersionUID	= 1L;

		private final HashMap<Column, JLabel>	labels;

		public RowPanel(BubbaGuiController controller) {
			super(controller, new GridBagLayout());
			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.CENTER;
			constraints.weighty = 1.0;
			constraints.gridy = 0;

			this.labels = new HashMap<Column, JLabel>();

			for (Column col : EnumSet.allOf(Column.class)) {
				int justification = JLabel.RIGHT;
				constraints.weightx = 0.0;
				switch (col) {
					case CIV:
						justification = JLabel.LEFT;
						break;
					case CITIES:
						justification = JLabel.CENTER;
						break;
					// case AST:
					case POPULATION:
					case VP:
						break;
					default:
						constraints.weightx = 1.0;
				}
				constraints.gridx = col.ordinal();
				JLabel label = this.enclosedLabelFactory("", constraints, justification, JLabel.CENTER);
				setLabelProperties(label, 100, 20, Color.BLACK, Color.WHITE, (float) 14.0);
				switch (col) {
					case CIV:
					case POPULATION:
					case CITIES:
						// case AST:
					case VP:
						label.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
						break;
					default:
						label.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
				}
				this.labels.put(col, label);
			}

			this.setBorder(BorderFactory.createEmptyBorder());
		}

		public JLabel getLabel(Column col) {
			return labels.get(col);
		}
	}

}
