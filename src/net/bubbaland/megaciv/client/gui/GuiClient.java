package net.bubbaland.megaciv.client.gui;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
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
import net.bubbaland.megaciv.messages.ClientMessage;
import net.bubbaland.megaciv.messages.ServerMessage;

@ClientEndpoint(decoders = { ServerMessage.MessageDecoder.class }, encoders = { ClientMessage.MessageEncoder.class })
public class GuiClient extends GameClient implements StopwatchListener {

	private final GuiController	gui;

	// File name of font
	final static private String	FONT_FILENAME	= "/net/bubbaland/megaciv/client/gui/fonts/tahoma.ttf";

	private static Clip			ONE_MINUTE, BEEP, TIME_UP;
	static {
		try {
			ONE_MINUTE = AudioSystem.getClip(null);
			ONE_MINUTE.open(
					AudioSystem.getAudioInputStream(GuiClient.class.getResource("audio/one_minute_remaining.wav")));
			BEEP = AudioSystem.getClip(null);
			BEEP.open(AudioSystem.getAudioInputStream(GuiClient.class.getResource("audio/beep.wav")));
			TIME_UP = AudioSystem.getClip(null);
			TIME_UP.open(AudioSystem.getAudioInputStream(GuiClient.class.getResource("audio/time_up.wav")));
		} catch (LineUnavailableException | IOException | UnsupportedAudioFileException exception) {
			System.out.println("Unable to load audio clips in GuiClient!");
		}

		// Change default font on all objects
		final Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			final Object key = keys.nextElement();
			final Object value = UIManager.get(key);
			if (value instanceof FontUIResource) {
				try {
					final Font font =
							Font.createFont(Font.TRUETYPE_FONT, GuiClient.class.getResourceAsStream(FONT_FILENAME));
					UIManager.put(key, new FontUIResource(font.deriveFont(12.0f)));
				} catch (FontFormatException | IOException exception) {
					exception.printStackTrace();
				}
			}
		}
	}

	public GuiClient(final String serverURL, final GuiController gui) {
		super(serverURL);
		this.gui = gui;
		this.getStopwatch().addStopwatchListener(this);
	}

	@Override
	@OnClose
	public void connectionClosed() {
		super.connectionClosed();
		new DisconnectedDialog(this.gui, this);
	}

	@Override
	@OnOpen
	public void onOpen(final Session session, final EndpointConfig config) {
		super.onOpen(session, config);
	}

	@Override
	@OnMessage
	public void onMessage(final ServerMessage message, final Session session) {
		final Game game = this.getGame();
		final ArrayList<Civilization.Name> civNamesBefore =
				( game != null ) ? game.getCivilizationNames() : new ArrayList<Civilization.Name>();
		super.onMessage(message, session);

		GuiClient.this.gui.updateGui();
		final ArrayList<Civilization.Name> civNamesAfter = ( GuiClient.this.getGame() != null ) ? GuiClient.this
				.getGame().getCivilizationNames() : new ArrayList<Civilization.Name>();
		final boolean civNamesIdentical =
				civNamesBefore.containsAll(civNamesAfter) && civNamesAfter.containsAll(civNamesBefore);

		/**
		 * If civilizations have changed, remove and add tabs for civilizations as necessary.
		 */
		if (!civNamesIdentical) {
			( new SwingWorker<Void, Void>() {
				@Override
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

				@Override
				public void done() {
					GuiClient.this.updateTabs();
				}
			} ).execute();
		}
	}

	protected void gameEnd() {
		new GameEndDialog(this, this.gui);
	}

	private void updateTabs() {
		SwingUtilities.invokeLater(() -> {

			final BubbaDragDropTabFrame frame = (BubbaDragDropTabFrame) GuiClient.this.gui.getFirstWindow();
			frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			final BubbaDnDTabbedPane pane = frame.getTabbedPane();

			final List<String> allTabs = frame.getTabNames().stream().filter(tabName1 -> !tabName1.startsWith("*"))
					.collect(Collectors.toList());

			final List<String> newTabs = frame.getTabNames().stream()
					.filter(tabName2 -> !tabName2.startsWith("*") && pane.indexOfTab(tabName2) == -1)
					.collect(Collectors.toList());

			Arrays.stream(pane.getTabNames()).filter(tabName3 -> !( allTabs.contains(tabName3) ))
					.forEach(tabName4 -> pane.remove(pane.indexOfTab(tabName4)));
			;

			newTabs.sort(new TabComparator());

			final int selectedTab = pane.getSelectedIndex();

			newTabs.stream().forEachOrdered(tabName5 -> frame.addTab(tabName5));

			pane.setSelectedIndex(selectedTab);

			frame.setCursor(Cursor.getDefaultCursor());
		});
	}

	/**
	 * Handle error in communicating with a client
	 *
	 * @param session
	 */
	@Override
	@OnError
	public void onError(final Session session, final Throwable throwable) {
		super.onError(session, throwable);
	}

	@Override
	public void log(final String message) {
		super.log(message);
		SwingUtilities.invokeLater(() -> {
			if (GuiClient.this.gui != null) {
				GuiClient.this.gui.setStatusBarText(message);
			}
		});
	}

	@Override
	public void tic(final Duration timeRemaining) {
		/**
		 * Play audio alerts at the appropriate times.
		 */
		final int remainingMillisecs = (int) timeRemaining.toMillis();
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
