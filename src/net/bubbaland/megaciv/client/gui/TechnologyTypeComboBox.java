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
import net.bubbaland.megaciv.game.Technology;
import net.bubbaland.megaciv.game.Technology.Type;

public class TechnologyTypeComboBox extends JComboBox<Technology.Type> implements ActionListener {

	private static final long serialVersionUID = -3004970120544141834L;

	public TechnologyTypeComboBox() {
		super();
		this.finishInit();
	}

	public TechnologyTypeComboBox(ComboBoxModel<Type> aModel) {
		super(aModel);
		this.finishInit();
	}

	public TechnologyTypeComboBox(Type[] items) {
		super(items);
		this.finishInit();
	}

	public TechnologyTypeComboBox(Vector<Type> items) {
		super(items);
		this.finishInit();
	}

	private void finishInit() {
		this.setRenderer(new TechnologyTypeCellRenderer(this.getRenderer()));
		Type selected = ( (Type) this.getSelectedItem() );
		Color foreground = selected.getTextColor();
		Color background = selected.getColor();

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

	private class TechnologyTypeCellRenderer implements ListCellRenderer<Technology.Type> {
		private final ListCellRenderer<? super Technology.Type> internal;

		public TechnologyTypeCellRenderer(ListCellRenderer<? super Technology.Type> listCellRenderer) {
			this.internal = listCellRenderer;
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

	}

}

