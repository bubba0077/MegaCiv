package net.bubbaland.megaciv.server;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import javax.websocket.DeploymentException;
import javax.websocket.Session;

import org.apache.commons.lang3.text.WordUtils;
import org.glassfish.tyrus.server.Server;

import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Game;
import net.bubbaland.megaciv.game.Game.Difficulty;
import net.bubbaland.megaciv.game.GameEvent;
import net.bubbaland.megaciv.game.Stopwatch;
import net.bubbaland.megaciv.game.StopwatchListener;
import net.bubbaland.megaciv.game.Technology;
import net.bubbaland.megaciv.game.User;
import net.bubbaland.megaciv.game.Civilization.AstChange;
import net.bubbaland.megaciv.game.Civilization.Name;
import net.bubbaland.megaciv.messages.*;
import net.bubbaland.megaciv.messages.TimerMessage.StopwatchEvent;
import net.bubbaland.sntp.SntpServer;

public class GameServer extends Server implements StopwatchListener {

	private Game										game;

	private Server										server;
	private SntpServer									sntpServer;


	private Stopwatch									stopwatch;

	private boolean										isRunning;
	private final int									serverPort;

	private Hashtable<Session, ClientMessageReceiver>	sessionList;

	public GameServer(String serverUrl, int serverPort) {
		this.serverPort = serverPort;
		this.server = new Server(serverUrl, serverPort, "/", null, ClientMessageReceiver.class);
		ClientMessageReceiver.registerServer(this);
		this.sntpServer = new SntpServer(serverPort + 1);
		this.sessionList = new Hashtable<Session, ClientMessageReceiver>();
		this.stopwatch = new Stopwatch();
		this.stopwatch.addStopwatchListener(this);
	}

	public void start() throws DeploymentException {
		this.server.start();
		this.sntpServer.run();
		this.isRunning = true;
	}

	public void stop() {
		server.stop();
		this.sntpServer = new SntpServer(serverPort + 1);
		this.isRunning = false;
	}

	public void addSession(Session session, ClientMessageReceiver endpoint) {
		this.log("New client connecting...");
		this.sessionList.put(session, endpoint);
		this.broadcastMessage(new UserListMessage(this.getUserList()));
	}

	private ArrayList<User> getUserList() {
		ArrayList<User> userList = new ArrayList<User>() {
			private static final long serialVersionUID = 5052692806073959873L;

			{
				for (ClientMessageReceiver endpoint : GameServer.this.sessionList.values()) {
					add(endpoint.getUser());
				}
			}
		};
		return userList;
	}

	public void removeSession(Session session) {
		this.log(this.sessionList.get(session).getUser() + " disconnected");
		this.sessionList.remove(session);
		this.broadcastMessage(new UserListMessage(this.getUserList()));
	}

