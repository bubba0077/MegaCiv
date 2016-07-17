package net.bubbaland.megaciv.client;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.SwingWorker;
import javax.websocket.ClientEndpoint;
import javax.websocket.DeploymentException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.bubbaland.megaciv.client.messages.*;
import net.bubbaland.megaciv.game.Game;
import net.bubbaland.megaciv.server.messages.*;

@ClientEndpoint(decoders = { ServerMessage.MessageDecoder.class }, encoders = { ClientMessage.MessageEncoder.class })
public class GameClient implements Runnable {

	// Format for log timestamps
	private SimpleDateFormat	timestampFormat;

	private final String		serverUrl;
	private Session				session;
	private boolean				isConnected;

	private volatile Game		game;

	public GameClient(final String serverUrl) {
		this.serverUrl = serverUrl;
		this.session = null;
		this.game = null;
		this.isConnected = false;
		this.timestampFormat = new SimpleDateFormat("[yyyy MMM dd HH:mm:ss]");
	}

	public Game getGame() {
		return this.game;
	}

	public void setTimestampFormat(SimpleDateFormat timestampFormat) {
		this.timestampFormat = timestampFormat;
	}

	public boolean isConnected() {
		return this.isConnected;
	}

	@Override
	public void run() {
		final ClientManager clientManager = ClientManager.createClient();
		try {
			clientManager.connectToServer(this, URI.create(this.serverUrl));
		} catch (DeploymentException | IOException exception) {
			this.log("Couldn't connect");
			exception.printStackTrace();
			this.connectionClosed();
		}
	}

	// /**
	// * Initial hook when a client first connects (TriviaServerEndpoint() is automatically called as well)
	// *
	// * @param session
	// * @param config
	// */
	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		this.session = session;
		this.log("Now connected to " + session.getRequestURI());
		this.isConnected = true;
	}

	/**
	 * Handle a message from the client
	 *
	 * @param message
	 * @param session
	 */
	@OnMessage
	public void onMessage(ServerMessage message, Session session) {
		String messageType = message.getClass().getSimpleName();
		switch (messageType) {
			case "GameDataMessage":
				this.game = ( (GameDataMessage) message ).getGame();
				// this.log(this.game.toString());
			default:
		}
	}

	/**
	 * Handle error in communicating with a client
	 *
	 * @param session
	 */
	@OnError
	public void onError(Session session, Throwable throwable) {
		this.log("Error receiving message from " + session.getRequestURI());
		throwable.printStackTrace();
	}

	/**
	 * Handle a client disconnection
	 *
	 * @param session
	 */
	@OnClose
	public void connectionClosed() {
		this.log("Connection closed!");
		this.isConnected = false;
	}

	/**
	 * Display message in the status bar and in console
	 *
	 * @param message
	 *            Message to log
	 */
	public void log(String message) {
		final String timestamp = timestampFormat.format(new Date());
		// Print message to console
		System.out.println(timestamp + " " + message);
	}

	/**
	 * Send a message to the specified client
	 *
	 * @param session
	 * @param message
	 */
	public void sendMessage(final ClientMessage message) {
		( new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() {
				GameClient.this.session.getAsyncRemote().sendObject(message);
				return null;
			}

			@Override
			public void done() {

			}
		} ).execute();

		// if (SwingUtilities.isEventDispatchThread()) {
		// System.out.println(this.getClass().getSimpleName() + "Trying to send message from Event Dispatch Thread!");
		// }
		// this.session.getAsyncRemote().sendObject(message);
	}

	public void saveGame(File file) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.writeValue(file, this.game);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	public void loadGame(File file) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			this.sendMessage(new LoadGameMessage(mapper.readValue(file, Game.class)));
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

}
