package net.bubbaland.megaciv.server;

import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.websocket.DeploymentException;
import javax.websocket.Session;

import org.glassfish.tyrus.server.Server;

import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Civilization.AstChange;
import net.bubbaland.megaciv.game.Civilization.Name;
import net.bubbaland.megaciv.game.Game;
import net.bubbaland.megaciv.game.Game.Difficulty;
import net.bubbaland.megaciv.game.GameEvent;
import net.bubbaland.megaciv.game.Stopwatch;
import net.bubbaland.megaciv.game.Technology;
import net.bubbaland.megaciv.game.User;
import net.bubbaland.megaciv.messages.AdditionalCreditMessage;
import net.bubbaland.megaciv.messages.AdvanceAstMessage;
import net.bubbaland.megaciv.messages.CensusMessage;
import net.bubbaland.megaciv.messages.CityUpdateMessage;
import net.bubbaland.megaciv.messages.CivEditMessage;
import net.bubbaland.megaciv.messages.ClientMessage;
import net.bubbaland.megaciv.messages.GameDataMessage;
import net.bubbaland.megaciv.messages.GameEndMessage;
import net.bubbaland.megaciv.messages.LoadGameMessage;
import net.bubbaland.megaciv.messages.NewGameMessage;
import net.bubbaland.megaciv.messages.RetireMessage;
import net.bubbaland.megaciv.messages.ServerMessage;
import net.bubbaland.megaciv.messages.SetUserMessage;
import net.bubbaland.megaciv.messages.StopwatchMessage;
import net.bubbaland.megaciv.messages.TechPurchaseMessage;
import net.bubbaland.megaciv.messages.UndoPurchaseMessage;
import net.bubbaland.megaciv.messages.UserListMessage;
import net.bubbaland.sntp.SntpServer;

public class GameServer extends Server {

	private Game											game;

	private final Server									server;
	private SntpServer										sntpServer;

	private final Stopwatch									stopwatch;

	private boolean											isRunning;
	private final int										serverPort;

	private final Hashtable<Session, ClientMessageReceiver>	sessionList;

	public GameServer(final String serverUrl, final int serverPort) {
		this.serverPort = serverPort;
		this.server = new Server(serverUrl, serverPort, "/", null, ClientMessageReceiver.class);
		ClientMessageReceiver.registerServer(this);
		this.sntpServer = new SntpServer(123);
		this.sessionList = new Hashtable<Session, ClientMessageReceiver>();
		this.stopwatch = new Stopwatch(Duration.ofMinutes(5));
	}

	@Override
	public void start() throws DeploymentException {
		this.server.start();
		this.sntpServer.run();
		this.isRunning = true;
	}

	@Override
	public void stop() {
		this.server.stop();
		this.sntpServer = new SntpServer(this.serverPort + 1);
		this.isRunning = false;
	}

	public void addSession(final Session session, final ClientMessageReceiver endpoint) {
		this.log("New client connecting...");
		this.sessionList.put(session, endpoint);
		this.broadcastMessage(new UserListMessage(this.getUserList()));
	}

	private ArrayList<User> getUserList() {
		final ArrayList<User> userList = new ArrayList<User>() {
			private static final long serialVersionUID = 5052692806073959873L;

			{
				for (final ClientMessageReceiver endpoint : GameServer.this.sessionList.values()) {
					this.add(endpoint.getUser());
				}
			}
		};
		return userList;
	}

	public void removeSession(final Session session) {
		this.log(this.sessionList.get(session).getUser() + " disconnected");
		this.sessionList.remove(session);
		this.broadcastMessage(new UserListMessage(this.getUserList()));
	}

