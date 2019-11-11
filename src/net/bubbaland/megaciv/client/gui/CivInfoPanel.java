package net.bubbaland.megaciv.client.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.apache.commons.lang3.text.WordUtils;

import net.bubbaland.gui.BubbaDnDTabbedPane;
import net.bubbaland.gui.BubbaMainPanel;
import net.bubbaland.gui.BubbaPanel;
import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Game;
import net.bubbaland.megaciv.game.Technology;
import net.bubbaland.megaciv.game.Technology.Type;

public class CivInfoPanel extends BubbaMainPanel {

	private static final long		serialVersionUID	= -7597920983496498119L;

	private final GuiClient			client;
	private final Civilization.Name	name;
	private final MegaCivFrame		frame;

	private final HeaderPanel		headerPanel;
	private final StatPanel			statPanel;
	private final TechPanel			techPanel;

	public CivInfoPanel(final GuiClient client, final GuiController controller, final MegaCivFrame frame,
			final Civilization.Name name) {
		super(controller, frame);
		this.client = client;
		this.name = name;
		this.frame = frame;

		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		constraints.gridx = 0;
		constraints.gridy = 0;

		this.headerPanel = new HeaderPanel();
		this.add(this.headerPanel, constraints);

		constraints.gridy = 1;
		this.statPanel = new StatPanel();
		this.add(this.statPanel, constraints);

		constraints.weighty = 1.0;
		constraints.gridy = 2;
		this.techPanel = new TechPanel();
		this.add(this.techPanel, constraints);

		constraints.gridy = 3;
		this.add(new JPanel(), constraints);

		this.loadProperties();
	}

	private class HeaderPanel extends BubbaPanel {

		private static final long	serialVersionUID	= 8854884285398596201L;

		private final JLabel		civNameLabel, ageLabel;

		public HeaderPanel() {
			super(CivInfoPanel.this.controller, new GridBagLayout());

			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.weightx = 1.0;
			constraints.weighty = 1.0;

			constraints.gridx = 0;
			constraints.gridy = 0;
			this.civNameLabel = this.enclosedLabelFactory("", constraints, SwingConstants.LEFT, SwingConstants.BOTTOM);
			constraints.weightx = 0.0;

			constraints.gridx = 1;
			constraints.gridy = 0;
			this.ageLabel = this.enclosedLabelFactory("", constraints, SwingConstants.RIGHT, SwingConstants.BOTTOM);
		}

		public void updateGui() {
			final Game game = CivInfoPanel.this.client.getGame();
			if (game == null) {
				return;
			}
			final Civilization civ = game.getCivilization(CivInfoPanel.this.name);

			this.civNameLabel.setText(
					WordUtils.capitalizeFully(CivInfoPanel.this.name.toString()) + " (" + civ.getPlayer() + ")");
			this.ageLabel.setText(civ.getCurrentAge().toString() + " (" + civ.getAstPosition() + ")");
		}

		public void loadProperties() {
			final Properties prop = CivInfoPanel.this.controller.getProperties();

			final Color foreground = Game.FOREGROUND_COLORS.get(CivInfoPanel.this.name);
			final Color background = Game.BACKGROUND_COLORS.get(CivInfoPanel.this.name);

			final int height = Integer.parseInt(prop.getProperty("CivInfoPanel.Header.Height"));

			BubbaPanel.setLabelProperties(this.civNameLabel,
					Integer.parseInt(prop.getProperty("CivInfoPanel.CivName.Width")), height, foreground, background,
					Float.parseFloat(prop.getProperty("CivInfoPanel.CivName.FontSize")));

			BubbaPanel.setLabelProperties(this.ageLabel, Integer.parseInt(prop.getProperty("CivInfoPanel.Age.Width")),
					height, foreground, background, Float.parseFloat(prop.getProperty("CivInfoPanel.Age.FontSize")));

		}

	}

	private class StatPanel extends BubbaPanel {

