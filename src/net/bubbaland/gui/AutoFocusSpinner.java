/**
 * Creates a spinner that automatically selects all of the text when the spinner receives focus. This allows for easier
 * data entry when components are being navigated by tabbing.
 * 
 * @author Walter Kolczynski
 * 
 */

package net.bubbaland.gui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SwingUtilities;

public class AutoFocusSpinner extends JSpinner {
	private static final long serialVersionUID = -7900543713955610434L;

	public AutoFocusSpinner(SpinnerModel model) {
		super(model);

		// Add an anonymous focus listener that will select all of the text when this gets focus
		( (JSpinner.DefaultEditor) AutoFocusSpinner.this.getEditor() ).getTextField()
				.addFocusListener(new FocusListener() {

					@Override
					public void focusGained(FocusEvent e) {
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								( (JSpinner.DefaultEditor) AutoFocusSpinner.this.getEditor() ).getTextField()
										.selectAll();
							}

						});
					}

					@Override
					public void focusLost(FocusEvent e) {

			}

				});
	}

}