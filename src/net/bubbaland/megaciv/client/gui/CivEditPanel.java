package net.bubbaland.megaciv.client.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.text.WordUtils;

import net.bubbaland.gui.BubbaGuiController;
import net.bubbaland.gui.BubbaPanel;
import net.bubbaland.megaciv.client.GameClient;
import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Game;
import net.bubbaland.megaciv.game.Technology;
import net.bubbaland.megaciv.game.Technology.Type;
import net.bubbaland.megaciv.messages.CivEditMessage;

public class CivEditPanel extends BubbaPanel implements ActionListener, ChangeListener {

	private static final long		serialVersionUID	= 148480222037355491L;

	private final JFrame			frame;
	private final GameClient		client;
	private final Civilization.Name	name;
	private Civilization			civ;
	private final HeaderPanel		headerPanel;
	private final StatPanel			statPanel;
	private final TechPanel			techPanel;

	public CivEditPanel(final GameClient client, final BubbaGuiController controller, final Civilization.Name name) {
		super(controller, new GridBagLayout());
		this.frame = new JFrame();
		this.client = client;
		this.name = name;

		this.civ = this.client.getGame().getCivilization(this.name).clone();

		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 3;

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
		constraints.gridx = 0;
		constraints.gridwidth = 1;
		final JButton resetButton = new JButton("Reset");
		resetButton.setActionCommand("Reset");
		resetButton.addActionListener(this);
		this.add(resetButton, constraints);

		constraints.gridy = 3;
		constraints.gridx = 1;
		final JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(this);
		this.add(cancelButton, constraints);

		constraints.gridy = 3;
		constraints.gridx = 2;
		constraints.gridwidth = 1;
		final JButton okButton = new JButton("Set");
		okButton.setActionCommand("Set");
		okButton.addActionListener(this);
		this.add(okButton, constraints);

		this.frame.add(this);
		this.frame.setTitle("Editing " + WordUtils.capitalizeFully(this.name.toString()));
		this.frame.pack();
		this.frame.setResizable(false);
		this.frame.setVisible(true);

		this.updateGui();
	}

	private class HeaderPanel extends BubbaPanel implements DocumentListener, ChangeListener {

		private static final long	serialVersionUID	= 8854884285398596201L;

		private final JTextField	playerTextField;
		private final JSpinner		astPositionSpinner;

		public HeaderPanel() {
			super(CivEditPanel.this.controller, new GridBagLayout());

			final Game game = CivEditPanel.this.client.getGame();
			final Civilization civ = game.getCivilization(CivEditPanel.this.name);

			final Properties prop = CivEditPanel.this.controller.getProperties();

			final Color foreground = Game.FOREGROUND_COLORS.get(CivEditPanel.this.name);
			final Color background = Game.BACKGROUND_COLORS.get(CivEditPanel.this.name);

			final int height = Integer.parseInt(prop.getProperty("CivEditPanel.Header.Height"));
			final int playerWidth = Integer.parseInt(prop.getProperty("CivEditPanel.Player.Width"));

			final float astFontSize = Float.parseFloat(prop.getProperty("CivEditPanel.AstPosition.FontSize"));

			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.weightx = 1.0;
			constraints.weighty = 1.0;

			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.gridheight = 2;
			this.enclosedLabelFactory(WordUtils.capitalizeFully(CivEditPanel.this.name.toString()),
					Integer.parseInt(prop.getProperty("CivEditPanel.CivName.Width")), height, foreground, background,
					constraints, Float.parseFloat(prop.getProperty("CivEditPanel.CivName.FontSize")),
					SwingConstants.LEFT, SwingConstants.BOTTOM);
			constraints.weightx = 0.0;
			constraints.gridheight = 1;

			constraints.gridx = 1;
			constraints.gridy = 0;
			this.enclosedLabelFactory("Player", playerWidth,
					Integer.parseInt(prop.getProperty("CivEditPanel.Player.Height0")), foreground, background,
					constraints, Float.parseFloat(prop.getProperty("CivEditPanel.Player.FontSize0")),
					SwingConstants.CENTER, SwingConstants.BOTTOM);

			constraints.gridy = 1;
			this.playerTextField = new JTextField(civ.getPlayer());
			this.playerTextField.setFont(this.playerTextField.getFont()
					.deriveFont(Float.parseFloat(prop.getProperty("CivEditPanel.Player.FontSize"))));
			this.playerTextField.getDocument().addDocumentListener(this);
			this.add(this.playerTextField, constraints);

			constraints.gridheight = 2;
			constraints.gridx = 2;
			constraints.gridy = 0;
			this.enclosedLabelFactory("AST Position",
					Integer.parseInt(prop.getProperty("CivEditPanel.AstPosition.Width")), height, foreground,
					background, constraints, astFontSize, SwingConstants.RIGHT, SwingConstants.CENTER);

			constraints.gridx = 3;
			this.astPositionSpinner =
					new JSpinner(new SpinnerNumberModel(civ.getAstPosition(), 0, game.lastAstStep(), 1));
			this.astPositionSpinner.setFont(this.astPositionSpinner.getFont().deriveFont(astFontSize));
			this.astPositionSpinner.addChangeListener(this);
			this.add(this.astPositionSpinner, constraints);
		}

