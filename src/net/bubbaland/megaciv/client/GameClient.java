package net.bubbaland.megaciv.client;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

import net.bubbaland.megaciv.game.Game;
import net.bubbaland.megaciv.game.Stopwatch;
import net.bubbaland.megaciv.game.User;
import net.bubbaland.megaciv.messages.*;
import net.bubbaland.sntp.SntpClient;
import net.bubbaland.sntp.SntpListener;

/**
 * MegaCivilization game client that handles communication with the game server and updates game data as necessary.
 * 
 * @author Walter Kolczynski
 * 
 */

@ClientEndpoint(decoders = { ServerMessage.MessageDecoder.class }, encoders = { ClientMessage.MessageEncoder.class })
public class GameClient implements Runnable, SntpListener {

	// Frequency (in secs) of synchronization of time with server
	private final static Duration		SNTP_POLL_INTERVAL		= Duration.ofMinutes(5);

	// Default trade timer length (in secs)
	private final static Duration		STARTING_TIMER_LENGTH	= Duration.ofMinutes(5);

	// Format for log timestamps
	private SimpleDateFormat			timestampFormat;

	// Connection session with server
	private Session						session;

	// Create an SNTP client to synchronize timing with server
	private final SntpClient			sntpClient;

	// Game data; updated by server when necessary
	private volatile Game				game;

	// Users connected to server; updated by server when necessary
	private volatile ArrayList<User>	userList;

	// User data for this client
	private User						user;

	// Timer for trade sessions
	private final Stopwatch				stopwatch;

	// URI for server address
	private final URI					uri;

	/**
	 * Create a new client that connects to the specified server
	 * 
	 * @param serverUrl
	 *            URL of game server
	 */
	public GameClient(final String serverUrl) {
		this.session = null;
		this.game = null;
		this.user = new User();
		this.userList = new ArrayList<User>();
		this.timestampFormat = new SimpleDateFormat("[yyyy MMM dd HH:mm:ss]");
		this.stopwatch = new Stopwatch(GameClient.STARTING_TIMER_LENGTH);
		this.uri = URI.create(serverUrl);
		this.sntpClient = new SntpClient(this.uri.getHost(), this.uri.getPort() + 1, SNTP_POLL_INTERVAL);
		this.sntpClient.addSntpListener(this);
	}

	/**
	 * Handle server disconnection
	 */
	@OnClose
	public void connectionClosed() {
		this.session = null;
		this.log("Connection closed!");
	}

	/**
	 * Get game data. Game data is updated by server when necessary.
	 * 
	 * @return The Game data.
	 */
	public Game getGame() {
		return this.game;
	}

	/**
	 * Get the currently open session.
	 * 
	 * @return The currently open session.
	 */
	public Session getSession() {
		return this.session;
	}

	/**
	 * Get the SNTP client (for time synchronization).
	 * 
	 * @return The client's SNTP client.
	 */
	public SntpClient getSntpClient() {
		return this.sntpClient;
	}

	/**
	 * Get the trading timer.
	 * 
	 * @return The client's trade timer.
	 */
	public Stopwatch getStopwatch() {
		return this.stopwatch;
	}

	/**
	 * Get this client's user data.
	 * 
	 * @return The current user.
	 */
	public User getUser() {
		return this.user;
	}

	/**
	 * Get list of users connected to server. This is updated by server as necessary.
	 * 
	 * @return A list of the connected users
	 */
	public ArrayList<User> getUserList() {
		return this.userList;
	}

	/**
	 * Find out if the client is connected to the server.
	 * 
	 * @return A boolean specifying whether client is connected to server.
	 */
	public boolean isConnected() {
		return this.session.isOpen();
	}

