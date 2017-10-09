package net.bubbaland.megaciv.client.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.math.BigInteger;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import net.bubbaland.gui.BubbaDialog;
import net.bubbaland.gui.BubbaDialogPanel;
import net.bubbaland.gui.BubbaGuiController;
import net.bubbaland.gui.BubbaPanel;
import net.bubbaland.gui.LinkedLabelGroup;
import net.bubbaland.megaciv.client.GameClient;
import net.bubbaland.megaciv.game.Stopwatch;
import net.bubbaland.megaciv.game.StopwatchListener;
import net.bubbaland.megaciv.messages.ClientTimerMessage;
import net.bubbaland.megaciv.messages.TimerMessage;

public class StopwatchPanel extends BubbaPanel implements ActionListener, StopwatchListener {

	private static final long		serialVersionUID	= 8183502027042074947L;

	private final JToggleButton		runButton;
	private final JButton			setButton, resetButton;
	private final LinkedLabelGroup	clockLabelGroup;
	private final JLabel			clockLabel;

	private final static double		ASPECT_RATIO		= 3.0;

	private final GameClient		client;

	public StopwatchPanel(GameClient client, GuiController controller) {
		super(controller, new GridBagLayout());

		this.client = client;

		// Set up layout constraints
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;
		constraints.gridx = 0;
		constraints.gridy = 0;
		this.setButton = new JButton("Set");
		this.setButton.setMargin(new Insets(0, 0, 0, 0));
		this.setButton.addActionListener(this);
		this.add(this.setButton, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		this.resetButton = new JButton("Reset");
		this.resetButton.setMargin(new Insets(0, 0, 0, 0));
		this.resetButton.addActionListener(this);
		this.add(this.resetButton, constraints);

		constraints.weightx = 0.0;
		constraints.weighty = 1.0;
		constraints.gridx = 0;
		constraints.gridy = 1;
		this.add(new JPanel(), constraints);

		constraints.weightx = 1.0;
		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.gridheight = 3;
		this.clockLabel = this.enclosedLabelFactory("", constraints, JLabel.CENTER, JLabel.CENTER);
		this.clockLabelGroup = new LinkedLabelGroup();
		this.clockLabelGroup.addLabel(this.clockLabel);

		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				StopwatchPanel.this.resized();
			}
		});

		constraints.weightx = 0.0;
		constraints.gridx = 2;
		constraints.gridy = 0;
		constraints.gridheight = 3;
		this.runButton = new JToggleButton("Run");
		this.runButton.addActionListener(this);
		this.runButton.setMargin(new Insets(0, 0, 0, 0));
		this.add(this.runButton, constraints);

		this.loadProperties();

		this.client.getStopwatch().addStopwatchListener(this);

		this.updateGui();
	}

	private void resized() {
		JPanel panel = (JPanel) this.clockLabel.getParent();
		int height = panel.getHeight();
		int width = (int) ( height * ASPECT_RATIO );
		panel.setPreferredSize(new Dimension(width, height));
		this.clockLabelGroup.resizeFonts();
	}

	public void tic(int deciseconds) {
		this.updateGui(deciseconds);
	}

	public void watchStarted() {
		this.runButton.setSelected(false);
		this.runButton.setForeground(Color.WHITE);
		this.runButton.setBackground(Color.RED);
		this.runButton.setText("Stop");
		this.runButton.setActionCommand("Stop");
	}

	public void watchStopped() {
		this.runButton.setSelected(false);
		this.runButton.setForeground(Color.BLACK);
		this.runButton.setBackground(Color.GREEN);
		this.runButton.setText("Run");
		this.runButton.setActionCommand("Run");
	}

	public void watchReset() {
		this.watchStopped();
		this.updateGui();
	}

	public void updateGui() {
		this.updateGui(this.client.getStopwatch().getTicsRemaining());
	}

	public void updateGui(int deciseconds) {
		int min = deciseconds / 600;
		double sec = Math.max(( deciseconds - ( 600 * min ) ) * 0.1, 0.0);
		this.clockLabel.setText(String.format("%02d", min) + ":" + String.format("%04.1f", sec));
		if (deciseconds < 150) {
			this.clockLabel.setForeground(Color.RED);
		} else {
			this.clockLabel.setForeground(Color.WHITE);
		}
		// this.resized();
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		long eventTime = event.getWhen() + this.client.getSntpClient().getOffset();
		Stopwatch stopwatch = this.client.getStopwatch();
		switch (event.getActionCommand()) {
			case "Set":
				new SetTimerDialog(this.controller);
				break;
			case "Run":
				this.client.sendMessage(new ClientTimerMessage(TimerMessage.StopwatchEvent.START, eventTime,
						stopwatch.getTimerLength(), stopwatch.getLastEventTic()));
				break;
			case "Stop":
				this.client.sendMessage(new ClientTimerMessage(TimerMessage.StopwatchEvent.STOP, eventTime,
						stopwatch.getTimerLength(), stopwatch.getLastEventTic()));
				break;
			case "Reset":
				this.client.sendMessage(new ClientTimerMessage(TimerMessage.StopwatchEvent.RESET, eventTime,
						stopwatch.getTimerLength(), stopwatch.getLastEventTic()));
				break;
		}
	}

	public void loadProperties() {
		Properties props = this.controller.getProperties();

		Color foreground =
				new Color(new BigInteger(props.getProperty("Stopwatch.Clock.ForegroundColor"), 16).intValue());
		Color background =
				new Color(new BigInteger(props.getProperty("Stopwatch.Clock.BackgroundColor"), 16).intValue());

		int buttonWidth = Integer.parseInt(props.getProperty("Stopwatch.Button.Width"));
		int buttonHeight = Integer.parseInt(props.getProperty("Stopwatch.Button.Height"));

		int runWidth = Integer.parseInt(props.getProperty("Stopwatch.Run.Width"));

		int clockHeight = Integer.parseInt(props.getProperty("Stopwatch.Clock.Height"));

		float buttonFontSize = Float.parseFloat(props.getProperty("Stopwatch.Button.FontSize"));
		float runFontSize = Float.parseFloat(props.getProperty("Stopwatch.Run.FontSize"));

		BubbaPanel.setButtonProperties(this.resetButton, buttonWidth, buttonHeight, null, null, buttonFontSize);
		BubbaPanel.setButtonProperties(this.setButton, buttonWidth, buttonHeight, null, null, buttonFontSize);
		this.runButton.setPreferredSize(new Dimension(runWidth, clockHeight));
		this.runButton.setFont(this.runButton.getFont().deriveFont(runFontSize));

		this.clockLabel.setForeground(foreground);
		this.clockLabel.getParent().setBackground(background);
	}

	private class SetTimerDialog extends BubbaDialogPanel {

		private static final long	serialVersionUID	= -3524760652681331356L;

		private final JSpinner		minSpinner, secSpinner;

		public SetTimerDialog(BubbaGuiController controller) {
			super(controller);

			Properties props = this.controller.getProperties();
			float fontSize = Float.parseFloat(props.getProperty("Stopwatch.Set.FontSize"));

			int timerLength = StopwatchPanel.this.client.getStopwatch().getTimerLength();

			int currentMin = timerLength / 60;
			int currentSec = timerLength - ( 60 * currentMin );

			this.minSpinner = new JSpinner(new SpinnerNumberModel(currentMin, 0, 59, 1));
			this.minSpinner.setFont(this.minSpinner.getFont().deriveFont(fontSize));
			this.secSpinner = new JSpinner(new SpinnerNumberModel(currentSec, 0, 59, 1));
			this.secSpinner.setFont(this.secSpinner.getFont().deriveFont(fontSize));

			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.weightx = 0.0;
			constraints.weighty = 0.0;
			constraints.gridx = 0;
			constraints.gridy = 0;
			this.add(this.minSpinner, constraints);

			constraints.gridx = 1;
			JLabel label = new JLabel(":");
			label.setFont(label.getFont().deriveFont(fontSize));
			this.add(label, constraints);

			constraints.gridx = 2;
			this.add(this.secSpinner, constraints);

			this.dialog = new BubbaDialog(this.controller, "Set Timer", this, JOptionPane.PLAIN_MESSAGE,
					JOptionPane.OK_CANCEL_OPTION);
			this.dialog.setVisible(true);
		}

		public void windowClosed(WindowEvent event) {
			long now = System.currentTimeMillis() + client.getSntpClient().getOffset();
			super.windowClosed(event);

			// If the OK button was pressed, open the question
			final int option = ( (Integer) this.dialog.getValue() ).intValue();

			if (option == JOptionPane.OK_OPTION) {
				StopwatchPanel.this.client.sendMessage(new ClientTimerMessage(TimerMessage.StopwatchEvent.SET, now,
						(int) this.minSpinner.getValue() * 60 + (int) this.secSpinner.getValue(),
						StopwatchPanel.this.client.getStopwatch().getLastEventTic()));
			}
		}

	}

}