	public void processIncomingMessage(final ClientMessage message, final Session session) {
		final String messageType = message.getClass().getSimpleName();
		final User user = this.sessionList.get(session).getUser();
		user.updateActivity();
		final GameEvent event = new GameEvent(message.getEventType(), user, message.toString());
		switch (messageType) {
			case "StopwatchMessage":
				this.stopwatch.remoteEvent((StopwatchMessage) message);
				this.broadcastMessage((StopwatchMessage) message);
				break;
			case "NewGameMessage":
				this.game = new Game();
				final HashMap<Civilization.Name, String> startingCivs = ( (NewGameMessage) message ).getCivNames();
				this.game.addCivilization(new ArrayList<Civilization.Name>(startingCivs.keySet()));
				for (final Civilization.Name name : startingCivs.keySet()) {
					this.game.getCivilization(name).setPlayer(startingCivs.get(name));
				}
				if (( (NewGameMessage) message ).useCredits()) {
					this.game.assignStartCredits();
				}
				final Difficulty difficulty = ( (NewGameMessage) message ).getDifficulty();
				this.game.setDifficulty(difficulty);
				final Civilization.Region region = ( (NewGameMessage) message ).getRegion();
				this.game.setRegion(region);
				final boolean showVP = ( (NewGameMessage) message ).showVP();
				this.game.setShowVP(showVP);

				// this.log(user + " created new " + WordUtils.capitalizeFully(difficulty.toString())
				// + " game with the following civilizations: " + startingCivs);
				// this.broadcastMessage(new GameDataMessage(this.game));
				break;
			case "CensusMessage":
				final HashMap<Civilization.Name, Integer> census = ( (CensusMessage) message ).getCensus();
				for (final Civilization.Name name : census.keySet()) {
					// this.log(name + " " + census.get(name) + " " + this.game.getCivilization(name));
					this.game.getCivilization(name).setPopulation(census.get(name));
				}
				// this.broadcastMessage(new GameDataMessage(this.game));
				// this.log("Census reported by " + user + ": " + census);
				break;
			case "CityUpdateMessage":
				final HashMap<Civilization.Name, Integer> cityCount = ( (CityUpdateMessage) message ).getCityCount();
				for (final Civilization.Name name : cityCount.keySet()) {
					this.game.getCivilization(name).setCityCount(cityCount.get(name));
				}
				// this.broadcastMessage(new GameDataMessage(this.game));
				// this.log("City counts updated by " + user + ": " + cityCount);
				break;
			case "TechPurchaseMessage": {
				final Civilization.Name name = ( (TechPurchaseMessage) message ).getCivName();
				final Civilization civ = this.game.getCivilization(name);
				final ArrayList<Technology> newTechs = ( (TechPurchaseMessage) message ).getTechs();
				for (final Technology newTech : newTechs) {
					civ.addTech(newTech, this.game.getCurrentRound());
				}
				civ.setPurchased(true);
				// this.log(name + " bought the following technologies: " + newTechs + " (via " + user + ")");
				// this.broadcastMessage(new GameDataMessage(this.game));
				break;
			}
			case "UndoPurchaseMessage": {
				final Civilization.Name name = ( (UndoPurchaseMessage) message ).getCivName();
				final Civilization civ = this.game.getCivilization(name);
				final int currentRound = this.game.getCurrentRound();
				civ.undoTechPurchase(currentRound);
				civ.setPurchased(false);
				// this.log(name + " undid the following technology purchases from this round: " + undoneTechs + " (via
				// "
				// + user + ")");
				// this.broadcastMessage(new GameDataMessage(this.game));
				break;
			}
			case "AdvanceAstMessage":
				final HashMap<Name, AstChange> advanceAst = ( (AdvanceAstMessage) message ).getAdvanceAst();
				for (final Civilization.Name name : advanceAst.keySet()) {
					this.game.getCivilization(name).changeAst(advanceAst.get(name));
				}
				this.game.nextRound();
				if (this.game.isGameOver()) {
					this.log("Game is over, sending message to " + this.sessionList.get(session));
					this.sendMessage(session, new GameEndMessage(this.game));
				}
				// this.log("Ast advances triggered by " + user + ": " + advanceAst);
				// this.broadcastMessage(new GameDataMessage(this.game));
				break;
			case "AdditionalCreditMessage": {
				final ArrayList<Technology.Type> credits = ( (AdditionalCreditMessage) message ).getCredits();
				final Technology tech = ( (AdditionalCreditMessage) message ).getTech();
				final Civilization.Name name = ( (AdditionalCreditMessage) message ).getCivName();
				final Civilization civ = this.game.getCivilization(name);
				civ.addTypeCredits(tech, credits);
				// this.log("Additional credits add to " + name.toString() + " by " + user + " for tech " + tech + ": "
				// + credits);
				// this.broadcastMessage(new GameDataMessage(this.game));
				break;
			}
			case "LoadGameMessage":
				this.game = ( (LoadGameMessage) message ).getGame();
				// this.log("Game loaded from save by " + user);
				// this.broadcastMessage(new GameDataMessage(this.game));
				break;
			case "RetireMessage":
				final Civilization.Name name = ( (RetireMessage) message ).getCivName();
				this.game.retireCivilization(name);
				// this.log(name.toString() + " has retired!");
				// this.broadcastMessage(new GameDataMessage(this.game));
				break;
			case "CivEditMessage":
				final Civilization civ = ( (CivEditMessage) message ).getCivilization();
				// Civilization oldCiv = this.game.getCivilization(civ.getName());
				this.game.setCivilization(civ);
				// this.log("Civilization edited by " + user + ":\n" + "Before Edit: " + oldCiv.toFullString() + "\n"
				// + "After Edit: " + civ.toFullString());
				// this.broadcastMessage(new GameDataMessage(this.game));
				break;
			case "SetUserMessage":
				final User newUser = ( (SetUserMessage) message ).getUser();
				this.sessionList.get(session).setUser(newUser);
				// this.log(user.getUserName() + " changed name to " + newUser.getUserName());
				break;
			case "HeartbeatMessage":
				break;
			default:
				this.log("ERROR: Unknown message type received: " + message.getClass().getSimpleName());
				return;
		}
		if (this.game != null) {
			this.game.logEvent(event);
		}
		this.log(event.toString());
		this.broadcastMessage(new GameDataMessage(this.game));
		this.broadcastMessage(new UserListMessage(this.getUserList()));
	}

