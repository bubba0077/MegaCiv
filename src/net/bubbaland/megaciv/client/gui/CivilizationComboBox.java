package net.bubbaland.megaciv.client.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Civilization.Name;
import net.bubbaland.megaciv.game.Technology.Type;

public class CivilizationComboBox extends JComboBox<Civilization.Name> implements ActionListener {

	private static final long serialVersionUID = -3004970120544141834L;

	public CivilizationComboBox() {
		super();
		this.finishInit();
	}

	public CivilizationComboBox(ComboBoxModel<Civilization.Name> aModel) {
		super(aModel);
		this.finishInit();
	}

	public CivilizationComboBox(Civilization.Name[] items) {
		super(items);
		this.finishInit();
	}

	public CivilizationComboBox(Vector<Civilization.Name> items) {
		super(items);
		this.finishInit();
	}

	private void finishInit() {
		this.setRenderer(new CivilizationCellRenderer(this.getRenderer()));
		Name selected = ( (Name) this.getSelectedItem() );
		Color foreground = Civilization.FOREGROUND_COLORS.get(selected);
		Color background = Civilization.BACKGROUND_COLORS.get(selected);

		this.addActionListener(this);

		this.setFocusable(false);
		this.setForeground(foreground);
		this.setBackground(background);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Type selected = ( (Type) this.getSelectedItem() );
		Color foreground = selected.getTextColor();
		Color background = selected.getColor();

		this.setForeground(foreground);
		this.setBackground(background);
	}

	private class CivilizationCellRenderer implements ListCellRenderer<Civilization.Name> {
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
}