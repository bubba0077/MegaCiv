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
	private float					minFontSize, maxFontSize;

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
	public void setPadding(int padding) {
		this.padding = padding;
	}

	/**
	 * @return the minFontSize
	 */
	public float getMinFontSize() {
		return this.minFontSize;
	}

	/**
	 * @param minFontSize
	 *            the minFontSize to set
	 */
	public void setMinFontSize(float minFontSize) {
		this.minFontSize = minFontSize;
	}

	/**
	 * @return the maxFontSize
	 */
	public float getMaxFontSize() {
		return this.maxFontSize;
	}

	/**
	 * @param maxFontSize
	 *            the maxFontSize to set
	 */
	public void setMaxFontSize(float maxFontSize) {
		this.maxFontSize = maxFontSize;
	}

	private static float	DEFAULT_MIN_FONTSIZE	= 6;
	private static float	DEFAULT_MAX_FONTSIZE	= 128;
	private static int		DEFAULT_PADDING			= 6;


	public LinkedLabelGroup() {
		this(DEFAULT_MIN_FONTSIZE, DEFAULT_MAX_FONTSIZE, DEFAULT_PADDING);
	}

	public LinkedLabelGroup(int padding) {
		this(DEFAULT_MIN_FONTSIZE, DEFAULT_MAX_FONTSIZE, padding);
	}

	public LinkedLabelGroup(float minFontSize, float maxFontSize) {
		this(minFontSize, maxFontSize, DEFAULT_PADDING);
	}

	public LinkedLabelGroup(float minFontSize, float maxFontSize, int padding) {
		this.group = new ArrayList<JLabel>();
		this.minFontSize = minFontSize;
		this.maxFontSize = maxFontSize;
		this.padding = padding;
	}

	public void addLabel(JLabel label) {
		this.group.add(label);
	}

	public void removeLabel(JLabel label) {
		if (this.group.contains(label)) {
			this.group.remove(label);
		}
	}

	public void resizeFonts() {
		float fontSize = this.maxFontSize;
		for (JLabel label : this.group) {
			Graphics g = label.getGraphics();
			if (g == null || label.isVisible() == false) {
				continue;
			}
			Dimension bounds = label.getSize();
			Font font = label.getFont();

			Rectangle r1 = new Rectangle();
			r1.setSize(getTextSize(label, font.deriveFont(fontSize)));

			while (fontSize > this.minFontSize && !( bounds.getWidth() > r1.getWidth() + this.padding
					&& bounds.getHeight() > r1.getHeight() + this.padding )) {
				fontSize--;
				r1.setSize(getTextSize(label, font.deriveFont(fontSize)));
			}
		}
		for (JLabel label : this.group) {
			label.setFont(label.getFont().deriveFont(fontSize));
		}
	}

	private static Dimension getTextSize(JLabel label, Font font) {
		Dimension size = new Dimension();
		Graphics g = label.getGraphics();
		g.setFont(font);
		FontMetrics fm = g.getFontMetrics(font);
		size.width = fm.stringWidth(label.getText());
		size.height = fm.getHeight();
		return size;
	}

}
