package net.bubbaland.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.JLabel;


/**
 *
 *
 * Extended from font-resizing labels at http://java-sl.com/tip_adapt_label_font_size.html
 *
 * @author Walter Kolczynski
 *
 */
public class LinkedLabelGroup {

	private final ArrayList<JLabel>	group;
	private int						padding;
	private double					minFontSize, maxFontSize;

	/**
	 * @return the padding
	 */
	public int getPadding() {
		return this.padding;
	}

	/**
	 * @param padding
	 *            the padding to set
	 */
	public void setPadding(final int padding) {
		this.padding = padding;
	}

	/**
	 * @return the minFontSize
	 */
	public double getMinFontSize() {
		return this.minFontSize;
	}

	/**
	 * @param minFontSize
	 *            the minFontSize to set
	 */
	public void setMinFontSize(final float minFontSize) {
		this.minFontSize = minFontSize;
	}

	/**
	 * @return the maxFontSize
	 */
	public double getMaxFontSize() {
		return this.maxFontSize;
	}

	/**
	 * @param maxFontSize
	 *            the maxFontSize to set
	 */
	public void setMaxFontSize(final float maxFontSize) {
		this.maxFontSize = maxFontSize;
	}

	private static double	DEFAULT_MIN_FONTSIZE	= 6;
	private static double	DEFAULT_MAX_FONTSIZE	= 128;
	private static int		DEFAULT_PADDING			= 6;


	public LinkedLabelGroup() {
		this(DEFAULT_MIN_FONTSIZE, DEFAULT_MAX_FONTSIZE, DEFAULT_PADDING);
	}

	public LinkedLabelGroup(final int padding) {
		this(DEFAULT_MIN_FONTSIZE, DEFAULT_MAX_FONTSIZE, padding);
	}

	public LinkedLabelGroup(final double minFontSize, final double maxFontSize) {
		this(minFontSize, maxFontSize, DEFAULT_PADDING);
	}

	public LinkedLabelGroup(final double minFontSize, final double maxFontSize, final int padding) {
		this.group = new ArrayList<JLabel>();
		this.minFontSize = minFontSize;
		this.maxFontSize = maxFontSize;
		this.padding = padding;
	}

	public void addLabel(final JLabel label) {
		this.group.add(label);
	}

	public void removeLabel(final JLabel label) {
		if (this.group.contains(label)) {
			this.group.remove(label);
		}
	}

	public void resizeFonts() {
		float fontSize = (float) this.maxFontSize;
		for (final JLabel label : this.group) {
			final Graphics g = label.getGraphics();
			final String text = label.getText();
			final Dimension bounds = label.getSize();
			if (g == null || !label.isVisible()) {
				// System.out.println("Skipping: " + label.getName() + " " + text + " " + ( !label.isVisible() ));
				continue;
			}
			final Font font = label.getFont();

			final Rectangle r1 = new Rectangle();
			r1.setSize(getTextSize(g, text, font.deriveFont(fontSize)));

			// System.out.println(label.getName() + " " + text + ":" + r1 + " " + bounds);

			while (fontSize > this.minFontSize && !( bounds.getWidth() > r1.getWidth() + this.padding
					&& bounds.getHeight() > r1.getHeight() + this.padding )) {
				fontSize--;
				r1.setSize(getTextSize(g, text, font.deriveFont(fontSize)));
			}
		}
		for (final JLabel label : this.group) {
			label.setFont(label.getFont().deriveFont(fontSize));
		}
	}

	private static Dimension getTextSize(final Graphics g, final String text, final Font font) {
		final Dimension size = new Dimension();
		g.setFont(font);
		final FontMetrics fm = g.getFontMetrics(font);
		size.width = fm.stringWidth(text);
		size.height = fm.getHeight();
		return size;
	}

	@Override
	public String toString() {
		String s = "LinkedLabelGroup " + super.toString() + "\n";
		s = s + "Minimum font size: " + this.minFontSize + "\n";
		s = s + "Maximum font size: " + this.maxFontSize + "\n";
		s = s + "Padding: " + this.padding + "\n";
		s = s + "Contains these labels: \n";
		for (final JLabel label : this.group) {
			s = s + label.getName() + ": " + label.getText() + "\n";
		}
		return s;
	}
}
