package net.bubbaland.megaciv.client.gui;

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.websocket.ClientEndpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import net.bubbaland.gui.BubbaDnDTabbedPane;
import net.bubbaland.gui.BubbaDragDropTabFrame;
import net.bubbaland.gui.NewTabDialog.TabComparator;
import net.bubbaland.megaciv.client.GameClient;
import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Game;
import net.bubbaland.megaciv.messages.*;

@ClientEndpoint(decoders = { ServerMessage.MessageDecoder.class }, encoders = { ClientMessage.MessageEncoder.class })
public class GuiClient extends GameClient {

	private final GuiController gui;

	public GuiClient(final String serverURL, GuiController gui) {
		super(serverURL);
		this.gui = gui;
	}

	@OnClose
	public void connectionClosed() {
		super.connectionClosed();
		new DisconnectedDialog(this.gui, this);
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		super.onOpen(session, config);
	}

	@OnMessage
	public void onMessage(ServerMessage message, Session session) {
		Game game = this.getGame();
		ArrayList<Civilization.Name> civNamesBefore =
				( game != null ) ? game.getCivilizationNames() : new ArrayList<Civilization.Name>();
		super.onMessage(message, session);


		GuiClient.this.gui.updateGui(true);
		ArrayList<Civilization.Name> civNamesAfter = ( GuiClient.this.getGame() != null ) ? GuiClient.this.getGame()
				.getCivilizationNames() : new ArrayList<Civilization.Name>();
		boolean civNamesIdentical =
				civNamesBefore.containsAll(civNamesAfter) && civNamesAfter.containsAll(civNamesBefore);

		/**
		 * If civilizations have changed, remove and add tabs for civilizations as necessary.
		 */
		if (!civNamesIdentical) {
			( new SwingWorker<Void, Void>() {
				public Void doInBackground() {
					while (GuiClient.this.getUser().getUserName() == null || GuiClient.this.getUser().getUserName()
							.equals(GuiClient.this.getSession().getId().substring(0, 7))) {
						try {
							Thread.sleep(50);
						} catch (final InterruptedException exception) {
							exception.printStackTrace();
							System.exit(2);
						}
					}
					return null;
				}

				public void done() {
					GuiClient.this.updateTabs();
				}
			} ).execute();
		}
	}

	private void updateTabs() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {

				BubbaDragDropTabFrame frame = (BubbaDragDropTabFrame) GuiClient.this.gui.getFirstWindow();
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				BubbaDnDTabbedPane pane = frame.getTabbedPane();

				List<String> allTabs = frame.getTabNames().stream().filter(tabName -> !tabName.startsWith("*"))
						.collect(Collectors.toList());

				List<String> newTabs = frame.getTabNames().stream()
						.filter(tabName -> !tabName.startsWith("*") && pane.indexOfTab(tabName) == -1)
						.collect(Collectors.toList());

				Arrays.stream(pane.getTabNames()).filter(tabName -> !( allTabs.contains(tabName) ))
						.forEach(tabName -> pane.remove(pane.indexOfTab(tabName)));
				;

				newTabs.sort(new TabComparator());

				int selectedTab = pane.getSelectedIndex();

				for (String tabName : newTabs) {
					// Add the tab to the tabbed pane
					frame.addTab(tabName);
				}
				pane.setSelectedIndex(selectedTab);

				frame.setCursor(Cursor.getDefaultCursor());
			}
		});
	}

	/**
	 * Handle error in communicating with a client
	 *
	 * @param session
	 */
	@OnError
	public void onError(Session session, Throwable throwable) {
		super.onError(session, throwable);
	}

	public void log(String message) {
		super.log(message);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				GuiClient.this.gui.setStatusBarText(message);
			}
		});
	}

}
