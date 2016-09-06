package net.bubbaland.megaciv.client.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.bubbaland.gui.BubbaMainPanel;
import net.bubbaland.gui.LinkedLabelGroup;
import net.bubbaland.megaciv.client.GameClient;
import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.TradeCardSet;
import net.bubbaland.megaciv.game.TradeStack;
import net.bubbaland.megaciv.game.Civilization.Region;
import net.bubbaland.megaciv.game.Game;
import net.bubbaland.megaciv.game.TradeStack.TradeCard;
import net.bubbaland.megaciv.game.TradeStack.TradeGood;

public class TradeCardPanel extends BubbaMainPanel {

	private static final long	serialVersionUID	= 7562478662505466778L;

	private static final int	N_COLUMNS			= 2;

	private enum Column {
		STACK, WEST, SHARED, EAST, CALAMITIES
	};

	private GameClient									client;
	private HashMap<Integer, HashMap<Column, JPanel>>	panels;
	private LinkedLabelGroup							stackNumberGroup, labelGroup;

	public TradeCardPanel(GuiClient client, GuiController controller, MegaCivFrame frame) {
		super(controller, frame);
		this.client = client;

		this.panels = new HashMap<Integer, HashMap<Column, JPanel>>();
		this.stackNumberGroup = new LinkedLabelGroup();
		this.labelGroup = new LinkedLabelGroup();

		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				validate();
				resizeFonts();
			}
		});

		frame.getTabbedPane().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				validate();
				resizeFonts();
			}
		});

		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		constraints.gridx = 0;
		constraints.gridy = 0;

		final GridBagConstraints constraints2 = new GridBagConstraints();
		constraints2.fill = GridBagConstraints.BOTH;
		constraints2.weightx = 1.0;
		constraints2.weighty = 1.0;
		constraints2.gridx = 0;
		constraints2.gridy = 0;

		int nCols = Column.values().length;

		for (int c = 0; c < N_COLUMNS; c++) {
			for (Column col : Column.values()) {
				constraints.gridx = col.ordinal() + c * nCols;
				if (col == Column.STACK) {
					constraints.weightx = 0.0;
				} else {
					constraints.weightx = 1.0;
				}

				JLabel label = new JLabel(col.name(), JLabel.CENTER);
				this.add(label, constraints);
			}
		}

		constraints.weightx = 0.0;
		constraints.weighty = 0.0;
		constraints.gridx = nCols;
		constraints.gridy = 1; // ( TradeCardSet.N_STACKS + 1 - 1 ) / N_COLUMNS + 1;
		constraints.gridwidth = nCols;
		StopwatchPanel clockPanel = new StopwatchPanel(client, controller);
		clockPanel.setPreferredSize(new Dimension(0, 0));
		this.add(clockPanel, constraints);

		constraints.weighty = 1.0;
		constraints.gridwidth = 1;

		for (int r = 1; r <= TradeCardSet.N_STACKS; r++) {
			this.panels.put(r, new HashMap<Column, JPanel>());
			constraints.gridy = r == 1 ? 1 : r / N_COLUMNS + 1;

			for (Column col : Column.values()) {
				constraints.gridx = r == 1 ? col.ordinal() : col.ordinal() + ( r % N_COLUMNS ) * nCols;
				if (col == Column.STACK) {
					constraints.weightx = 0.0;
				} else {
					constraints.weightx = 1.0;
				}
				JPanel panel = new JPanel();
				panel.setLayout(new GridBagLayout());
				panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
				if (col == Column.STACK) {
					JLabel label = new JLabel(r + "", JLabel.CENTER);
					this.stackNumberGroup.addLabel(label);
					panel.add(label, constraints2);
				}
				this.panels.get(r).put(col, panel);
				this.add(panel, constraints);
			}
		}
	}

	public void resizeFonts() {
		this.stackNumberGroup.resizeFonts();
		this.labelGroup.resizeFonts();
	}


	public void updateGui(boolean forceUpdate) {
		Game game = this.client.getGame();
		if (game == null) {
			return;
		}
		TradeCardSet stacks = game.getTradeCards();
		ArrayList<Region> regions = new ArrayList<Region>();
		regions.add(this.client.getGame().getRegion());
		if (this.client.getGame().getRegion() == Region.BOTH) {
			regions.add(Region.WEST);
			regions.add(Region.EAST);
		}

		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.gridx = 0;
		constraints.gridy = 0;

		this.labelGroup = new LinkedLabelGroup();

		for (int r = 1; r <= TradeCardSet.N_STACKS; r++) {
			HashMap<TradeGood, HashMap<Region, Integer>> goods = stacks.getStack(r).getGoods();
			ArrayList<TradeStack.Calamity> calamities = stacks.getStack(r).getCalamities();

			HashMap<Civilization.Region, ArrayList<TradeCard>> stackDivision =
					new HashMap<Civilization.Region, ArrayList<TradeCard>>();
			for (Region region : regions) {
				stackDivision.put(region, new ArrayList<TradeCard>());
			}

			for (TradeCard card : goods.keySet()) {
				if (goods.get(card).keySet().size() > 1) {
					stackDivision.get(Region.BOTH).add(card);
				} else {
					stackDivision.get(goods.get(card).keySet().toArray()[0]).add(card);
				}
			}

			for (Column col : Column.values()) {
				JPanel panel = this.panels.get(r).get(col);
				switch (col) {
					case STACK:
						break;
					case WEST:
						panel.removeAll();
						Region region = Region.valueOf(col.name());
						if (!regions.contains(region)) {
							continue;
						}
						constraints.gridy = 0;
						for (TradeCard card : stackDivision.get(region)) {
							int quantity = goods.get(card).get(region);
							JLabel label = new JLabel(card.toString() + " " + quantity, JLabel.LEFT);
							this.labelGroup.addLabel(label);
							panel.add(label, constraints);
							constraints.gridy = constraints.gridy + 1;
						}
						break;
					case EAST:
						panel.removeAll();
						region = Region.valueOf(col.name());
						if (!regions.contains(region)) {
							continue;
						}
						constraints.gridy = 0;
						for (TradeCard card : stackDivision.get(region)) {
							int quantity = goods.get(card).get(region);
							JLabel label = new JLabel(card.toString() + " " + quantity, JLabel.RIGHT);
							this.labelGroup.addLabel(label);
							panel.add(label, constraints);
							constraints.gridy = constraints.gridy + 1;
						}
						break;
					case SHARED:
						panel.removeAll();
						if (!regions.contains(Region.BOTH)) {
							continue;
						}
						constraints.gridy = 0;
						for (TradeCard card : stackDivision.get(Region.BOTH)) {
							int westQuantity = goods.get(card).get(Region.WEST);
							int eastQuantity = goods.get(card).get(Region.EAST);
							JLabel label = new JLabel(westQuantity + " " + card.toString() + " " + eastQuantity,
									JLabel.CENTER);
							this.labelGroup.addLabel(label);
							panel.add(label, constraints);
							constraints.gridy = constraints.gridy + 1;
						}
						break;
					case CALAMITIES:
						panel.removeAll();
						constraints.gridy = 0;
						for (TradeStack.Calamity calamity : calamities) {
							JLabel label = new JLabel(calamity.toString(), JLabel.CENTER);
							this.labelGroup.addLabel(label);
							panel.add(label, constraints);
							constraints.gridy = constraints.gridy + 1;
						}
				}
			}
		}
		this.validate();
		this.resizeFonts();
	}

	protected void loadProperties() {
		// TODO Auto-generated method stub

	}

}
