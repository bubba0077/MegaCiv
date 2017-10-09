package net.bubbaland.megaciv.client.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JLabel;

import net.bubbaland.gui.BubbaPanel;
import net.bubbaland.gui.LinkedLabelGroup;
import net.bubbaland.megaciv.game.Game;

public class ControlsPanel extends BubbaPanel implements ActionListener {

	private static final long	serialVersionUID	= 7305427277230101867L;

	private final GuiClient		client;

	private final JButton		censusButton, cityButton, astButton;

	LinkedLabelGroup			turnGroup, turnNumberGroup;

	private final JLabel		turnLabel, turnNumberLabel;

	public ControlsPanel(GuiClient client, GuiController controller) {
		super(controller, new GridBagLayout());
		this.client = client;

		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridheight = 2;
		this.censusButton = new JButton("Take Census");
		this.censusButton.setActionCommand("Take Census");
		this.censusButton.addActionListener(this);
		this.censusButton.setMargin(new Insets(0, 0, 0, 0));
		this.add(this.censusButton, constraints);

		constraints.gridx = 1;
		constraints.gridy = 0;
		this.cityButton = new JButton("Update Cities");
		this.cityButton.setActionCommand("Update Cities");
		this.cityButton.addActionListener(this);
		this.cityButton.setMargin(new Insets(0, 0, 0, 0));
		this.add(this.cityButton, constraints);

		constraints.gridx = 2;
		constraints.gridy = 0;
		this.astButton = new JButton("Advance AST");
		this.astButton.setActionCommand("Advance AST");
		this.astButton.addActionListener(this);
		this.astButton.setMargin(new Insets(0, 0, 0, 0));
		this.add(this.astButton, constraints);

		constraints.gridx = 3;
		constraints.gridy = 0;
		constraints.weighty = 0.0;
		constraints.gridheight = 1;
		this.turnLabel = this.enclosedLabelFactory("Turn", constraints, JLabel.CENTER, JLabel.CENTER);
		this.turnGroup = new LinkedLabelGroup(16.0, 16.0);
		turnGroup.addLabel(this.turnLabel);

		constraints.gridx = 3;
		constraints.gridy = 1;
		constraints.weighty = 1.0;
		constraints.gridheight = 1;
		this.turnNumberLabel = this.enclosedLabelFactory("", constraints, JLabel.CENTER, JLabel.CENTER);
		this.turnNumberGroup = new LinkedLabelGroup();
		this.turnNumberGroup.addLabel(this.turnNumberLabel);

		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				resizeFonts();
			}
		});
	}

	public void resizeFonts() {
		this.turnGroup.resizeFonts();
		this.turnNumberGroup.resizeFonts();
	}

	public void updateGui() {
		// this.client.log("Updating " + this.getClass().getSimpleName());
		// TODO Auto-generated method stub
		Game game = this.client.getGame();
		if (game != null) {
			this.turnNumberLabel.setText(this.client.getGame().getTurn() + "");
		}
		this.validate();
		this.resizeFonts();
	}

	public void loadProperties() {
		Properties props = this.controller.getProperties();

		int width = Integer.parseInt(props.getProperty("ControlPanel.Width"));
		int height = Integer.parseInt(props.getProperty("ControlPanel.Height"));
		float fontSize = Float.parseFloat(props.getProperty("ControlPanel.FontSize"));

		BubbaPanel.setButtonProperties(this.censusButton, width, height, null, null, fontSize);
		BubbaPanel.setButtonProperties(this.cityButton, width, height, null, null, fontSize);
		BubbaPanel.setButtonProperties(this.astButton, width, height, null, null, fontSize);

		width = Integer.parseInt(props.getProperty("ControlPanel.Turn.Width"));

		BubbaPanel.setLabelProperties(this.turnLabel, width,
				Integer.parseInt(props.getProperty("ControlPanel.Turn.Top.Height")), null, null,
				Float.parseFloat(props.getProperty("ControlPanel.Turn.Top.FontSize")));
		BubbaPanel.setLabelProperties(this.turnNumberLabel, width,
				Integer.parseInt(props.getProperty("ControlPanel.Turn.Bottom.Height")), null, null,
				Float.parseFloat(props.getProperty("ControlPanel.Turn.Bottom.FontSize")));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (this.client.getGame() == null) {
			return;
		}
		String command = e.getActionCommand();
		switch (command) {
			case "Take Census":
				new CensusDialog(this.client, this.controller);
				break;
			case "Update Cities":
				new CityUpdateDialog(this.client, this.controller);
				break;
			case "Advance AST":
				new AstAlterationDialog(this.client, this.controller);
				break;
		}
	}

}