	public void processIncomingMessage(ClientMessage message, Session session) {
		String messageType = message.getClass().getSimpleName();
		User user = this.sessionList.get(session).getUser();
		user.updateActivity();
		GameEvent event = new GameEvent(message.getEventType(), user, message.toString());
		switch (messageType) {
			case "TimerMessage":
				this.stopwatch.remoteEvent((TimerMessage) message, Duration.ZERO);
				break;
			case "NewGameMessage":
				this.game = new Game();
				HashMap<Civilization.Name, String> startingCivs = ( (NewGameMessage) message ).getCivNames();
				this.game.addCivilization(new ArrayList<Civilization.Name>(startingCivs.keySet()));
				for (Civilization.Name name : startingCivs.keySet()) {
					this.game.getCivilization(name).setPlayer(startingCivs.get(name));
				}
				if (( (NewGameMessage) message ).useCredits()) {
					this.game.assignStartCredits();
				}
				Difficulty difficulty = ( (NewGameMessage) message ).getDifficulty();
				this.game.setDifficulty(difficulty);
				Civilization.Region region = ( (NewGameMessage) message ).getRegion();
				this.game.setRegion(region);

				this.log(user + " created new " + WordUtils.capitalizeFully(difficulty.toString())
						+ " game with the following civilizations: " + startingCivs);
				// this.broadcastMessage(new GameDataMessage(this.game));
				break;
			// case "AssignPlayerMessage": {
			// Civilization civ = this.game.getCivilization(( (AssignPlayerMessage) message ).getCivilizationName());
			// String player = ( (AssignPlayerMessage) message ).getPlayer();
			// civ.setPlayer(player);
			// this.log(user + "assigned " + civ.getName() + " to " + player);
			// // this.broadcastMessage(new GameDataMessage(this.game));
			// break;
			// }
			case "CensusMessage":
				HashMap<Civilization.Name, Integer> census = ( (CensusMessage) message ).getCensus();
				for (Civilization.Name name : census.keySet()) {
					this.log(name + " " + census.get(name) + " " + this.game.getCivilization(name));
					this.game.getCivilization(name).setPopulation(census.get(name));
				}
				// this.broadcastMessage(new GameDataMessage(this.game));
				this.log("Census reported by " + user + ": " + census);
				break;
			case "CityUpdateMessage":
				HashMap<Civilization.Name, Integer> cityCount = ( (CityUpdateMessage) message ).getCityCount();
				for (Civilization.Name name : cityCount.keySet()) {
					this.game.getCivilization(name).setCityCount(cityCount.get(name));
				}
				// this.broadcastMessage(new GameDataMessage(this.game));
				this.log("City counts updated by " + user + ": " + cityCount);
				break;
			case "TechPurchaseMessage": {
				Civilization.Name name = ( (TechPurchaseMessage) message ).getCivName();
				Civilization civ = this.game.getCivilization(name);
				ArrayList<Technology> newTechs = ( (TechPurchaseMessage) message ).getTechs();
				for (Technology newTech : newTechs) {
					civ.addTech(newTech, this.game.getCurrentRound());
				}
				civ.setPurchased(true);
				this.log(name + " bought the following technologies: " + newTechs + " (via " + user + ")");
				// this.broadcastMessage(new GameDataMessage(this.game));
				break;
			}
			case "UndoPurchaseMessage": {
				Civilization.Name name = ( (UndoPurchaseMessage) message ).getCivName();
				Civilization civ = this.game.getCivilization(name);
				int currentRound = this.game.getCurrentRound();
				ArrayList<Technology> undoneTechs = civ.undoTechPurchase(currentRound);
				civ.setPurchased(false);
				this.log(name + " undid the following technology purchases from this round: " + undoneTechs + " (via "
						+ user + ")");
				// this.broadcastMessage(new GameDataMessage(this.game));
				break;
			}
			case "AdvanceAstMessage":
				final HashMap<Name, AstChange> advanceAst = ( (AdvanceAstMessage) message ).getAdvanceAst();
				for (Civilization.Name name : advanceAst.keySet()) {
					this.game.getCivilization(name).changeAst(advanceAst.get(name));
				}
				this.game.nextRound();
				this.log("Ast advances triggered by " + user + ": " + advanceAst);
				// this.broadcastMessage(new GameDataMessage(this.game));
				break;
			case "AdditionalCreditMessage": {
				ArrayList<Technology.Type> credits = ( (AdditionalCreditMessage) message ).getCredits();
				Technology tech = ( (AdditionalCreditMessage) message ).getTech();
				Civilization.Name name = ( (AdditionalCreditMessage) message ).getCivName();
				Civilization civ = this.game.getCivilization(name);
				civ.addTypeCredits(tech, credits);
				this.log("Additional credits add to " + name.toString() + " by " + user + " for tech " + tech + ": "
						+ credits);
				// this.broadcastMessage(new GameDataMessage(this.game));
				break;
			}
			case "LoadGameMessage":
				this.game = ( (LoadGameMessage) message ).getGame();
				this.log("Game loaded from save by " + user);
				// this.broadcastMessage(new GameDataMessage(this.game));
				break;
			case "RetireMessage":
				Civilization.Name name = ( (RetireMessage) message ).getCivName();
				this.game.retireCivilization(name);
				this.log(name.toString() + " has retired!");
				// this.broadcastMessage(new GameDataMessage(this.game));
				break;
			case "CivEditMessage":
				Civilization civ = ( (CivEditMessage) message ).getCivilization();
				Civilization oldCiv = this.game.getCivilization(civ.getName());
				this.game.setCivilization(civ);
				this.log("Civilization edited by " + user + ":\n" + "Before Edit: " + oldCiv.toFullString() + "\n"
						+ "After Edit: " + civ.toFullString());
				// this.broadcastMessage(new GameDataMessage(this.game));
				break;
			case "SetUserMessage":
				User newUser = ( (SetUserMessage) message ).getUser();
				this.sessionList.get(session).setUser(newUser);
				this.log(user.getUserName() + " changed name to " + newUser.getUserName());
				break;
			default:
				this.log("ERROR: Unknown Message Type Received!");
				return;
		}
		if (game != null) {
			this.game.logEvent(event);
		}
		if (event.toString() != null) {
			this.log(event.toString());
		} else {
			System.out.println("toString not yet defined for " + event.getType());
		}
		this.broadcastMessage(new GameDataMessage(this.game));
		this.broadcastMessage(new UserListMessage(this.getUserList()));
	}

