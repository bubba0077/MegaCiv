package net.bubbaland.megaciv.client.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import net.bubbaland.megaciv.game.Technology;
import net.bubbaland.megaciv.game.Technology.Type;

class TechnologyTypeCellRenderer implements ListCellRenderer<Technology.Type>, ActionListener {
	private final ListCellRenderer<? super Technology.Type>	internal;
	private final JComboBox<Technology.Type>				combobox;

	public TechnologyTypeCellRenderer(ListCellRenderer<? super Technology.Type> listCellRenderer,
			JComboBox<Technology.Type> combobox) {
		this.internal = listCellRenderer;
		this.combobox = combobox;
		Type selected = ( (Type) this.combobox.getSelectedItem() );
		Color foreground = selected.getTextColor();
		Color background = selected.getColor();

		this.combobox.setFocusable(false);
		this.combobox.setForeground(foreground);
		this.combobox.setBackground(background);
		this.combobox.addActionListener(this);

	}

	@Override
	public Component getListCellRendererComponent(JList<? extends Technology.Type> list, Technology.Type value,
			int index, boolean isSelected, boolean cellHasFocus) {
		final Component renderer = this.internal.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);
		if (renderer instanceof JLabel) {
			Color foreground = value.getTextColor();
			Color background = value.getColor();
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

	@Override
	public void actionPerformed(ActionEvent event) {
		Type selected = ( (Type) this.combobox.getSelectedItem() );
		Color foreground = selected.getTextColor();
		Color background = selected.getColor();

		this.combobox.setForeground(foreground);
		this.combobox.setBackground(background);
	}

}