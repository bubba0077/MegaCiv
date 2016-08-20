package net.bubbaland.megaciv.client;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import net.bubbaland.megaciv.game.Game;
import net.bubbaland.megaciv.game.Stopwatch;
import net.bubbaland.megaciv.game.User;
import net.bubbaland.megaciv.messages.*;
import net.bubbaland.sntp.SntpClient;

@ClientEndpoint(decoders = { ServerMessage.MessageDecoder.class }, encoders = { ClientMessage.MessageEncoder.class })
public class GameClient implements Runnable {

	// Format for log timestamps
	private SimpleDateFormat	timestampFormat;

	// private final String serverUrl;
	private Session				session;

	private final SntpClient	sntpClient;

	/**
	 * @return the sntpClient
	 */
	public SntpClient getSntpClient() {
		return this.sntpClient;
	}

	private static int SNTP_POLL_INTERVAL = 30000;

	/**
	 * @return the session
	 */
	public Session getSession() {
		return this.session;
	}

	private boolean				isConnected;

	private volatile Game		game;

	private ArrayList<User>		userList;

	private User				user;

	private final static int	STARTING_TIMER_LENGTH_SEC	= 300;

	private final Stopwatch		stopwatch;

	private final URI			uri;

	public GameClient(final String serverUrl) {
		// this.serverUrl = serverUrl;
		this.session = null;
		this.game = null;
		this.user = new User();
		this.userList = new ArrayList<User>();
		this.isConnected = false;
		this.timestampFormat = new SimpleDateFormat("[yyyy MMM dd HH:mm:ss]");
		this.stopwatch = new Stopwatch(GameClient.STARTING_TIMER_LENGTH_SEC);

		this.uri = URI.create(serverUrl);
		this.sntpClient = new SntpClient(this.uri.getHost(), this.uri.getPort() + 1, SNTP_POLL_INTERVAL);
	}

	public Game getGame() {
		return this.game;
	}

	public ArrayList<User> getUserList() {
		return this.userList;
	}

	public boolean userNameExists(String userName) {
		for (User user : this.userList) {
			user.compareTo(this.user);
			if (user.compareTo(this.user) != 0 && userName.equals(user.getUserName())) {
				return true;
			}
		}
		return false;
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
			clientManager.connectToServer(this, this.uri);
		} catch (DeploymentException | IOException exception) {
			this.log("Couldn't connect");
			exception.printStackTrace();
			this.connectionClosed();
		}
	}

	/**
	 * Initial hook when a client first connects (TriviaServerEndpoint() is automatically called as well)
	 *
	 * @param session
	 * @param config
	 */
	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		this.session = session;
		this.log("Now connected to " + session.getRequestURI());
		this.isConnected = true;
		if (this.user.getUserName().equals("")) {
			this.user.setUserName(session.getId().substring(0, 7));
		}
		this.sendMessage(new SetUserMessage(this.user));

		this.sntpClient.start();
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
				break;
			case "UserListMessage":
				this.userList = ( (UserListMessage) message ).getUserList();
				break;
			case "ServerTimerMessage":
				this.stopwatch.remoteEvent((ServerTimerMessage) message, this.sntpClient.getOffset());
				break;
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

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return this.user;
	}

	public Stopwatch getStopwatch() {
		return this.stopwatch;
	}

}