		public void reset() {
			this.astPositionSpinner.setValue(CivEditPanel.this.civ.getAstPosition());
		}

		@Override
		public void stateChanged(final ChangeEvent event) {
			CivEditPanel.this.civ.setAstPosition((int) this.astPositionSpinner.getValue());
			CivEditPanel.this.updateGui();
		}

		@Override
		public void changedUpdate(final DocumentEvent arg0) {
			CivEditPanel.this.civ.setPlayer(this.playerTextField.getText());
		}

		@Override
		public void insertUpdate(final DocumentEvent arg0) {
			CivEditPanel.this.civ.setPlayer(this.playerTextField.getText());
		}

		@Override
		public void removeUpdate(final DocumentEvent arg0) {
			CivEditPanel.this.civ.setPlayer(this.playerTextField.getText());
		}

	}

	private class StatPanel extends BubbaPanel implements ChangeListener {

		private static final long						serialVersionUID	= 737717239222757041L;

		private final JLabel							vpLabel;
		private final HashMap<Technology.Type, JLabel>	creditLabels, creditLabelsTop;

		private final JSpinner							popSpinner, citySpinner;

		public StatPanel() {
			super(CivEditPanel.this.controller, new GridBagLayout());

			final Properties props = CivEditPanel.this.controller.getProperties();

			final Color foreground =
					new Color(new BigInteger(props.getProperty("CivEditPanel.Stat.Foreground"), 16).intValue());
			final Color background =
					new Color(new BigInteger(props.getProperty("CivEditPanel.Stat.Background"), 16).intValue());

			final int heightTop = Integer.parseInt(props.getProperty("CivEditPanel.Stat.Top.Height"));
			final int heightBottom = Integer.parseInt(props.getProperty("CivEditPanel.Stat.Bottom.Height"));

			final int creditWidth = Integer.parseInt(props.getProperty("CivEditPanel.Credit.Width"));

			final float creditFontSizeTop = Float.parseFloat(props.getProperty("CivEditPanel.Credit.Top.FontSize"));
			final float creditFontSize = Float.parseFloat(props.getProperty("CivEditPanel.Credit.FontSize"));

			this.setBackground(background);

			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.weightx = 1.0;
			constraints.weighty = 1.0;

			constraints.gridx = 1;
			constraints.gridy = 0;
			this.enclosedLabelFactory("Pop", Integer.parseInt(props.getProperty("CivEditPanel.Population.Width")),
					heightTop, foreground, background, constraints,
					Float.parseFloat(props.getProperty("CivEditPanel.Population.Top.FontSize")), SwingConstants.CENTER,
					SwingConstants.TOP);

			constraints.gridx = 1;
			constraints.gridy = 1;
			this.popSpinner = new JSpinner(
					new SpinnerNumberModel(CivEditPanel.this.civ.getPopulation(), 0, Game.MAX_POPULATION, 1));
			this.popSpinner.setFont(this.popSpinner.getFont()
					.deriveFont(Float.parseFloat(props.getProperty("CivEditPanel.Population.FontSize"))));
			this.popSpinner.setPreferredSize(
					new Dimension(Integer.parseInt(props.getProperty("CivEditPanel.Population.Width")), heightBottom));
			this.popSpinner.addChangeListener(this);
			this.popSpinner.setName("Population");
			this.add(this.popSpinner, constraints);

			constraints.gridx = 0;
			constraints.gridy = 0;
			this.enclosedLabelFactory("Cities", Integer.parseInt(props.getProperty("CivEditPanel.City.Width")),
					heightTop, foreground, background, constraints,
					Float.parseFloat(props.getProperty("CivEditPanel.City.Top.FontSize")), SwingConstants.CENTER,
					SwingConstants.TOP);

			constraints.gridx = 0;
			constraints.gridy = 1;
			this.citySpinner =
					new JSpinner(new SpinnerNumberModel(CivEditPanel.this.civ.getCityCount(), 0, Game.MAX_CITIES, 1));
			this.citySpinner.setFont(this.citySpinner.getFont()
					.deriveFont(Float.parseFloat(props.getProperty("CivEditPanel.City.FontSize"))));
			this.citySpinner.setPreferredSize(
					new Dimension(Integer.parseInt(props.getProperty("CivEditPanel.City.Width")), heightBottom));
			this.citySpinner.addChangeListener(this);
			this.citySpinner.setName("Cities");
			this.add(this.citySpinner, constraints);

			constraints.gridx = 2;
			constraints.gridy = 0;
			this.enclosedLabelFactory("VP", Integer.parseInt(props.getProperty("CivEditPanel.VP.Width")), heightTop,
					foreground, background, constraints,
					Float.parseFloat(props.getProperty("CivEditPanel.VP.Top.FontSize")), SwingConstants.CENTER,
					SwingConstants.TOP);

			constraints.gridx = 2;
			constraints.gridy = 1;
			this.vpLabel = this.enclosedLabelFactory("", Integer.parseInt(props.getProperty("CivEditPanel.VP.Width")),
					heightBottom, foreground, background, constraints,
					Float.parseFloat(props.getProperty("CivEditPanel.VP.FontSize")), SwingConstants.CENTER,
					SwingConstants.CENTER);

			constraints.gridx = 3;
			constraints.gridy = 1;
			this.enclosedLabelFactory("Credits:", Integer.parseInt(props.getProperty("CivEditPanel.Credit0.Width")),
					heightBottom, foreground, background, constraints,
					Float.parseFloat(props.getProperty("CivEditPanel.Credit0.FontSize")), SwingConstants.RIGHT,
					SwingConstants.CENTER);

			this.creditLabelsTop = new HashMap<Technology.Type, JLabel>();
			this.creditLabels = new HashMap<Technology.Type, JLabel>();
			for (final Technology.Type type : EnumSet.allOf(Technology.Type.class)) {
				constraints.gridx = 4 + type.ordinal();
				constraints.gridy = 0;
				this.creditLabelsTop.put(type,
						this.enclosedLabelFactory(WordUtils.capitalizeFully(type.toString()), creditWidth, heightTop,
								type.getColor(), background, constraints, creditFontSizeTop, SwingConstants.CENTER,
								SwingConstants.TOP));
				constraints.gridy = 1;
				this.creditLabels.put(type, this.enclosedLabelFactory("", creditWidth, heightBottom, type.getColor(),
						background, constraints, creditFontSize, SwingConstants.CENTER, SwingConstants.CENTER));
			}
		}

