package net.bubbaland.megaciv.client;

import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.websocket.ClientEndpoint;
import javax.websocket.DeploymentException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;

import net.bubbaland.megaciv.game.Game;
import net.bubbaland.megaciv.messages.*;
import net.bubbaland.megaciv.messages.client.ClientMessage;
import net.bubbaland.megaciv.messages.server.GameDataMessage;
import net.bubbaland.megaciv.messages.server.ServerMessage;

@ClientEndpoint(decoders = { ServerMessage.MessageDecoder.class }, encoders = { ClientMessage.MessageEncoder.class })
public class GameClient implements Runnable {

	// Format for log timestamps
	private SimpleDateFormat	timestampFormat;

	private final String		serverUrl;
	private Session				session;
	private boolean				isConnected;

	private Game				game;

	public GameClient(final String serverUrl) {
		this.serverUrl = serverUrl;
		this.session = null;
		this.isConnected = false;
		this.timestampFormat = new SimpleDateFormat("[yyyy MMM dd HH:mm:ss]");
		this.run();
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
		String messageType = message.getClass().toString();
		this.log(messageType + " received");
		switch (message.messageType()) {
			case "GameData":
				this.game = ( (GameDataMessage) message ).getGame();
			default:
				this.log("Unknown message type received!");
		}
	}

	@OnMessage


	/**
	 * Handle error in communicating with a client
	 *
	 * @param session
	 */
	@OnError
	public void onError(Session session, Throwable throwable) {
		// TODO Auto-generated method stub
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

}
