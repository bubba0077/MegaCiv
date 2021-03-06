package net.bubbaland.gui;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.math.BigInteger;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * Super-class for most of the dialog box panels in the GUI.
 *
 * Creates a new panel using the GridBagLayout manager. Also implements an AncestorListener to allow focus on an element
 * after the dialog is created.
 *
 * @author Walter Kolczynski
 *
 */
public abstract class BubbaDialogPanel extends BubbaPanel implements AncestorListener, FocusListener, WindowListener {

	private static final long	serialVersionUID	= -4127179718225373888L;

	protected static float		fontSize, textAreaFontSize;
	protected static int		sliderPaddingBottom, sliderPaddingTop, sliderPaddingRight, sliderPaddingLeft;
	protected static Color		warningColor;

	protected BubbaDialog		dialog;

	public BubbaDialogPanel(final BubbaGuiController controller) {
		super(controller, new GridBagLayout());
	}

	/**
	 * Override the default behavior of the text area to click the OK button of the option pane on enter and insert a
	 * line break on shift-enter
	 *
	 * @param component
	 *            The text are whose behavior we want to change
	 */
	public void addEnterOverride(final JComponent component) {
		component.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "Text Submit");
		component.getInputMap().put(KeyStroke.getKeyStroke("shift ENTER"), "insert-break");
		component.getActionMap().put("Text Submit", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				BubbaDialogPanel.this.submitText();
			}
		});
	}

	/**
	 * Change focus to the listened-to component when its ancestor is shown.
	 */
	@Override
	public void ancestorAdded(final AncestorEvent event) {
		// Change the focus to the text area when created
		final AncestorListener al = this;
		try {
			Thread.sleep(10);
		} catch (final InterruptedException e) {}
		SwingUtilities.invokeLater(() -> {
			final JComponent component = event.getComponent();
			component.requestFocusInWindow();
			component.removeAncestorListener(al);
		});

	}

	@Override
	public void ancestorMoved(final AncestorEvent event) {}

	@Override
	public void ancestorRemoved(final AncestorEvent event) {}

	@Override
	public void focusGained(final FocusEvent event) {
		final JComponent source = (JComponent) event.getSource();
		if (source instanceof JTextField) {
			// try {
			// Thread.sleep(10);
			// } catch (InterruptedException e) { }
			SwingUtilities.invokeLater(() -> ( (JTextField) source ).selectAll());

		}
	}

	@Override
	public void focusLost(final FocusEvent e) {}

	@Override
	public void windowOpened(final WindowEvent event) {}

	@Override
	public void windowClosing(final WindowEvent event) {}

	@Override
	public void windowClosed(final WindowEvent event) {
		this.dialog.removeWindowListener(this);
	}

	@Override
	public void windowIconified(final WindowEvent event) {}

	@Override
	public void windowDeiconified(final WindowEvent event) {}

	@Override
	public void windowActivated(final WindowEvent event) {}

	@Override
	public void windowDeactivated(final WindowEvent event) {}

	/**
	 * Tell the dialog to click the OK button on the option pane.
	 */
	public void submitText() {
		this.dialog.clickOK();
	}

	public static void loadProperties(final Properties properties) {
		/**
		 * Warning Color
		 */
		warningColor = new Color(new BigInteger(properties.getProperty("Dialog.Warning.Color"), 16).intValue());

		/**
		 * Font Sizes
		 */
		fontSize = Float.parseFloat(properties.getProperty("Dialog.FontSize"));
		textAreaFontSize = Float.parseFloat(properties.getProperty("Dialog.TextArea.FontSize"));
	}

}
