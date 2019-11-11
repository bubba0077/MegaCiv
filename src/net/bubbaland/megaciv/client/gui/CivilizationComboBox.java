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
import net.bubbaland.megaciv.game.Game;

public class CivilizationComboBox extends JComboBox<Civilization.Name> implements ActionListener {

	private static final long serialVersionUID = -3004970120544141834L;

	public CivilizationComboBox() {
		super();
		this.finishInit();
	}

	public CivilizationComboBox(final ComboBoxModel<Civilization.Name> aModel) {
		super(aModel);
		this.finishInit();
	}

	public CivilizationComboBox(final Civilization.Name[] items) {
		super(items);
		this.finishInit();
	}

	public CivilizationComboBox(final Vector<Civilization.Name> items) {
		super(items);
		this.finishInit();
	}

	private void finishInit() {
		this.setRenderer(new CivilizationCellRenderer(this.getRenderer()));
		final Name selected = ( (Name) this.getSelectedItem() );
		final Color foreground = Game.FOREGROUND_COLORS.get(selected);
		final Color background = Game.BACKGROUND_COLORS.get(selected);

		this.addActionListener(this);

		this.setFocusable(false);
		this.setForeground(foreground);
		this.setBackground(background);
	}

	@Override
	public void actionPerformed(final ActionEvent event) {
		final Name selected = ( (Name) this.getSelectedItem() );
		final Color foreground = Game.FOREGROUND_COLORS.get(selected);
		final Color background = Game.BACKGROUND_COLORS.get(selected);

		this.setForeground(foreground);
		this.setBackground(background);
	}

	private class CivilizationCellRenderer implements ListCellRenderer<Civilization.Name> {
		private final ListCellRenderer<? super Name> internal;

		public CivilizationCellRenderer(final ListCellRenderer<? super Name> listCellRenderer) {
			this.internal = listCellRenderer;
		}

		@Override
		public Component getListCellRendererComponent(final JList<? extends Name> list, final Name value,
				final int index, final boolean isSelected, final boolean cellHasFocus) {
			final Component renderer =
					this.internal.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (renderer instanceof JLabel) {
				final Color foreground = Game.FOREGROUND_COLORS.get(value);
				final Color background = Game.BACKGROUND_COLORS.get(value);
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