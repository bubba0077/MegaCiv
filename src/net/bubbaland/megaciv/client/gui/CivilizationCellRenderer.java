package net.bubbaland.megaciv.client.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Civilization.Name;

class CivilizationCellRenderer implements ListCellRenderer<Civilization.Name> {
	private final ListCellRenderer<? super Name> internal;

	public CivilizationCellRenderer(ListCellRenderer<? super Name> listCellRenderer) {
		this.internal = listCellRenderer;
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends Name> list, Name value, int index,
			boolean isSelected, boolean cellHasFocus) {
		final Component renderer = this.internal.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);
		if (renderer instanceof JLabel) {
			Color foreground = Civilization.FOREGROUND_COLORS.get(value);
			Color background = Civilization.BACKGROUND_COLORS.get(value);
			if (isSelected) {
				( (JLabel) renderer ).setForeground(background);
				( (JLabel) renderer ).setBackground(foreground);
			} else {
				( (JLabel) renderer ).setForeground(foreground);
				( (JLabel) renderer ).setBackground(background);
			}

		}
		return renderer;
	}

}