	/**
	 * Load game save data from file and send to server.
	 * 
	 * @param file
	 *            File containing save data.
	 */
	public void loadGame(File file) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
		try {
			Game game = mapper.readValue(file, Game.class);
			this.sendMessage(new LoadGameMessage(game));
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * Print a message to standard out with a timestamp.
	 *
	 * @param message
	 *            Message to log.
	 */
	public void log(String message) {
		final String timestamp = timestampFormat.format(new Date());
		// Print message to console
		System.out.println(timestamp + " " + message);
	}

	/**
	 * Handle error in communicating with the server
	 *
	 * @param session
	 *            The session that caused the error.
	 */
	@OnError
	public void onError(Session session, Throwable throwable) {
		this.log("Error receiving message from " + session.getRequestURI());
		throwable.printStackTrace();
	}

	/**
	 * Handle a new message from the server.
	 *
	 * @param message
	 *            The message from the server.
	 * @param session
	 *            The session that received the message.
	 */
	@OnMessage
	public void onMessage(ServerMessage message, Session session) {
		String messageType = message.getClass().getSimpleName();
		switch (messageType) {
			case "GameDataMessage": // Received updated game data
				this.game = ( (GameDataMessage) message ).getGame();
				// this.log(this.game.toString());
				break;
			case "UserListMessage": // Received an updated user list
				this.userList = ( (UserListMessage) message ).getUserList();
				break;
			case "StopwatchMessage": // Received a timer synchronization message
				this.stopwatch.remoteEvent((StopwatchMessage) message);
				break;
			default:
				this.log("ERROR: Unknown message type received: " + message.getClass().getSimpleName());
		}
	}

	/**
	 * Handle when a connection to the server is first established.
	 *
	 * @param session
	 *            The session that has just been activated.
	 * @param config
	 *            The configuration used to configure this endpoint.
	 */
	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		this.session = session;
		this.log("Now connected to " + session.getRequestURI());
		if (this.user.getUserName().equals("")) {
			this.user.setUserName(session.getId().substring(0, 7));
		}
		this.sendMessage(new SetUserMessage(this.user));

		this.sntpClient.start();
	}

	/**
	 * Run the client by connecting to the server.
	 * 
	 */
	@Override
	public void run() {
		final ClientManager clientManager = ClientManager.createClient();
		try {
			clientManager.connectToServer(this, this.uri);
		} catch (DeploymentException | IOException exception) {
			this.log("Couldn't connect to " + this.uri);
			this.connectionClosed();
		}
	}

	/**
	 * Save current game data to a file.
	 * 
	 * @param file
	 *            A file to save game data in.
	 */
	public void saveGame(File file) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.setVisibility(mapper.getVisibilityChecker().with(JsonAutoDetect.Visibility.NONE));
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
		try {
			writer.writeValue(file, game);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * Send a message to the server. Most messages are updates to the game state based on user input.
	 *
	 * @param session
	 *            The session connected to the server
	 * @param message
	 *            The message to be delivered.
	 */
	public void sendMessage(final ClientMessage message) {
		( new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() {
				if (!GameClient.this.isConnected()) {
					GameClient.this.run();
				}
				GameClient.this.session.getAsyncRemote().sendObject(message);
				return null;
			}

			@Override
			public void done() {

			}
		} ).execute();
	}

	/**
	 * Get the date-time format to be used for log timestamps.
	 * 
	 * @param timestampFormat
	 *            The date-time format to be used for log timestamps.
	 */
	protected void setTimestampFormat(SimpleDateFormat timestampFormat) {
		this.timestampFormat = timestampFormat;
	}

	/**
	 * Set user data for this client.
	 * 
	 * @param user
	 *            The new user data.
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * Determine whether a user name is already present in the server user list.
	 * 
	 * @param userName
	 *            User name to check.
	 * @return A boolean specifying whether this is a duplicate user name.
	 */
	public boolean userNameExists(String userName) {
		return this.userList.stream().filter(user -> user.compareTo(this.user) != 0)
				.anyMatch(user -> userName.equals(user.getUserName()));
	}

	@Override
	public void onSntpError(Instant when) {
		this.log("SNTP error at " + when.toString());
		this.sendMessage(new KeepAliveMessage());
	}

	@Override
	public void onSntpSync(Instant when) {
		this.log("SNTP Sync at " + when.toString());
		this.stopwatch.setServerOffset(this.sntpClient.getOffset());
	}

}
