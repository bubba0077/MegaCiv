package net.bubbaland.megaciv.client.gui;

import java.awt.Color;
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
import javax.swing.Timer;

import net.bubbaland.gui.BubbaAudio;
import net.bubbaland.gui.BubbaDialog;
import net.bubbaland.gui.BubbaDialogPanel;
import net.bubbaland.gui.BubbaGuiController;
import net.bubbaland.gui.BubbaPanel;

public class StopwatchPanel extends BubbaPanel implements ActionListener {

	private static final long		serialVersionUID			= 8183502027042074947L;

	private final static BubbaAudio	BEEP						= new BubbaAudio(StopwatchPanel.class,
			"audio/beep.mp3");
	private final static BubbaAudio	ALARM						= new BubbaAudio(StopwatchPanel.class,
			"audio/finalSound.mp3");

	public final int				STARTING_TIMER_LENGTH_SEC	= 300;

	private int						timerLength;
	private volatile int			deciseconds;

	private Timer					timer;

	private final JToggleButton		runButton;
	private final JButton			setButton, resetButton;
	private final JLabel			clockLabel;

	public StopwatchPanel(GuiClient client, GuiController controller) {
		super(controller, new GridBagLayout());

		this.timerLength = STARTING_TIMER_LENGTH_SEC * 10;

		// Set up layout constraints
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;
		constraints.gridx = 0;
		constraints.gridy = 0;
		this.setButton = new JButton("Set");
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
		this.runButton.setBackground(Color.GREEN);
		this.runButton.addActionListener(this);
		this.add(this.runButton, constraints);

		this.timer = new Timer(100, this);
		this.timer.setActionCommand("Tic");
		this.resetTimer();

		this.loadProperties();

		this.updateGui();
	}

	public void resetTimer() {
		this.timer.stop();
		this.deciseconds = this.timerLength;
		this.runButton.setSelected(false);
		this.runButton.setBackground(Color.GREEN);
		this.runButton.setText("Run");
		this.runButton.setActionCommand("Run");
		this.updateGui();
	}

	public void updateGui() {
		int min = this.deciseconds / 600;
		double sec = Math.max(( this.deciseconds - ( 600 * min ) ) * 0.1, 0.0);
		this.clockLabel.setText(String.format("%02d", min) + ":" + String.format("%04.1f", sec));
		if (this.deciseconds < 150) {
			this.clockLabel.setForeground(Color.RED);
		} else {
			this.clockLabel.setForeground(Color.WHITE);
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		switch (event.getActionCommand()) {
			case "Tic":
				switch (this.deciseconds--) {
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
						// this.timer.stop();
						ALARM.play();
						break;
					case -150:
						this.resetTimer();
				}
				this.updateGui();
				break;
			case "Set":
				new SetTimerDialog(this.controller);
				break;
			case "Run":
				this.timer.start();
				this.runButton.setBackground(Color.RED);
				this.runButton.setActionCommand("Stop");
				break;
			case "Stop":
				this.timer.stop();
				this.runButton.setBackground(Color.GREEN);
				this.runButton.setText("Run");
				this.runButton.setActionCommand("Run");
				break;
			case "Reset":
				this.resetTimer();
				break;
		}
	}

	public void loadProperties() {
		Properties props = this.controller.getProperties();

		Color foreground = new Color(
				new BigInteger(props.getProperty("Stopwatch.Clock.ForegroundColor"), 16).intValue());
		Color background = new Color(
				new BigInteger(props.getProperty("Stopwatch.Clock.BackgroundColor"), 16).intValue());

		int buttonWidth = Integer.parseInt(props.getProperty("Stopwatch.Button.Width"));
		int buttonHeight = Integer.parseInt(props.getProperty("Stopwatch.Button.Height"));

		int clockWidth = Integer.parseInt(props.getProperty("Stopwatch.Clock.Width"));
		int clockHeight = Integer.parseInt(props.getProperty("Stopwatch.Clock.Height"));

		float buttonFontSize = Float.parseFloat(props.getProperty("Stopwatch.Button.FontSize"));
		float clockFontSize = Float.parseFloat(props.getProperty("Stopwatch.Clock.FontSize"));

		BubbaPanel.setButtonProperties(this.resetButton, buttonWidth, buttonHeight, null, buttonFontSize);
		BubbaPanel.setButtonProperties(this.setButton, buttonWidth, buttonHeight, null, buttonFontSize);

		BubbaPanel.setLabelProperties(this.clockLabel, clockWidth, clockHeight, foreground, background, clockFontSize);
	}

	private class SetTimerDialog extends BubbaDialogPanel {

		private static final long	serialVersionUID	= -3524760652681331356L;

		private final JSpinner		minSpinner, secSpinner;

		public SetTimerDialog(BubbaGuiController controller) {
			super(controller);

			Properties props = this.controller.getProperties();
			float fontSize = Float.parseFloat(props.getProperty("Stopwatch.Set.FontSize"));

			int currentMin = StopwatchPanel.this.timerLength / 600;
			int currentSec = ( StopwatchPanel.this.timerLength - ( 600 * currentMin ) ) / 10;

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
				StopwatchPanel.this.timerLength = (int) this.minSpinner.getValue() * 600
						+ (int) this.secSpinner.getValue() * 10;
				StopwatchPanel.this.resetTimer();
			}
		}

	}

}
