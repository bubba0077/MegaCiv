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

import org.apache.commons.lang3.text.WordUtils;

import net.bubbaland.gui.BubbaDnDTabbedPane;
import net.bubbaland.gui.BubbaMainPanel;
import net.bubbaland.gui.BubbaPanel;
import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Game;
import net.bubbaland.megaciv.game.Technology;
import net.bubbaland.megaciv.game.Civilization.Age;
import net.bubbaland.megaciv.game.Technology.Type;

public class CivInfoPanel extends BubbaMainPanel {

	private static final long		serialVersionUID	= -7597920983496498119L;

	private final GuiClient			client;
	private final Civilization.Name	name;
	private final MegaCivFrame		frame;

	private final HeaderPanel		headerPanel;
	private final StatPanel			statPanel;
	private final TechPanel			techPanel;

	public CivInfoPanel(GuiClient client, GuiController controller, MegaCivFrame frame, Civilization.Name name) {
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
			this.civNameLabel = this.enclosedLabelFactory("", constraints, JLabel.LEFT, JLabel.BOTTOM);
			constraints.weightx = 0.0;

			constraints.gridx = 1;
			constraints.gridy = 0;
			this.ageLabel = this.enclosedLabelFactory("", constraints, JLabel.RIGHT, JLabel.BOTTOM);
		}

		public void updateGui(boolean forceUpdate) {
			Game game = CivInfoPanel.this.client.getGame();
			if (game == null) {
				return;
			}
			Civilization civ = game.getCivilization(name);

			this.civNameLabel.setText(WordUtils.capitalizeFully(name.toString()) + " (" + civ.getPlayer() + ")");
			this.ageLabel.setText(civ.getCurrentAge().toString() + " (" + civ.getAstPosition() + ")");
		}