		@Override
		public void stateChanged(final ChangeEvent event) {
			final String sourceName = ( (Component) event.getSource() ).getName();
			switch (sourceName) {
				case "Population":
					CivEditPanel.this.civ.setPopulation((int) this.popSpinner.getValue());
					break;
				case "Cities":
					CivEditPanel.this.civ.setCityCount((int) this.citySpinner.getValue());
					break;
				default:
					CivEditPanel.this.client.log("Source not recognized in " + this.getClass().getSimpleName());
			}
			CivEditPanel.this.updateGui();
		}

		public void reset() {
			this.popSpinner.setValue(CivEditPanel.this.civ.getPopulation());
			this.citySpinner.setValue(CivEditPanel.this.civ.getCityCount());
		}

		public void updateGui() {
			final String text = String.format("%03d", CivEditPanel.this.civ.getVP());
			this.vpLabel.setText(text);

			for (final Technology.Type type : EnumSet.allOf(Technology.Type.class)) {
				this.creditLabels.get(type).setText(CivEditPanel.this.civ.getTypeCredit(type) + "");
			}
		}
	}

	private class TechPanel extends BubbaPanel implements ActionListener {

		private static final long						serialVersionUID	= 8750082449037521031L;

		private final HashMap<Technology, JCheckBox>	techCheckboxes;
		private Color									ownedColor, unownedColor;

		private final JLabel							writtenRecordLabel, monumentLabel;
		private final ArrayList<TechnologyTypeComboBox>	writtenRecordComboboxes, monumentComboboxes;

		private final int								N_ROWS				= 17;

