package net.bubbaland.megaciv.client.gui;

import java.awt.Cursor;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
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
import net.bubbaland.megaciv.game.StopwatchListener;
import net.bubbaland.megaciv.messages.*;

@ClientEndpoint(decoders = { ServerMessage.MessageDecoder.class }, encoders = { ClientMessage.MessageEncoder.class })
public class GuiClient extends GameClient implements StopwatchListener {

	private final GuiController	gui;

	private static Clip			ONE_MINUTE, BEEP, TIME_UP;
	static {
		try {
			ONE_MINUTE = AudioSystem.getClip();
			ONE_MINUTE.open(
					AudioSystem.getAudioInputStream(GuiClient.class.getResource("audio/one_minute_remaining.wav")));
			BEEP = AudioSystem.getClip();
			BEEP.open(AudioSystem.getAudioInputStream(GuiClient.class.getResource("audio/beep.wav")));
			TIME_UP = AudioSystem.getClip();
			TIME_UP.open(AudioSystem.getAudioInputStream(GuiClient.class.getResource("audio/time_up.wav")));
		} catch (LineUnavailableException | IOException | UnsupportedAudioFileException exception) {
			System.out.println("Unable to load audio clips in GuiClient!");
		}
	}

	public GuiClient(final String serverURL, GuiController gui) {
		super(serverURL);
		this.gui = gui;
		this.getStopwatch().addStopwatchListener(this);
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


		GuiClient.this.gui.updateGui();
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

				newTabs.stream().forEachOrdered(tabName -> frame.addTab(tabName));

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

	@Override
	public void tic(Duration timeRemaining) {
		/**
		 * Play audio alerts at the appropriate times.
		 */
		int remainingMillisecs = (int) timeRemaining.toMillis();
		switch (remainingMillisecs) {
			case 1000:
			case 2000:
			case 3000:
			case 4000:
			case 5000:
			case 15000:
			case 30000:
				BEEP.setFramePosition(0);
				BEEP.start();
				break;
			case 60000:
				ONE_MINUTE.setFramePosition(0);
				ONE_MINUTE.start();
				break;
			case 0:
				TIME_UP.setFramePosition(0);
				TIME_UP.start();
				break;
		}
	}

	@Override
	public void watchStarted() {}

	@Override
	public void watchStopped() {}

	@Override
	public void watchReset() {}

}