	public void communicationsError(Session session, Throwable throwable) {
		this.log("Error while communicating with " + this.sessionList.get(session).getUser().getUserName() + ":");
		throwable.printStackTrace();
	}

	/**
	 * Print a message with timestamp to the console.
	 *
	 * @param message
	 *            The message
	 */
	public void log(String message) {
		final LocalDateTime date = LocalDateTime.now();
		System.out.println(GameEvent.dateFormat.format(date) + ": " + message);
	}

	void sendGame(Session session) {
		this.sendMessage(session, new GameDataMessage(this.game));
	}

	void sendClock(Session session) {
		Instant lastEventTime = this.stopwatch.getLastEventTime();
		Duration timerLength = this.stopwatch.getTimerLength();
		Duration lastDeciseconds = this.stopwatch.getLastEventTic();
		this.sendMessage(session,
				new TimerMessage(TimerMessage.StopwatchEvent.SET, lastEventTime, timerLength, lastDeciseconds));
		this.sendMessage(session, new TimerMessage(TimerMessage.StopwatchEvent.SET_LAST_TIC, lastEventTime, timerLength,
				lastDeciseconds));
		if (this.stopwatch.isRunning()) {
			this.sendMessage(session,
					new TimerMessage(TimerMessage.StopwatchEvent.START, lastEventTime, timerLength, lastDeciseconds));
		}
	}

	/**
	 * Send a message to the specified client
	 *
	 * @param session
	 * @param message
	 */
	private void sendMessage(Session session, ServerMessage message) {
		if (session == null) return;
		session.getAsyncRemote().sendObject(message);
		// this.log("Sent message to " + sessionList.get(session).getUser());
	}

	private void broadcastMessage(ServerMessage message) {
		for (Session session : this.sessionList.keySet()) {
			this.sendMessage(session, message);
		}
	}

	private void broadcastTimeMessage(TimerMessage.StopwatchEvent action, Instant eventTime, Duration timerLength,
			Duration lastDeciseconds) {
		for (Session session : this.sessionList.keySet()) {
			this.sendMessage(session, new TimerMessage(action, eventTime, timerLength, lastDeciseconds));
		}
	}

	public static void main(String args[]) {
		GameServer server = null;
		if (args.length > 1) {
			server = new GameServer(args[0], Integer.parseInt(args[1]));
		} else {
			server = new GameServer("localhost", 1200);
		}
		try {
			server.start();
			while (server.isRunning) {}
		} catch (final DeploymentException exception) {
			exception.printStackTrace();
			server.stop();
		}
	}

	@Override
	public void tic(Duration tics) {}

	@Override
	public void watchStarted() {
		this.broadcastTimeMessage(StopwatchEvent.START, this.stopwatch.getLastEventTime(),
				this.stopwatch.getTimerLength(), this.stopwatch.getLastEventTic());
	}

	@Override
	public void watchStopped() {
		this.broadcastTimeMessage(StopwatchEvent.STOP, this.stopwatch.getLastEventTime(),
				this.stopwatch.getTimerLength(), this.stopwatch.getLastEventTic());
	}

	@Override
	public void watchReset() {
		this.broadcastTimeMessage(StopwatchEvent.RESET, this.stopwatch.getLastEventTime(),
				this.stopwatch.getTimerLength(), this.stopwatch.getLastEventTic());
	}

}