		public TechPanel() {
			super(CivEditPanel.this.controller, new GridBagLayout());

			final Properties prop = CivEditPanel.this.controller.getProperties();

			this.ownedColor = new Color(new BigInteger(prop.getProperty("CivEditPanel.Tech.Owned"), 16).intValue());
			this.unownedColor = new Color(new BigInteger(prop.getProperty("CivEditPanel.Tech.Unowned"), 16).intValue());
			final Color background =
					new Color(new BigInteger(prop.getProperty("CivEditPanel.Tech.Background"), 16).intValue());

			this.setBackground(background);

			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.CENTER;
			constraints.weightx = 1.0;
			constraints.weighty = 1.0;
			constraints.gridwidth = 2;

			this.techCheckboxes = new HashMap<Technology, JCheckBox>();

			for (final Technology tech : EnumSet.allOf(Technology.class)) {
				constraints.gridx = ( 0 + tech.ordinal() / this.N_ROWS ) * constraints.gridwidth;
				constraints.gridy = 0 + tech.ordinal() % this.N_ROWS;

				String techString = "<html>" + tech.getName();
				for (final Type type : tech.getTypes()) {
					techString = techString + " <img height=\"16\" width=\"16\" align=\"bottom\" src=\""
							+ GuiClient.class.getResource("images/" + type.toString() + ".png") + "\" alt=\""
							+ type.toString() + "\">";
				}
				techString = techString + "</html>";

				final JCheckBox checkbox = new JCheckBox(techString);
				checkbox.setActionCommand("Tech");
				checkbox.setName(tech.toString());
				checkbox.addActionListener(this);
				checkbox.setToolTipText("<html><img src=\""
						+ CivInfoPanel.class.getResource("images/advances/" + tech.toString() + ".png") + "\"></html>");

				this.add(checkbox, constraints);
				this.techCheckboxes.put(tech, checkbox);
			}

			this.writtenRecordComboboxes = new ArrayList<TechnologyTypeComboBox>();
			constraints.gridx = 4;
			constraints.gridwidth = 2;
			constraints.gridy = this.N_ROWS;
			this.writtenRecordLabel = this.enclosedLabelFactory("Written Record Credits", constraints,
					SwingConstants.CENTER, SwingConstants.CENTER);
			constraints.gridwidth = 1;

			constraints.gridy = this.N_ROWS + 1;
			for (int i = 0; i < 2; i++) {
				constraints.gridx = 4 + i;
				final TechnologyTypeComboBox combobox = new TechnologyTypeComboBox(Technology.Type.values());
				combobox.setActionCommand("Written Record Credit");
				combobox.addActionListener(this);
				this.add(combobox, constraints);
				this.writtenRecordComboboxes.add(combobox);
			}

			this.monumentComboboxes = new ArrayList<TechnologyTypeComboBox>();

			constraints.gridx = 2;
			constraints.gridy = this.N_ROWS;
			constraints.gridwidth = 2;
			this.monumentLabel = this.enclosedLabelFactory("Monument Credits", constraints, SwingConstants.CENTER,
					SwingConstants.CENTER);
			constraints.gridwidth = 1;

			for (int i = 0; i < 4; i++) {
				constraints.gridx = 2 + i / 2;
				constraints.gridy = this.N_ROWS + 1 + i % 2;
				final TechnologyTypeComboBox combobox = new TechnologyTypeComboBox(Technology.Type.values());
				combobox.setActionCommand("Monument Credit");
				combobox.addActionListener(this);
				this.add(combobox, constraints);
				this.monumentComboboxes.add(combobox);
			}

			this.reset();
		}

		public void reset() {
			for (final Technology tech : EnumSet.allOf(Technology.class)) {
				final JCheckBox checkbox = this.techCheckboxes.get(tech);
				checkbox.setSelected(CivEditPanel.this.civ.hasTech(tech));
			}
			ArrayList<Technology.Type> credits = CivEditPanel.this.civ.getTypeCredits(Technology.WRITTEN_RECORD);
			for (int i = 0; i < 2; i++) {
				this.writtenRecordComboboxes.get(i).removeActionListener(this);
				this.writtenRecordComboboxes.get(i).setSelectedItem(credits.get(i));
				this.writtenRecordComboboxes.get(i).addActionListener(this);
			}
			credits = CivEditPanel.this.civ.getTypeCredits(Technology.MONUMENT);
			for (int i = 0; i < 4; i++) {
				this.monumentComboboxes.get(i).removeActionListener(this);
				this.monumentComboboxes.get(i).setSelectedItem(credits.get(i));
				this.monumentComboboxes.get(i).addActionListener(this);
			}
			this.updateGui();
		}

