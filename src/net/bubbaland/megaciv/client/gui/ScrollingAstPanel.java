package net.bubbaland.megaciv.client.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

import net.bubbaland.gui.BubbaGuiController;
import net.bubbaland.gui.BubbaPanel;
import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Game;

public class ScrollingAstPanel extends BubbaPanel {

	private static final long serialVersionUID = -1197287409680075891L;

	private enum Column {
		CIV, POPULATION, CITIES, VP, AST, AST01, AST02, AST03, AST04, AST05, AST06, AST07, AST08, AST09, AST10, AST11, AST12, AST13, AST14, AST15, AST16
	}

	private static final HashMap<Column, Civilization.SortOption>	sortHash	= new HashMap<Column, Civilization.SortOption>() {
																					private static final long serialVersionUID = -3473350095491262976L;

																					{
																						put(Column.AST,
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

	private final static HashMap<Column, ColumnData>				colData		= new HashMap<Column, ColumnData>() {
																					private static final long serialVersionUID = 1L;

																					{
																						put(Column.AST, new ColumnData(
																								"AST", 0));
																						put(Column.CIV, new ColumnData(
																								"Civ", 1));
																						put(Column.POPULATION,
																								new ColumnData("Pop",
																										2));
																						put(Column.CITIES,
																								new ColumnData("Cities",
																										3));
																						put(Column.VP, new ColumnData(
																								"VP", 4));
																						put(Column.AST01,
																								new ColumnData("", 5));
																						put(Column.AST02,
																								new ColumnData("", 6));
																						put(Column.AST03,
																								new ColumnData("", 7));
																						put(Column.AST04,
																								new ColumnData("", 8));
																						put(Column.AST05,
																								new ColumnData("", 9));
																						put(Column.AST06,
																								new ColumnData("", 10));
																						put(Column.AST07,
																								new ColumnData("", 11));
																						put(Column.AST08,
																								new ColumnData("", 12));
																						put(Column.AST09,
																								new ColumnData("", 13));
																						put(Column.AST10,
																								new ColumnData("", 14));
																						put(Column.AST11,
																								new ColumnData("", 15));
																						put(Column.AST12,
																								new ColumnData("", 16));
																						put(Column.AST13,
																								new ColumnData("", 17));
																						put(Column.AST14,
																								new ColumnData("", 18));
																						put(Column.AST15,
																								new ColumnData("", 19));
																						put(Column.AST16,
																								new ColumnData("", 20));
																					}
																				};


	private HashMap<Integer, RowPanel>								civRows;
	private HeaderPanel												headerPanel;

	private final GuiClient											client;

	private HashMap<Column, Integer>								width;
	private HashMap<Column, Float>									fontSize;
	private int														rowHeight;

	private final GuiController										controller;

	private Civilization.SortOption									sortOption;
	private Civilization.SortDirection								sortDirection;

	public ScrollingAstPanel(GuiClient client, GuiController controller) {
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
	}

	public synchronized void updateGui(boolean forceUpdate) {
		loadProperties();

		Game game = this.client.getGame();
		if (this.civRows == null || game.getNCivilizations() != this.civRows.size()) {
			this.redoRows(game.getCivilizationNames());
		}

		ArrayList<Civilization> sortedCivs = Civilization.sortBy(this.client.getGame().getCivilizations(),
				this.sortOption, this.sortDirection);

		if (sortedCivs.size() == 0 || this.headerPanel == null) {
			return;
		}

		Civilization firstCiv = sortedCivs.get(0);

		for (Civilization.Age age : Civilization.Age.values()) {
			int ageStart = firstCiv.getAgeStart(age);
			int ageEnd = this.client.getGame().lastAstStep();
			Civilization.Age nextAge = age.nextAge();
			if (nextAge != null) {
				ageEnd = firstCiv.getAgeStart(nextAge) - 1;
			}
			int diff = age == Civilization.Age.STONE ? ageEnd - ageStart : ageEnd - ageStart + 1;
			Color foregroundColor = this.controller.getAstForegroundColor(age);
			Color backgroundColor = this.controller.getAstBackgroundColor(age);

			BubbaPanel.setLabelProperties(this.headerPanel.getAgeHeader(age), width.get(Column.AST01) * diff,
					this.rowHeight, foregroundColor, backgroundColor, 14.0f);
		}

		for (Civilization civ : sortedCivs) {
			Civilization.Name name = civ.getName();
			RowPanel panel = this.civRows.get(sortedCivs.indexOf(civ));
			for (Column col : Column.values()) {
				JLabel label = panel.getLabel(col);
				String text = "";
				Color foregroundColor = this.controller.getCivForegroundColor(name);
				Color backgroundColor = this.controller.getCivBackgroundColor(name);
				switch (col) {
					case AST:
						text = civ.getAst() + "";
						break;
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
						if (astStep > civ.getAstPosition()) {
							foregroundColor = this.controller.getAstForegroundColor(civ.getAge(astStep));
							backgroundColor = this.controller.getAstBackgroundColor(civ.getAge(astStep));
						}
						// System.out.println(this.getClass().getSimpleName() + " " + col + " " + civ.getAge(astStep));
						label.setVisible(astStep <= this.client.getGame().lastAstStep());
				}
				label.setText(text);
				setLabelProperties(label, this.width.get(col), this.rowHeight, foregroundColor, backgroundColor,
						this.fontSize.get(col));
			}


		}
	}

	public void loadProperties() {
		Properties props = this.controller.getProperties();

		this.width = new HashMap<Column, Integer>();
		this.fontSize = new HashMap<Column, Float>();

		for (Column col : Column.values()) {
			int width;
			float fontSize;

			switch (col) {
				case CIV:
				case POPULATION:
				case CITIES:
				case AST:
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

		private static final long						serialVersionUID	= 810881884756701202L;

		private final HashMap<Column, JLabel>			colLabels;
		private final HashMap<Civilization.Age, JLabel>	ageLabels;

		public HeaderPanel(BubbaGuiController controller) {
			super(controller, new GridBagLayout());
			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.SOUTH;
			constraints.weighty = 1.0;
			constraints.gridy = 0;

			this.colLabels = new HashMap<Column, JLabel>();

			for (Column col : Column.values()) {
				constraints.weightx = 0.0;
				constraints.gridx = colData.get(col).getColumnLocation();

				String string = Game.capitalizeFirst(col.toString());

				int width = ScrollingAstPanel.this.width.get(col);
				int height = ScrollingAstPanel.this.rowHeight;

				float fontSize = 14.0f;

				Color foreground = Color.WHITE;
				Color background = Color.BLACK;

				int justification = JLabel.CENTER;

				switch (col) {
					case CIV:
						constraints.weightx = 1.0;
						string = "Civilization (Player)";
						justification = JLabel.LEFT;
						break;
					case POPULATION:
						string = "Pop";
						break;
					case CITIES:
						break;
					case AST:
						string = "AST";
						break;
					case VP:
						string = "VP";
						break;
					default:
						continue;
				}

				JLabel label = this.enclosedLabelFactory(string, width, height, foreground, background, constraints,
						fontSize, justification, JLabel.BOTTOM);
				this.colLabels.put(col, label);
				label.setName(col.toString());
				label.addMouseListener(this);
			}

			constraints.weightx = 0.0;

			this.ageLabels = new HashMap<Civilization.Age, JLabel>();
			for (Civilization.Age age : Civilization.Age.values()) {
				constraints.gridx = colData.get(Column.AST01).getColumnLocation() + age.ordinal();
				this.ageLabels.put(age, this.enclosedLabelFactory("", constraints, JLabel.LEFT, JLabel.BOTTOM));
			}

			this.updateGui(true);
		}

		public JLabel getAgeHeader(Civilization.Age age) {
			return this.ageLabels.get(age);
		}

		public void updateGui(boolean forceUpdate) {
			for (Column col : this.colLabels.keySet()) {
				if (ScrollingAstPanel.sortHash.get(col) == ScrollingAstPanel.this.sortOption) {
					switch (ScrollingAstPanel.this.sortDirection) {
						case ASCENDING:
							this.colLabels.get(col).setIcon(UP_ARROW);
							break;
						case DESCENDING:
							this.colLabels.get(col).setIcon(DOWN_ARROW);
							break;
					}
				} else {
					this.colLabels.get(col).setIcon(null);
				}
			}
			ScrollingAstPanel.this.updateGui(true);
		}


		@Override
		public void mouseClicked(MouseEvent e) {
			final String source = ( (JComponent) e.getSource() ).getName();
			Column col = Column.valueOf(source);
			if (ScrollingAstPanel.sortHash.get(col) == ScrollingAstPanel.this.sortOption) {
				switch (ScrollingAstPanel.this.sortDirection) {
					case ASCENDING:
						ScrollingAstPanel.this.sortDirection = Civilization.SortDirection.DESCENDING;
						break;
					case DESCENDING:
						ScrollingAstPanel.this.sortDirection = Civilization.SortDirection.ASCENDING;
						break;
				}
			} else {
				Civilization.SortOption newSort = ScrollingAstPanel.sortHash.get(col);
				if (newSort != null) {
					ScrollingAstPanel.this.sortOption = ScrollingAstPanel.sortHash.get(col);
					ScrollingAstPanel.this.sortDirection = Civilization.SortDirection.ASCENDING;
				}
			}
			this.updateGui(true);
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

			for (Column col : Column.values()) {
				int justification = JLabel.RIGHT;
				switch (col) {
					case CIV:
						justification = JLabel.LEFT;
						constraints.weightx = 1.0;
						break;
					default:
						constraints.weightx = 0.0;
				}
				constraints.gridx = colData.get(col).getColumnLocation();
				JLabel label = this.enclosedLabelFactory("", constraints, justification, JLabel.CENTER);
				setLabelProperties(label, 100, 20, Color.BLACK, Color.WHITE, (float) 14.0);
				switch (col) {
					case CIV:
					case POPULATION:
					case CITIES:
					case AST:
					case VP:
						label.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
						break;
					default:
						label.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
				}
				this.labels.put(col, label);
			}

			// this.setBorder(BorderFactory.createEmptyBorder());
			this.setBackground(Color.BLACK);
		}

		public JLabel getLabel(Column col) {
			return labels.get(col);
		}
	}

	private static class ColumnData {

		private final int colLocation;

		public ColumnData(String headerText, int colLocation) {
			this.colLocation = colLocation;
		}

		public int getColumnLocation() {
			return this.colLocation;
		}

	}

}
