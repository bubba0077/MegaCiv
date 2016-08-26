package net.bubbaland.megaciv.client.gui;

import java.awt.Point;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.apache.commons.lang3.text.WordUtils;

import net.bubbaland.gui.BubbaDragDropTabFrame;
import net.bubbaland.gui.BubbaGuiController;
import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Game;

@SuppressWarnings("unused")
public class MegaCivFrame extends BubbaDragDropTabFrame implements ActionListener {

	private static final long	serialVersionUID	= -8995125745966985308L;

	private final GuiController	controller;
	private final GuiClient		client;

	protected MegaCivFrame(GuiClient client, GuiController controller) {
		super(controller);
		this.client = client;
		this.controller = controller;
		this.initTabInfoHash();

		this.tabbedPane.setFont(this.tabbedPane.getFont()
				.deriveFont(Float.parseFloat(this.controller.getProperties().getProperty("Tab.FontSize", "12f"))));

		// Create Menu
		final JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);

		final JMenu gameMenu = new JMenu("Game");
		gameMenu.setMnemonic(KeyEvent.VK_G);
		menuBar.add(gameMenu);

		JMenuItem menuItem = new JMenuItem("New Game");
		menuItem.setMnemonic(KeyEvent.VK_N);
		menuItem.setActionCommand("New Game");
		menuItem.addActionListener(this);
		gameMenu.add(menuItem);

		menuItem = new JMenuItem("Save Game");
		menuItem.setMnemonic(KeyEvent.VK_S);
		menuItem.setActionCommand("Save Game");
		menuItem.addActionListener(this);
		gameMenu.add(menuItem);

		menuItem = new JMenuItem("Load Save");
		menuItem.setMnemonic(KeyEvent.VK_L);
		menuItem.setActionCommand("Load Save");
		menuItem.addActionListener(this);
		gameMenu.add(menuItem);

		gameMenu.addSeparator();

		menuItem = new JMenuItem("Change User Name");
		menuItem.setActionCommand("Change Name");
		menuItem.addActionListener(this);
		gameMenu.add(menuItem);

		menuItem = new JMenuItem("Load Defaults");
		menuItem.setActionCommand("Load Defaults");
		menuItem.addActionListener(this);
		gameMenu.add(menuItem);

		menuItem = new JMenuItem("Retire a Player");
		menuItem.setActionCommand("Retire");
		menuItem.addActionListener(this);
		gameMenu.add(menuItem);

		menuItem = new JMenuItem("Quit");
		menuItem.setActionCommand("Quit");
		menuItem.addActionListener(this);
		gameMenu.add(menuItem);
	}

	@Override
	public ArrayList<String> getTabNames() {
		ArrayList<String> tabNames = super.getTabNames();
		ArrayList<Civilization.Name> civNames =
				( this.client.getGame() == null ) ? new ArrayList<Civilization.Name>() : this.client.getGame()
						.getCivilizationNames();
		for (Iterator<String> iterator = tabNames.iterator(); iterator.hasNext();) {
			String tabName = iterator.next();
			if (Civilization.Name.contains(tabName)
					&& !civNames.contains(Civilization.Name.valueOf(tabName.toUpperCase()))) {
				iterator.remove();
			}
		}
		return tabNames;
	}

	@Override
	protected void initTabInfoHash() {
		super.initTabInfoHash();
		this.tabInformationHash.put("AST",
				new TabInformation("Panel showing AST", AstTabPanel.class,
						new Class<?>[] { GuiClient.class, GuiController.class, MegaCivFrame.class },
						new Object[] { this.client, this.controller, this }));
		this.tabInformationHash.put("Trade",
				new TabInformation("Tab listing trade cards", TradeTabPanel.class,
						new Class<?>[] { GuiClient.class, GuiController.class, MegaCivFrame.class },
						new Object[] { this.client, this.controller, this }));
		for (Civilization.Name name : EnumSet.allOf(Civilization.Name.class)) {
			this.tabInformationHash.put(WordUtils.capitalizeFully(name.toString()),
					new TabInformation(name.toString() + " Information", CivInfoPanel.class,
							new Class<?>[] { GuiClient.class, GuiController.class, MegaCivFrame.class,
									Civilization.Name.class },
							new Object[] { this.client, this.controller, this, name }));
		}
	}

	public void updateGui(boolean forceUpdate) {
		super.updateGui(forceUpdate);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();
		switch (command) {
			case "New Game":
				new NewGameDialog(this.client, this.controller);
				break;
			case "Save Game": {
				JFileChooser chooser = new JFileChooser();
				int returnVal = chooser.showOpenDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					this.client.saveGame(chooser.getSelectedFile());
				}
				break;
			}
			case "Load Save": {
				JFileChooser chooser = new JFileChooser();
				int returnVal = chooser.showOpenDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					this.client.loadGame(chooser.getSelectedFile());
				}
				break;
			}
			case "Retire":
				new RetireDialog(this.client, this.controller);
				break;
			case "Load Defaults":
				this.controller.loadDefaults();
				break;
			case "Quit":
				this.controller.endProgram();
				break;
			case "Change Name":
				new UserDialog(this.controller, this.client);
				break;
			default:
				this.setStatusBarMessage(
						"Unknown action command " + command + "received by " + this.getClass().getSimpleName());
		}
	}

	public MegaCivFrame copy() {
		return new MegaCivFrame(this.client, this.controller);
	}
}
