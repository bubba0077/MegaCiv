package net.bubbaland.megaciv.client.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import net.bubbaland.gui.BubbaAudio;
import net.bubbaland.gui.BubbaDialog;
import net.bubbaland.gui.BubbaDialogPanel;
import net.bubbaland.gui.BubbaGuiController;
import net.bubbaland.gui.BubbaPanel;
import net.bubbaland.megaciv.client.GameClient;

public class StopwatchPanel extends BubbaPanel implements ActionListener, StopwatchListener {

	private static final long		serialVersionUID			= 8183502027042074947L;

	private final static BubbaAudio	BEEP						=
			new BubbaAudio(StopwatchPanel.class, "audio/beep.mp3");
	private final static BubbaAudio	ALARM						=
			new BubbaAudio(StopwatchPanel.class, "audio/finalSound.mp3");

	public final static int			STARTING_TIMER_LENGTH_SEC	= 300;

	private static Stopwatch		stopwatch					= new Stopwatch(STARTING_TIMER_LENGTH_SEC);

	private final JToggleButton		runButton;
	private final JButton			setButton, resetButton;
	private final JLabel			clockLabel;

	public StopwatchPanel(GameClient client, GuiController controller) {
		super(controller, new GridBagLayout());

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

		constraints.weightx = 0.0;
		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.gridheight = 3;
		this.clockLabel = this.enclosedLabelFactory("", constraints, JLabel.CENTER, JLabel.CENTER);

		constraints.weightx = 0.0;
		constraints.gridx = 2;
		constraints.gridy = 0;
		constraints.gridheight = 3;
		this.runButton = new JToggleButton("Run");
		this.runButton.addActionListener(this);
		this.runButton.setMargin(new Insets(0, 0, 0, 0));
		this.add(this.runButton, constraints);

		this.loadProperties();

		stopwatch.addStopwatchListener(this);

		this.updateGui();
	}

	public void tic(int deciseconds) {
		switch (deciseconds) {
			case 10:
			case 20:
			case 30:
			case 40:
			case 50:
			case 150:
			case 600:
				BEEP.play();
				break;
			case 0:
				ALARM.play();
				break;
		}
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
		this.updateGui();
	}

	public void updateGui() {
		this.updateGui(stopwatch.getTicsRemaining());
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
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		switch (event.getActionCommand()) {
			case "Set":
				new SetTimerDialog(this.controller);
				break;
			case "Run":
				stopwatch.start();
				break;
			case "Stop":
				stopwatch.stop();
				break;
			case "Reset":
				stopwatch.reset();
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

		int clockWidth = Integer.parseInt(props.getProperty("Stopwatch.Clock.Width"));
		int clockHeight = Integer.parseInt(props.getProperty("Stopwatch.Clock.Height"));

		float buttonFontSize = Float.parseFloat(props.getProperty("Stopwatch.Button.FontSize"));
		float runFontSize = Float.parseFloat(props.getProperty("Stopwatch.Run.FontSize"));
		float clockFontSize = Float.parseFloat(props.getProperty("Stopwatch.Clock.FontSize"));

		BubbaPanel.setButtonProperties(this.resetButton, buttonWidth, buttonHeight, null, null, buttonFontSize);
		BubbaPanel.setButtonProperties(this.setButton, buttonWidth, buttonHeight, null, null, buttonFontSize);
		this.runButton.setPreferredSize(new Dimension(runWidth, clockHeight));
		this.runButton.setFont(this.runButton.getFont().deriveFont(runFontSize));

		BubbaPanel.setLabelProperties(this.clockLabel, clockWidth, clockHeight, foreground, background, clockFontSize);
	}

	private class SetTimerDialog extends BubbaDialogPanel {

		private static final long	serialVersionUID	= -3524760652681331356L;

		private final JSpinner		minSpinner, secSpinner;

		public SetTimerDialog(BubbaGuiController controller) {
			super(controller);

			Properties props = this.controller.getProperties();
			float fontSize = Float.parseFloat(props.getProperty("Stopwatch.Set.FontSize"));

			int timerLength = stopwatch.getTimerLength();

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
			super.windowClosed(event);

			// If the OK button was pressed, open the question
			final int option = ( (Integer) this.dialog.getValue() ).intValue();

			if (option == JOptionPane.OK_OPTION) {
				StopwatchPanel.stopwatch
						.setTimer((int) this.minSpinner.getValue() * 60 + (int) this.secSpinner.getValue());
				StopwatchPanel.stopwatch.reset();
			}
		}

	}

}