		private static final long						serialVersionUID	= 737717239222757041L;

		private final JLabel							populationLabel, cityLabel, vpLabel, techLabel;
		private final JLabel							populationLabel0, cityLabel0, vpLabel0, techLabel0;
		private final JLabel							creditLabel0;
		private final HashMap<Technology.Type, JLabel>	creditLabels, creditLabelsTop;
		// private final JLabel techDetailLabel0, techDetailL1Label0, techDetailL2Label0,
		// techDetailL3Label0, techDetailL1Label, techDetailL2Label, techDetailL3Label;

		public StatPanel() {
			super(CivInfoPanel.this.controller, new GridBagLayout());

			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.weightx = 1.0;
			constraints.weighty = 1.0;

			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.gridheight = 1;
			this.cityLabel0 =
					this.enclosedLabelFactory("Cities", constraints, SwingConstants.CENTER, SwingConstants.TOP);

			constraints.gridx = 0;
			constraints.gridy = 1;
			constraints.gridheight = 3;
			this.cityLabel = this.enclosedLabelFactory("", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

			constraints.gridx = 1;
			constraints.gridy = 0;
			constraints.gridheight = 1;
			this.populationLabel0 =
					this.enclosedLabelFactory("Pop", constraints, SwingConstants.CENTER, SwingConstants.TOP);

			constraints.gridx = 1;
			constraints.gridy = 1;
			constraints.gridheight = 3;
			this.populationLabel =
					this.enclosedLabelFactory("", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

			constraints.gridx = 2;
			constraints.gridy = 0;
			constraints.gridheight = 1;
			this.techLabel0 = this.enclosedLabelFactory("Adv", constraints, SwingConstants.CENTER, SwingConstants.TOP);

			constraints.gridx = 2;
			constraints.gridy = 1;
			constraints.gridheight = 3;
			this.techLabel = this.enclosedLabelFactory("", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

			constraints.gridx = 3;
			constraints.gridy = 0;
			constraints.gridheight = 1;
			this.vpLabel0 = this.enclosedLabelFactory("VP", constraints, SwingConstants.CENTER, SwingConstants.TOP);

			constraints.gridx = 3;
			constraints.gridy = 1;
			constraints.gridheight = 3;
			this.vpLabel = this.enclosedLabelFactory("", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

			// constraints.gridx = 4;
			// constraints.gridy = 0;
			// constraints.gridwidth = 2;
			// constraints.gridheight = 1;
			// constraints.weightx = 0.5;
			// constraints.weighty = 0.5;
			// this.techDetailLabel0 = this.enclosedLabelFactory("Techs", constraints, JLabel.CENTER, JLabel.CENTER);
			// constraints.gridwidth = 1;
			//
			// constraints.gridx = 4;
			// constraints.gridy = 1;
			// constraints.gridheight = 1;
			// this.techDetailL1Label0 = this.enclosedLabelFactory("1 VP", constraints, JLabel.LEFT, JLabel.CENTER);
			//
			// constraints.gridx = 4;
			// constraints.gridy = 2;
			// constraints.gridheight = 1;
			// this.techDetailL2Label0 = this.enclosedLabelFactory("3 VP", constraints, JLabel.LEFT, JLabel.CENTER);
			//
			// constraints.gridx = 4;
			// constraints.gridy = 3;
			// constraints.gridheight = 1;
			// this.techDetailL3Label0 = this.enclosedLabelFactory("6 VP", constraints, JLabel.LEFT, JLabel.CENTER);
			//
			// constraints.gridx = 5;
			// constraints.gridy = 1;
			// constraints.gridheight = 1;
			// this.techDetailL1Label = this.enclosedLabelFactory("", constraints, JLabel.RIGHT, JLabel.CENTER);
			//
			// constraints.gridx = 5;
			// constraints.gridy = 2;
			// constraints.gridheight = 1;
			// this.techDetailL2Label = this.enclosedLabelFactory("", constraints, JLabel.RIGHT, JLabel.CENTER);
			//
			// constraints.gridx = 5;
			// constraints.gridy = 3;
			// constraints.gridheight = 1;
			// this.techDetailL3Label = this.enclosedLabelFactory("", constraints, JLabel.RIGHT, JLabel.CENTER);

			constraints.gridx = 4;
			constraints.gridy = 1;
			constraints.gridheight = 3;
			constraints.weightx = 1.0;
			constraints.weighty = 1.0;
			this.creditLabel0 =
					this.enclosedLabelFactory("Credits:", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

			this.creditLabelsTop = new HashMap<Technology.Type, JLabel>();
			this.creditLabels = new HashMap<Technology.Type, JLabel>();
			for (final Technology.Type type : EnumSet.allOf(Technology.Type.class)) {
				constraints.gridx = 5 + type.ordinal();
				constraints.gridy = 0;
				constraints.gridheight = 1;
				this.creditLabelsTop.put(type, this.enclosedLabelFactory(WordUtils.capitalizeFully(type.toString()),
						constraints, SwingConstants.CENTER, SwingConstants.TOP));
				constraints.gridheight = 3;
				constraints.gridy = 1;
				this.creditLabels.put(type,
						this.enclosedLabelFactory("", constraints, SwingConstants.CENTER, SwingConstants.CENTER));
			}
		}

		public void updateGui() {
			final Game game = CivInfoPanel.this.client.getGame();
			if (game == null) {
				return;
			}
			final Civilization civ = game.getCivilization(CivInfoPanel.this.name);

			this.populationLabel.setText(String.format("%02d", civ.getPopulation()));
			this.cityLabel.setText(civ.getCityCount() + "");
			this.techLabel.setText(String.format("%02d", civ.getTechs().size()));

			final String text = String.format("%03d", civ.getVP());
			this.vpLabel.setText(text);

			// this.techDetailL1Label.setText(civ.getTechCountByVP(1) + "");
			// this.techDetailL2Label.setText(civ.getTechCountByVP(3) + "");
			// this.techDetailL3Label.setText(civ.getTechCountByVP(6) + "");

			for (final Technology.Type type : EnumSet.allOf(Technology.Type.class)) {
				this.creditLabels.get(type).setText(civ.getTypeCredit(type) + "");
			}


		}

		public void loadProperties() {
			final Properties props = CivInfoPanel.this.controller.getProperties();

			final Color foreground =
					new Color(new BigInteger(props.getProperty("CivInfoPanel.Stat.Foreground"), 16).intValue());
			final Color background =
					new Color(new BigInteger(props.getProperty("CivInfoPanel.Stat.Background"), 16).intValue());

			final int heightTop = Integer.parseInt(props.getProperty("CivInfoPanel.Stat.Top.Height"));
			final int heightBottom = Integer.parseInt(props.getProperty("CivInfoPanel.Stat.Bottom.Height"));

			// int techLeftWidth = Integer.parseInt(props.getProperty("CivInfoPanel.Stat.Tech.Left.Width"));
			// int techRightWidth = Integer.parseInt(props.getProperty("CivInfoPanel.Stat.Tech.Right.Width"));
			final int creditWidth = Integer.parseInt(props.getProperty("CivInfoPanel.Credit.Width"));

			// float techFontSize = Float.parseFloat(props.getProperty("CivInfoPanel.Stat.Tech.FontSize"));

			final float creditFontSizeTop = Float.parseFloat(props.getProperty("CivInfoPanel.Credit.Top.FontSize"));
			final float creditFontSize = Float.parseFloat(props.getProperty("CivInfoPanel.Credit.FontSize"));

			this.setBackground(background);

			BubbaPanel.setLabelProperties(this.populationLabel,
					Integer.parseInt(props.getProperty("CivInfoPanel.Population.Width")), heightBottom, foreground,
					background, Float.parseFloat(props.getProperty("CivInfoPanel.Population.FontSize")));

			BubbaPanel.setLabelProperties(this.cityLabel,
					Integer.parseInt(props.getProperty("CivInfoPanel.City.Width")), heightBottom, foreground,
					background, Float.parseFloat(props.getProperty("CivInfoPanel.City.FontSize")));

			BubbaPanel.setLabelProperties(this.techLabel,
					Integer.parseInt(props.getProperty("CivInfoPanel.Stat.Tech.Width")), heightBottom, foreground,
					background, Float.parseFloat(props.getProperty("CivInfoPanel.Stat.Tech.FontSize")));

			BubbaPanel.setLabelProperties(this.vpLabel, Integer.parseInt(props.getProperty("CivInfoPanel.VP.Width")),
					heightBottom, foreground, background,
					Float.parseFloat(props.getProperty("CivInfoPanel.VP.FontSize")));

			BubbaPanel.setLabelProperties(this.populationLabel0,
					Integer.parseInt(props.getProperty("CivInfoPanel.Population.Width")), heightTop, foreground,
					background, Float.parseFloat(props.getProperty("CivInfoPanel.Population.Top.FontSize")));

			BubbaPanel.setLabelProperties(this.cityLabel0,
					Integer.parseInt(props.getProperty("CivInfoPanel.City.Width")), heightTop, foreground, background,
					Float.parseFloat(props.getProperty("CivInfoPanel.City.Top.FontSize")));

			BubbaPanel.setLabelProperties(this.techLabel0,
					Integer.parseInt(props.getProperty("CivInfoPanel.Stat.Tech.Width")), heightTop, foreground,
					background, Float.parseFloat(props.getProperty("CivInfoPanel.Stat.Tech.Top.FontSize")));

			BubbaPanel.setLabelProperties(this.vpLabel0, Integer.parseInt(props.getProperty("CivInfoPanel.VP.Width")),
					heightTop, foreground, background,
					Float.parseFloat(props.getProperty("CivInfoPanel.VP.Top.FontSize")));

			// BubbaPanel.setLabelProperties(this.techDetailLabel0, techLeftWidth + techRightWidth, heightTop,
			// foreground,
			// background, techFontSize);
			//
			// BubbaPanel.setLabelProperties(this.techDetailL1Label0, techLeftWidth, heightBottom / 3, foreground,
			// background, techFontSize);
			//
			// BubbaPanel.setLabelProperties(this.techDetailL2Label0, techLeftWidth, heightBottom / 3, foreground,
			// background, techFontSize);
			//
			// BubbaPanel.setLabelProperties(this.techDetailL3Label0, techLeftWidth, heightBottom / 3, foreground,
			// background, techFontSize);
			//
			// BubbaPanel.setLabelProperties(this.techDetailL1Label, techRightWidth, heightBottom / 3, foreground,
			// background, techFontSize);
			//
			// BubbaPanel.setLabelProperties(this.techDetailL2Label, techRightWidth, heightBottom / 3, foreground,
			// background, techFontSize);
			//
			// BubbaPanel.setLabelProperties(this.techDetailL3Label, techRightWidth, heightBottom / 3, foreground,
			// background, techFontSize);

			BubbaPanel.setLabelProperties(this.creditLabel0,
					Integer.parseInt(props.getProperty("CivInfoPanel.Credit0.Width")), heightBottom, foreground,
					background, Float.parseFloat(props.getProperty("CivInfoPanel.Credit0.FontSize")));

			for (final Technology.Type type : EnumSet.allOf(Technology.Type.class)) {
				BubbaPanel.setLabelProperties(this.creditLabelsTop.get(type), creditWidth, heightTop, type.getColor(),
						background, creditFontSizeTop);
				BubbaPanel.setLabelProperties(this.creditLabels.get(type), creditWidth, heightBottom, type.getColor(),
						background, creditFontSize);
			}
		}

	}

	private class TechPanel extends BubbaPanel {

		private static final long					serialVersionUID	= 8750082449037521031L;

		private final HashMap<Technology, JLabel>	techLabels;
		private Color								ownedColor, unownedColor;

		private final int							N_ROWS				= 13;

		public TechPanel() {
			super(CivInfoPanel.this.controller, new GridBagLayout());

			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.weightx = 1.0;
			constraints.weighty = 1.0;

			this.techLabels = new HashMap<Technology, JLabel>();

			for (final Technology tech : EnumSet.allOf(Technology.class)) {
				constraints.gridx = 0 + tech.ordinal() / this.N_ROWS;
				constraints.gridy = 0 + tech.ordinal() % this.N_ROWS;

				String techString = "<html>" + tech.getName() + " (" + tech.getBaseCost() + ")";
				for (final Type type : tech.getTypes()) {
					techString = techString + " <img height=\"20\" width=\"20\" align=\"bottom\" src=\""
							+ GuiClient.class.getResource("images/" + type.toString() + ".png") + "\" alt=\""
							+ type.toString() + "\">";
				}
				techString = techString + "</html>";

				final JLabel label =
						this.enclosedLabelFactory(techString, constraints, SwingConstants.LEFT, SwingConstants.CENTER);
				label.setToolTipText("<html><img src=\""
						+ GuiClient.class.getResource("images/advances/" + tech.toString() + ".png") + "\"></html>");
				this.techLabels.put(tech, label);
			}

		}

		public void updateGui() {
			final Game game = CivInfoPanel.this.client.getGame();
			if (game == null) {
				return;
			}
			final Civilization civ = game.getCivilization(CivInfoPanel.this.name);

			final ArrayList<Technology> ownedTechs = civ.getTechs();

			for (final Technology tech : EnumSet.allOf(Technology.class)) {
				final Color color = ownedTechs.contains(tech) ? this.ownedColor : this.unownedColor;
				this.techLabels.get(tech).setForeground(color);
			}


		}

		public void loadProperties() {
			final Properties prop = CivInfoPanel.this.controller.getProperties();

			this.ownedColor = new Color(new BigInteger(prop.getProperty("CivInfoPanel.Tech.Owned"), 16).intValue());
			this.unownedColor = new Color(new BigInteger(prop.getProperty("CivInfoPanel.Tech.Unowned"), 16).intValue());
			final Color background =
					new Color(new BigInteger(prop.getProperty("CivInfoPanel.Tech.Background"), 16).intValue());

			final int height = Integer.parseInt(prop.getProperty("CivInfoPanel.Tech.Height"));
			final int width = Integer.parseInt(prop.getProperty("CivInfoPanel.Tech.Width"));

			final float fontSize = Float.parseFloat(prop.getProperty("CivInfoPanel.Tech.FontSize"));

			this.setBackground(background);

			for (final Technology tech : EnumSet.allOf(Technology.class)) {
				BubbaPanel.setLabelProperties(this.techLabels.get(tech), width, height, this.unownedColor, background,
						fontSize);
			}
			this.updateGui();
		}
	}

	@Override
	public void updateGui() {
		if (!this.client.getGame().getCivilizationNames().contains(this.name)) {
			final BubbaDnDTabbedPane pane = this.frame.getTabbedPane();
			pane.removeTabAt(pane.indexOfComponent(this));
			return;
		}

		this.headerPanel.updateGui();
		this.statPanel.updateGui();
		this.techPanel.updateGui();

		final BubbaDnDTabbedPane tabbedPane = this.frame.getTabbedPane();
		final int index = tabbedPane.indexOfComponent(this);

		this.frame.getTabbedPane().setForegroundAt(index, Game.FOREGROUND_COLORS.get(this.name));
		this.frame.getTabbedPane().setBackgroundAt(index, Game.BACKGROUND_COLORS.get(this.name));
	}

	@Override
	protected void loadProperties() {
		this.headerPanel.loadProperties();
		this.statPanel.loadProperties();
		this.techPanel.loadProperties();
	}

}