		public void loadProperties() {
			Properties prop = CivInfoPanel.this.controller.getProperties();

			Color foreground = Civilization.FOREGROUND_COLORS.get(name);
			Color background = Civilization.BACKGROUND_COLORS.get(name);

			int height = Integer.parseInt(prop.getProperty("CivInfoPanel.Header.Height"));

			BubbaPanel.setLabelProperties(this.civNameLabel,
					Integer.parseInt(prop.getProperty("CivInfoPanel.CivName.Width")), height, foreground, background,
					Float.parseFloat(prop.getProperty("CivInfoPanel.CivName.FontSize")));

			BubbaPanel.setLabelProperties(this.ageLabel, Integer.parseInt(prop.getProperty("CivInfoPanel.Age.Width")),
					height, foreground, background, Float.parseFloat(prop.getProperty("CivInfoPanel.Age.FontSize")));

		}

	}

	private class StatPanel extends BubbaPanel {

		private static final long						serialVersionUID	= 737717239222757041L;

		private final JLabel							populationLabel, cityLabel, vpLabel;
		private final JLabel							populationLabel0, cityLabel0, vpLabel0;
		private final JLabel							creditLabel0;
		private final HashMap<Technology.Type, JLabel>	creditLabels, creditLabelsTop;

		public StatPanel() {
			super(CivInfoPanel.this.controller, new GridBagLayout());

			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.weightx = 1.0;
			constraints.weighty = 1.0;

			constraints.gridx = 1;
			constraints.gridy = 0;
			this.populationLabel0 = this.enclosedLabelFactory("Pop", constraints, JLabel.CENTER, JLabel.TOP);

			constraints.gridx = 1;
			constraints.gridy = 1;
			this.populationLabel = this.enclosedLabelFactory("", constraints, JLabel.CENTER, JLabel.CENTER);

			constraints.gridx = 0;
			constraints.gridy = 0;
			this.cityLabel0 = this.enclosedLabelFactory("Cities", constraints, JLabel.CENTER, JLabel.TOP);

			constraints.gridx = 0;
			constraints.gridy = 1;
			this.cityLabel = this.enclosedLabelFactory("", constraints, JLabel.CENTER, JLabel.CENTER);

			constraints.gridx = 2;
			constraints.gridy = 0;
			this.vpLabel0 = this.enclosedLabelFactory("VP", constraints, JLabel.CENTER, JLabel.TOP);

			constraints.gridx = 2;
			constraints.gridy = 1;
			this.vpLabel = this.enclosedLabelFactory("", constraints, JLabel.CENTER, JLabel.CENTER);


			constraints.gridx = 3;
			constraints.gridy = 1;
			this.creditLabel0 = this.enclosedLabelFactory("Credits:", constraints, JLabel.RIGHT, JLabel.CENTER);

			this.creditLabelsTop = new HashMap<Technology.Type, JLabel>();
			this.creditLabels = new HashMap<Technology.Type, JLabel>();
			for (Technology.Type type : EnumSet.allOf(Technology.Type.class)) {
				constraints.gridx = 4 + type.ordinal();
				constraints.gridy = 0;
				this.creditLabelsTop.put(type, this.enclosedLabelFactory(WordUtils.capitalizeFully(type.toString()),
						constraints, JLabel.CENTER, JLabel.TOP));
				constraints.gridy = 1;
				this.creditLabels.put(type, this.enclosedLabelFactory("", constraints, JLabel.CENTER, JLabel.CENTER));
			}
		}

		public void updateGui(boolean forceUpdate) {
			Game game = CivInfoPanel.this.client.getGame();
			if (game == null) {
				return;
			}
			Civilization civ = game.getCivilization(name);

			this.populationLabel.setText(String.format("%02d", civ.getPopulation()));
			this.cityLabel.setText(civ.getCityCount() + "");
			String text = String.format("%03d", civ.getVP());
			if (civ.getCurrentAge() == Age.LATE_IRON
					&& civ.onlyLateIron(CivInfoPanel.this.client.getGame().getCivilizations())) {
				text = "*" + text;
			}
			this.vpLabel.setText(text);

			for (Technology.Type type : EnumSet.allOf(Technology.Type.class)) {
				this.creditLabels.get(type).setText(civ.getTypeCredit(type) + "");
			}

		}

		public void loadProperties() {
			Properties props = CivInfoPanel.this.controller.getProperties();

			Color foreground =
					new Color(new BigInteger(props.getProperty("CivInfoPanel.Stat.Foreground"), 16).intValue());
			Color background =
					new Color(new BigInteger(props.getProperty("CivInfoPanel.Stat.Background"), 16).intValue());

			int heightTop = Integer.parseInt(props.getProperty("CivInfoPanel.Stat.Top.Height"));
			int heightBottom = Integer.parseInt(props.getProperty("CivInfoPanel.Stat.Bottom.Height"));

			int creditWidth = Integer.parseInt(props.getProperty("CivInfoPanel.Credit.Width"));

			float creditFontSizeTop = Float.parseFloat(props.getProperty("CivInfoPanel.Credit.Top.FontSize"));
			float creditFontSize = Float.parseFloat(props.getProperty("CivInfoPanel.Credit.FontSize"));

			this.setBackground(background);

			BubbaPanel.setLabelProperties(this.populationLabel,
					Integer.parseInt(props.getProperty("CivInfoPanel.Population.Width")), heightBottom, foreground,
					background, Float.parseFloat(props.getProperty("CivInfoPanel.Population.FontSize")));

			BubbaPanel.setLabelProperties(this.cityLabel,
					Integer.parseInt(props.getProperty("CivInfoPanel.City.Width")), heightBottom, foreground,
					background, Float.parseFloat(props.getProperty("CivInfoPanel.City.FontSize")));

			BubbaPanel.setLabelProperties(this.vpLabel, Integer.parseInt(props.getProperty("CivInfoPanel.VP.Width")),
					heightBottom, foreground, background,
					Float.parseFloat(props.getProperty("CivInfoPanel.VP.FontSize")));

			BubbaPanel.setLabelProperties(this.populationLabel0,
					Integer.parseInt(props.getProperty("CivInfoPanel.Population.Width")), heightTop, foreground,
					background, Float.parseFloat(props.getProperty("CivInfoPanel.Population.Top.FontSize")));

			BubbaPanel.setLabelProperties(this.cityLabel0,
					Integer.parseInt(props.getProperty("CivInfoPanel.City.Width")), heightTop, foreground, background,
					Float.parseFloat(props.getProperty("CivInfoPanel.City.Top.FontSize")));

			BubbaPanel.setLabelProperties(this.vpLabel0, Integer.parseInt(props.getProperty("CivInfoPanel.VP.Width")),
					heightTop, foreground, background,
					Float.parseFloat(props.getProperty("CivInfoPanel.VP.Top.FontSize")));

			BubbaPanel.setLabelProperties(this.creditLabel0,
					Integer.parseInt(props.getProperty("CivInfoPanel.Credit0.Width")), heightBottom, foreground,
					background, Float.parseFloat(props.getProperty("CivInfoPanel.Credit0.FontSize")));

			for (Technology.Type type : EnumSet.allOf(Technology.Type.class)) {
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

			for (Technology tech : EnumSet.allOf(Technology.class)) {
				constraints.gridx = 0 + tech.ordinal() / N_ROWS;
				constraints.gridy = 0 + tech.ordinal() % N_ROWS;

				String techString = "<html>" + tech.getName();
				for (Type type : tech.getTypes()) {
					techString = techString + " <span color=\"" + type.getHtmlColor() + "\">•</span>";
				}
				techString = techString + "</html>";

				JLabel label = this.enclosedLabelFactory(techString, constraints, JLabel.LEFT, JLabel.CENTER);
				label.setToolTipText("<html><img src=\""
						+ CivInfoPanel.class.getResource("images/" + tech.toString() + ".png") + "\"></html>");
				this.techLabels.put(tech, label);
			}

		}

		public void updateGui(boolean forceUpdate) {
			Game game = CivInfoPanel.this.client.getGame();
			if (game == null) {
				return;
			}
			Civilization civ = game.getCivilization(name);

			ArrayList<Technology> ownedTechs = civ.getTechs();

			for (Technology tech : EnumSet.allOf(Technology.class)) {
				Color color = ownedTechs.contains(tech) ? this.ownedColor : this.unownedColor;
				this.techLabels.get(tech).setForeground(color);
			}
		}

		public void loadProperties() {
			Properties prop = CivInfoPanel.this.controller.getProperties();

			this.ownedColor = new Color(new BigInteger(prop.getProperty("CivInfoPanel.Tech.Owned"), 16).intValue());
			this.unownedColor = new Color(new BigInteger(prop.getProperty("CivInfoPanel.Tech.Unowned"), 16).intValue());
			Color background =
					new Color(new BigInteger(prop.getProperty("CivInfoPanel.Tech.Background"), 16).intValue());

			int height = Integer.parseInt(prop.getProperty("CivInfoPanel.Tech.Height"));
			int width = Integer.parseInt(prop.getProperty("CivInfoPanel.Tech.Width"));

			float fontSize = Float.parseFloat(prop.getProperty("CivInfoPanel.Tech.FontSize"));

			this.setBackground(background);

			for (Technology tech : EnumSet.allOf(Technology.class)) {
				BubbaPanel.setLabelProperties(this.techLabels.get(tech), width, height, this.unownedColor, background,
						fontSize);
			}
			this.updateGui(true);
		}
	}

	@Override
	public void updateGui(boolean forceUpdate) {
		this.headerPanel.updateGui(forceUpdate);
		this.statPanel.updateGui(forceUpdate);
		this.techPanel.updateGui(forceUpdate);

		BubbaDnDTabbedPane tabbedPane = this.frame.getTabbedPane();
		int index = tabbedPane.indexOfComponent(this);

		this.frame.getTabbedPane().setForegroundAt(index, Civilization.FOREGROUND_COLORS.get(this.name));
		this.frame.getTabbedPane().setBackgroundAt(index, Civilization.BACKGROUND_COLORS.get(this.name));
	}

	@Override
	protected void loadProperties() {
		this.headerPanel.loadProperties();
		this.statPanel.loadProperties();
		this.techPanel.loadProperties();
	}

}
