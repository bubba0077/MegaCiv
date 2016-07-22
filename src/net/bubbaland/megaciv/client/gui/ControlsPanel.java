package net.bubbaland.megaciv.client.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.JButton;

import net.bubbaland.gui.BubbaPanel;

public class ControlsPanel extends BubbaPanel implements ActionListener {

	private static final long	serialVersionUID	= 7305427277230101867L;

	private final GuiClient		client;

	private final JButton		censusButton, cityButton, techButton, astButton;

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
		this.techButton = new JButton("Purchase Techs");
		this.techButton.setActionCommand("Purchase Techs");
		this.techButton.addActionListener(this);
		this.techButton.setMargin(new Insets(0, 0, 0, 0));
		this.add(this.techButton, constraints);

		constraints.gridx = 3;
		constraints.gridy = 0;
		this.astButton = new JButton("Advance AST");
		this.astButton.setActionCommand("Advance AST");
		this.astButton.addActionListener(this);
		this.astButton.setMargin(new Insets(0, 0, 0, 0));
		this.add(this.astButton, constraints);
	}

	public void updateGui(boolean forceUpdate) {
		// this.client.log("Updating " + this.getClass().getSimpleName());
		// TODO Auto-generated method stub

	}

	public void loadProperties() {
		Properties props = this.controller.getProperties();

		int width = Integer.parseInt(props.getProperty("ControlPanel.Width"));
		int height = Integer.parseInt(props.getProperty("ControlPanel.Height"));
		float fontSize = Float.parseFloat(props.getProperty("ControlPanel.FontSize"));

		BubbaPanel.setButtonProperties(this.censusButton, width, height, null, null, fontSize);
		BubbaPanel.setButtonProperties(this.cityButton, width, height, null, null, fontSize);
		BubbaPanel.setButtonProperties(this.techButton, width, height, null, null, fontSize);
		BubbaPanel.setButtonProperties(this.astButton, width, height, null, null, fontSize);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		switch (command) {
			case "Take Census":
				new CensusDialog(this.client, this.controller);
				break;
			case "Update Cities":
				new CityUpdateDialog(this.client, this.controller);
				break;
			case "Purchase Techs":
				new TechnologyStoreDialog(this.client, this.controller);
				break;
			case "Advance AST":
				new AstAlterationDialog(this.client, this.controller);
				break;
		}
	}

}
