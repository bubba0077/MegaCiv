package net.bubbaland.megaciv.client.gui;

import java.awt.Color;
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
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import net.bubbaland.gui.BubbaMainPanel;
import net.bubbaland.gui.LinkedLabelGroup;
import net.bubbaland.megaciv.client.GameClient;
import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Civilization.Region;
import net.bubbaland.megaciv.game.Game;
import net.bubbaland.megaciv.game.TradeCardSet;
import net.bubbaland.megaciv.game.TradeStack;
import net.bubbaland.megaciv.game.TradeStack.TradeCard;
import net.bubbaland.megaciv.game.TradeStack.TradeGood;

public class TradeCardPanel extends BubbaMainPanel {

	private static final long	serialVersionUID	= 7562478662505466778L;

	private static final int	N_COLUMNS			= 2;

	private enum ColumnType {
		STACK, WEST, SHARED, EAST, CALAMITIES
	};

	private final GameClient									client;
	private final HashMap<Integer, JLabel>						headers;
	private final HashMap<Integer, HashMap<ColumnType, JPanel>>	panels;
	private final LinkedLabelGroup								stackNumberGroup;

	private LinkedLabelGroup									labelGroup;

	public TradeCardPanel(final GuiClient client, final GuiController controller, final MegaCivFrame frame) {
		super(controller, frame);
		this.client = client;

		this.panels = new HashMap<Integer, HashMap<ColumnType, JPanel>>();
		this.stackNumberGroup = new LinkedLabelGroup();
		this.labelGroup = new LinkedLabelGroup(0);

		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				TradeCardPanel.this.validate();
				TradeCardPanel.this.resizeFonts();
			}
		});

		frame.getTabbedPane().addChangeListener(arg0 -> {
			TradeCardPanel.this.validate();
			TradeCardPanel.this.resizeFonts();
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

		final int nColTypes = ColumnType.values().length;

		/**
		 * Create the headers
		 */
		this.headers = new HashMap<Integer, JLabel>();
		for (int c = 0; c < N_COLUMNS; c++) {
			for (final ColumnType col : ColumnType.values()) {
				constraints.gridx = col.ordinal() + c * nColTypes;
				if (col == ColumnType.STACK) {
					constraints.weightx = 0.0;
				} else {
					constraints.weightx = 1.0;
				}

				final JLabel label = new JLabel(col.name(), SwingConstants.CENTER);
				this.headers.put(constraints.gridx, label);
				this.add(label, constraints);
			}
		}

		/**
		 * Create stopwatch panel
		 */
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;
		constraints.gridx = nColTypes;
		constraints.gridy = 1; // ( TradeCardSet.N_STACKS + 1 - 1 ) / N_COLUMNS + 1;
		constraints.gridwidth = nColTypes;
		final StopwatchPanel clockPanel = new StopwatchPanel(client, controller);
		clockPanel.setPreferredSize(new Dimension(0, 0));
		this.add(clockPanel, constraints);

		constraints.weighty = 1.0;
		constraints.gridwidth = 1;

		/**
		 * Fill the other spaces
		 */
		final int nRows = (int) Math.ceil(TradeCardSet.N_STACKS / ( N_COLUMNS + 0.0 ));
		for (int stack = 1; stack <= TradeCardSet.N_STACKS; stack++) {
			this.panels.put(stack, new HashMap<ColumnType, JPanel>());
			final int xOffset = ( stack - 2 ) / ( nRows - 1 );
			constraints.gridy = stack == 1 ? 1 : ( ( stack - 2 ) % ( nRows - 1 ) ) + 2;

			for (final ColumnType col : ColumnType.values()) {
				constraints.gridx = stack == 1 ? col.ordinal() : col.ordinal() + xOffset * nColTypes;
				if (col == ColumnType.STACK) {
					constraints.weightx = 0.0;
				} else {
					constraints.weightx = 1.0;
				}
				final JPanel panel = new JPanel();
				panel.setLayout(new GridBagLayout());
				panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
				if (col == ColumnType.STACK) {
					final JLabel label = new JLabel(stack + "", SwingConstants.CENTER);
					this.stackNumberGroup.addLabel(label);
					panel.add(label, constraints2);
				}
				this.panels.get(stack).put(col, panel);
				this.add(panel, constraints);
			}
		}
	}

	public void resizeFonts() {
		this.stackNumberGroup.resizeFonts();
		this.labelGroup.resizeFonts();
	}


	@Override
	public void updateGui() {
		final Game game = this.client.getGame();
		if (game == null) {
			return;
		}
		final TradeCardSet stacks = game.getTradeCards();
		final ArrayList<Region> regions = new ArrayList<Region>();
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
			final HashMap<TradeGood, HashMap<Region, Integer>> goods = stacks.getStack(r).getGoods();
			final ArrayList<TradeStack.Calamity> calamities = stacks.getStack(r).getCalamities();

			final HashMap<Civilization.Region, ArrayList<TradeCard>> stackDivision =
					new HashMap<Civilization.Region, ArrayList<TradeCard>>();
			for (final Region region : regions) {
				stackDivision.put(region, new ArrayList<TradeCard>());
			}

			for (final TradeCard card : goods.keySet()) {
				if (goods.get(card).keySet().size() > 1) {
					stackDivision.get(Region.BOTH).add(card);
				} else {
					stackDivision.get(goods.get(card).keySet().toArray()[0]).add(card);
				}
			}

			for (final ColumnType col : ColumnType.values()) {
				final JPanel panel = this.panels.get(r).get(col);
				final int c = ( (GridBagLayout) this.getLayout() ).getConstraints(panel).gridx;
				final JLabel header = this.headers.get(c);
				switch (col) {
					case STACK:
						break;
					case WEST: {
						panel.removeAll();
						final Region region = Region.valueOf(col.name());
						final boolean visible = regions.contains(region);
						panel.setVisible(visible);
						if (header != null) {
							header.setVisible(visible);
						}
						if (!visible) {
							continue;
						}
						constraints.gridy = 0;
						for (final TradeCard card : stackDivision.get(region)) {
							final int quantity = goods.get(card).get(region);
							final JLabel label =
									new JLabel(card.toString() + " (" + quantity + ")", SwingConstants.LEFT);
							this.labelGroup.addLabel(label);
							panel.add(label, constraints);
							constraints.gridy = constraints.gridy + 1;
						}
						break;
					}
					case EAST: {
						panel.removeAll();
						final Region region = Region.valueOf(col.name());
						final boolean visible = regions.contains(region);
						panel.setVisible(visible);
						if (header != null) {
							header.setVisible(visible);
						}
						if (!visible) {
							continue;
						}
						constraints.gridy = 0;
						for (final TradeCard card : stackDivision.get(region)) {
							final int quantity = goods.get(card).get(region);
							final JLabel label =
									new JLabel(card.toString() + " (" + quantity + ")", SwingConstants.RIGHT);
							this.labelGroup.addLabel(label);
							panel.add(label, constraints);
							constraints.gridy = constraints.gridy + 1;
						}
						break;
					}
					case SHARED: {
						panel.removeAll();
						final boolean visible = regions.contains(Region.BOTH);
						panel.setVisible(visible);
						if (header != null) {
							header.setVisible(visible);
						}
						if (!visible) {
							continue;
						}
						constraints.gridy = 0;
						for (final TradeCard card : stackDivision.get(Region.BOTH)) {
							final int westQuantity = goods.get(card).get(Region.WEST);
							final int eastQuantity = goods.get(card).get(Region.EAST);

							constraints.gridx = 0;
							JLabel label = new JLabel("(" + westQuantity + ")", SwingConstants.LEFT);
							this.labelGroup.addLabel(label);
							panel.add(label, constraints);

							constraints.gridx = 1;
							label = new JLabel(card.toString(), SwingConstants.CENTER);
							this.labelGroup.addLabel(label);
							panel.add(label, constraints);

							constraints.gridx = 2;
							label = new JLabel("(" + eastQuantity + ")", SwingConstants.RIGHT);
							this.labelGroup.addLabel(label);
							panel.add(label, constraints);

							constraints.gridy = constraints.gridy + 1;
						}
						break;
					}
					case CALAMITIES:
						panel.removeAll();
						constraints.gridy = 0;
						for (final TradeStack.Calamity calamity : calamities) {
							final JLabel label = new JLabel(calamity.toString(), SwingConstants.CENTER);
							this.labelGroup.addLabel(label);
							label.setToolTipText(calamity.toHtmlString());
							label.setForeground(Color.RED);
							panel.add(label, constraints);
							constraints.gridy = constraints.gridy + 1;
						}
				}
			}
		}
		this.validate();
		this.resizeFonts();
	}

	@Override
	protected void loadProperties() {
		// TODO Auto-generated method stub

	}

}