		public void updateGui() {
			final Properties prop = CivEditPanel.this.controller.getProperties();

			this.ownedColor = new Color(new BigInteger(prop.getProperty("CivEditPanel.Tech.Owned"), 16).intValue());
			this.unownedColor = new Color(new BigInteger(prop.getProperty("CivEditPanel.Tech.Unowned"), 16).intValue());
			final Color background =
					new Color(new BigInteger(prop.getProperty("CivEditPanel.Tech.Background"), 16).intValue());

			final int height = Integer.parseInt(prop.getProperty("CivEditPanel.Tech.Height"));
			final int width = Integer.parseInt(prop.getProperty("CivEditPanel.Tech.Width"));

			final float fontSize = Float.parseFloat(prop.getProperty("CivEditPanel.Tech.FontSize"));

			final ArrayList<Technology> ownedTechs = CivEditPanel.this.civ.getTechs();

			for (final Technology tech : EnumSet.allOf(Technology.class)) {
				final JCheckBox checkbox = this.techCheckboxes.get(tech);
				final Color color = ownedTechs.contains(tech) ? this.ownedColor : this.unownedColor;
				BubbaPanel.setButtonProperties(checkbox, width, height, color, background, fontSize);
			}

			this.writtenRecordLabel.getParent().setEnabled(CivEditPanel.this.civ.hasTech(Technology.WRITTEN_RECORD));
			for (final TechnologyTypeComboBox combobox : this.writtenRecordComboboxes) {
				combobox.setEnabled(CivEditPanel.this.civ.hasTech(Technology.WRITTEN_RECORD));
			}
			this.monumentLabel.getParent().setEnabled(CivEditPanel.this.civ.hasTech(Technology.MONUMENT));
			for (final TechnologyTypeComboBox combobox : this.monumentComboboxes) {
				combobox.setEnabled(CivEditPanel.this.civ.hasTech(Technology.MONUMENT));
			}
		}

		@Override
		public void actionPerformed(final ActionEvent event) {
			final String command = event.getActionCommand();
			switch (command) {
				case "Tech":
					final JCheckBox source = ( (JCheckBox) event.getSource() );
					final String techName = source.getName();
					final Technology tech = Technology.valueOf(techName);
					if (source.isSelected()) {
						CivEditPanel.this.civ.addTech(tech, CivEditPanel.this.client.getGame().getCurrentRound());
					} else {
						CivEditPanel.this.civ.removeTech(tech);
					}
					break;
				case "Written Record Credit": {
					final ArrayList<Technology.Type> credits = new ArrayList<Technology.Type>() {
						private static final long serialVersionUID = -3048516348338206499L;

						{
							for (final TechnologyTypeComboBox combobox : TechPanel.this.writtenRecordComboboxes) {
								this.add((Type) combobox.getSelectedItem());
							}
						}
					};
					CivEditPanel.this.civ.addTypeCredits(Technology.WRITTEN_RECORD, credits);
					break;
				}
				case "Monument Credit":
					final ArrayList<Technology.Type> credits = new ArrayList<Technology.Type>() {
						private static final long serialVersionUID = 4142653378901436651L;

						{
							for (final TechnologyTypeComboBox combobox : TechPanel.this.monumentComboboxes) {
								this.add((Type) combobox.getSelectedItem());
							}
						}
					};
					CivEditPanel.this.civ.addTypeCredits(Technology.MONUMENT, credits);
					break;
			}
			CivEditPanel.this.updateGui();

		}

	}

	@Override
	public void actionPerformed(final ActionEvent event) {
		final String command = event.getActionCommand();
		switch (command) {
			case "Reset":
				this.reset();
				break;
			case "Set":
				this.client.sendMessage(new CivEditMessage(this.civ));
				// Intentional fall-through
			case "Cancel":
				this.frame.dispose();
				break;

		}
	}

	public void reset() {
		this.civ = this.client.getGame().getCivilization(this.name).clone();
		this.headerPanel.reset();
		this.statPanel.reset();
		this.techPanel.reset();
	}

	public void updateGui() {
		this.statPanel.updateGui();
		this.techPanel.updateGui();
	}

	@Override
	public void stateChanged(final ChangeEvent event) {
		this.updateGui();
	}

}