	public void communicationsError(final Session session, final Throwable throwable) {
		this.log("Error while communicating with " + this.sessionList.get(session).getUser().getUserName() + ":");
		throwable.printStackTrace();
	}

	/**
	 * Print a message with timestamp to the console.
	 *
	 * @param message
	 *            The message
	 */
	public void log(final String message) {
		final LocalDateTime date = LocalDateTime.now();
		System.out.println(GameEvent.dateFormat.format(date) + ": " + message);
	}

	void sendGame(final Session session) {
		this.sendMessage(session, new GameDataMessage(this.game));
	}

	void sendClock(final Session session) {
		final Instant startTime = this.stopwatch.getStartTime();
		if (this.stopwatch.isRunning()) {
			this.sendMessage(session, this.stopwatch.generateTimerMessage(Stopwatch.StopwatchEvent.START, startTime));
		} else {
			this.sendMessage(session, this.stopwatch.generateTimerMessage(Stopwatch.StopwatchEvent.STOP, startTime));
		}
	}

	/**
	 * Send a message to the specified client
	 *
	 * @param session
	 * @param message
	 */
	private void sendMessage(final Session session, final ServerMessage message) {
		if (session == null) {
			return;
		}
		session.getAsyncRemote().sendObject(message);
		// this.log("Sent message to " + sessionList.get(session).getUser());
	}

	private void broadcastMessage(final ServerMessage message) {
		this.sessionList.keySet().parallelStream().forEach(session -> this.sendMessage(session, message));
	}

	public static void main(final String args[]) {
		GameServer server = null;
		if (System.console() == null && !GraphicsEnvironment.isHeadless()) {
			System.out.println("Redirecting");
			final JFrame frame = new JFrame();
			final JPanel panel = new JPanel(new GridLayout());
			frame.add(panel);
			final JTextArea outputArea = new JTextArea(40, 50);
			outputArea.setEditable(false);
			final JScrollPane scrollPane = new JScrollPane(outputArea);

			panel.add(scrollPane);

			final PrintStream output = new PrintStream(new OutputStream() {
				@Override
				public void write(final int arg0) throws IOException {
					// redirects data to the text area
					outputArea.append(String.valueOf((char) arg0));
					// scrolls the text area to the end of data
					outputArea.setCaretPosition(outputArea.getDocument().getLength());
				}
			});

			frame.pack();
			frame.setVisible(true);
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

			System.setOut(output);
			System.setErr(output);
			System.out.println("System.out and System.err redirected here");
		}
		if (args.length > 1) {
			server = new GameServer(args[0], Integer.parseInt(args[1]));
		} else {
			final JTextField hostname = new JTextField("localhost");
			final JSpinner port = new JSpinner(new SpinnerNumberModel(1099, 0, 65535, 1));
			final Object[] message = { "Hostname:", hostname, "Port:", port };
			JOptionPane.showMessageDialog(null, message, "Server Configuration", JOptionPane.PLAIN_MESSAGE);
			server = new GameServer(hostname.getText(), (int) port.getValue());
		}
		try {
			server.start();
			while (server.isRunning) {}
		} catch (final DeploymentException exception) {
			exception.printStackTrace();
			server.stop();
		}
	}

}
