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
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import net.bubbaland.gui.BubbaDialog;
import net.bubbaland.gui.BubbaDialogPanel;
import net.bubbaland.gui.BubbaGuiController;
import net.bubbaland.gui.BubbaPanel;
import net.bubbaland.gui.LinkedLabelGroup;
import net.bubbaland.megaciv.client.GameClient;
import net.bubbaland.megaciv.game.Stopwatch;
import net.bubbaland.megaciv.game.StopwatchListener;

public class StopwatchPanel extends BubbaPanel implements ActionListener, StopwatchListener {

	private static final long		serialVersionUID	= 8183502027042074947L;

	private final JToggleButton		runButton;
	private final JButton			setButton, resetButton;
	private final LinkedLabelGroup	clockLabelGroup;
	private final JLabel			clockLabel;

	private final static double		ASPECT_RATIO		= 3.0;

	private final GameClient		client;

	public StopwatchPanel(final GameClient client, final GuiController controller) {
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
		this.clockLabel = this.enclosedLabelFactory("", constraints, SwingConstants.CENTER, SwingConstants.CENTER);
		this.clockLabelGroup = new LinkedLabelGroup();
		this.clockLabelGroup.addLabel(this.clockLabel);

		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
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
		final JPanel panel = (JPanel) this.clockLabel.getParent();
		final int height = panel.getHeight();
		final int width = (int) ( height * ASPECT_RATIO );
		panel.setPreferredSize(new Dimension(width, height));
		this.clockLabelGroup.resizeFonts();
	}

	@Override
	public void tic(final Duration timeRemaining) {
		this.updateGui(timeRemaining);
	}

	@Override
	public void watchStarted() {
		this.runButton.setSelected(false);
		this.runButton.setForeground(Color.WHITE);
		this.runButton.setBackground(Color.RED);
		this.runButton.setText("Stop");
		this.runButton.setActionCommand("Stop");
	}

	@Override
	public void watchStopped() {
		this.runButton.setSelected(false);
		this.runButton.setForeground(Color.BLACK);
		this.runButton.setBackground(Color.GREEN);
		this.runButton.setText("Run");
		this.runButton.setActionCommand("Run");
	}

	@Override
	public void watchReset() {
		this.watchStopped();
		this.updateGui();
	}

	public void updateGui() {
		this.updateGui(this.client.getStopwatch().getTimeRemaining(Instant.now()));
	}

	public void updateGui(final Duration timeRemaining) {
		this.clockLabel.setText(Stopwatch.formatTimer(timeRemaining));
		if (timeRemaining.minus(Duration.ofSeconds(30)).isNegative()) {
			this.clockLabel.setForeground(Color.RED);
		} else {
			this.clockLabel.setForeground(Color.WHITE);
		}
		// this.resized();
	}

	@Override
	public void actionPerformed(final ActionEvent event) {
		final Instant eventTime = Instant.ofEpochMilli(event.getWhen());

		final Stopwatch stopwatch = this.client.getStopwatch();
		switch (event.getActionCommand()) {
			case "Set":
				new SetTimerDialog(this.controller);
				break;
			case "Run":
				this.client.sendMessage(stopwatch.generateTimerMessage(Stopwatch.StopwatchEvent.START, eventTime));
				break;
			case "Stop":
				this.client.sendMessage(stopwatch.generateTimerMessage(Stopwatch.StopwatchEvent.STOP, eventTime));
				break;
			case "Reset":
				// stopwatch.reset(eventTime);
				this.client.sendMessage(stopwatch.generateTimerMessage(Stopwatch.StopwatchEvent.SET, eventTime));
				break;
		}
	}

	public void loadProperties() {
		final Properties props = this.controller.getProperties();

		final Color foreground =
				new Color(new BigInteger(props.getProperty("Stopwatch.Clock.ForegroundColor"), 16).intValue());
		final Color background =
				new Color(new BigInteger(props.getProperty("Stopwatch.Clock.BackgroundColor"), 16).intValue());

		final int buttonWidth = Integer.parseInt(props.getProperty("Stopwatch.Button.Width"));
		final int buttonHeight = Integer.parseInt(props.getProperty("Stopwatch.Button.Height"));

		final int runWidth = Integer.parseInt(props.getProperty("Stopwatch.Run.Width"));

		final int clockHeight = Integer.parseInt(props.getProperty("Stopwatch.Clock.Height"));

		final float buttonFontSize = Float.parseFloat(props.getProperty("Stopwatch.Button.FontSize"));
		final float runFontSize = Float.parseFloat(props.getProperty("Stopwatch.Run.FontSize"));

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

		public SetTimerDialog(final BubbaGuiController controller) {
			super(controller);

			final Properties props = this.controller.getProperties();
			final float fontSize = Float.parseFloat(props.getProperty("Stopwatch.Set.FontSize"));

			final Duration timerLength = StopwatchPanel.this.client.getStopwatch().getTimerLength();

			final int currentMin = (int) timerLength.toMinutes();
			final int currentSec = (int) timerLength.minus(Duration.ofMinutes(currentMin)).getSeconds();

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
			final JLabel label = new JLabel(":");
			label.setFont(label.getFont().deriveFont(fontSize));
			this.add(label, constraints);

			constraints.gridx = 2;
			this.add(this.secSpinner, constraints);

			this.dialog = new BubbaDialog(this.controller, "Set Timer", this, JOptionPane.PLAIN_MESSAGE,
					JOptionPane.OK_CANCEL_OPTION);
			this.dialog.setVisible(true);
		}

		@Override
		public void windowClosed(final WindowEvent event) {
			// long now = System.currentTimeMillis() + client.getSntpClient().getOffset();
			final Instant now = Instant.now();
			super.windowClosed(event);

			// If the OK button was pressed, open the question
			final int option = ( (Integer) this.dialog.getValue() ).intValue();

			if (option == JOptionPane.OK_OPTION) {
				final Duration length = Duration.ofMinutes((int) this.minSpinner.getValue())
						.plus(Duration.ofSeconds((int) this.secSpinner.getValue()));
				final Stopwatch stopwatch = StopwatchPanel.this.client.getStopwatch();
				stopwatch.setTimer(length);
				StopwatchPanel.this.client
						.sendMessage(stopwatch.generateTimerMessage(Stopwatch.StopwatchEvent.SET, now));
			}
		}

	}

}
