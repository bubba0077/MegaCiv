package net.bubbaland.megaciv.client.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Civilization.Name;

class CivilizationCellRenderer implements ListCellRenderer<Civilization.Name>, ActionListener {
	private final ListCellRenderer<? super Name>	internal;
	private final JComboBox<Name>					combobox;

	public CivilizationCellRenderer(ListCellRenderer<? super Name> listCellRenderer, JComboBox<Name> combobox) {
		this.internal = listCellRenderer;
		this.combobox = combobox;
		Name selected = ( (Name) this.combobox.getSelectedItem() );
		Color foreground = Civilization.FOREGROUND_COLORS.get(selected);
		Color background = Civilization.BACKGROUND_COLORS.get(selected);

		this.combobox.setFocusable(false);

		this.combobox.setForeground(foreground);
		this.combobox.setBackground(background);
		this.combobox.addActionListener(this);
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends Name> list, Name value, int index, boolean isSelected,
			boolean cellHasFocus) {
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

	@Override
	public void actionPerformed(ActionEvent event) {

		Name selected = ( (Name) this.combobox.getSelectedItem() );
		Color foreground = Civilization.FOREGROUND_COLORS.get(selected);
		Color background = Civilization.BACKGROUND_COLORS.get(selected);

		this.combobox.getEditor().getEditorComponent().setBackground(background);
		( (JTextField) this.combobox.getEditor().getEditorComponent() ).setBackground(background);
		this.combobox.setForeground(foreground);
		this.combobox.setBackground(background);

	}

}