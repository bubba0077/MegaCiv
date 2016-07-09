package net.bubbaland.megaciv.server;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import javax.websocket.DeploymentException;
import javax.websocket.Session;

import org.glassfish.tyrus.server.Server;

import net.bubbaland.megaciv.game.Game;
import net.bubbaland.megaciv.messages.client.*;
import net.bubbaland.megaciv.messages.server.*;

public class GameServer extends Server {

	private Game										game;

	private Server										server;

	private boolean										isRunning;

	private Hashtable<Session, ClientMessageReceiver>	sessionList;

	// Date format to use inside backup files
	static public final SimpleDateFormat				stringDateFormat	= new SimpleDateFormat(
			"yyyy MMM dd HH:mm:ss");

	public GameServer(String serverUrl, int serverPort) {
		this.server = new Server(serverUrl, serverPort, "/", null, ClientMessageReceiver.class);
		ClientMessageReceiver.registerServer(this);
		this.sessionList = new Hashtable<Session, ClientMessageReceiver>();
	}

	public void start() throws DeploymentException {
		server.start();
		this.isRunning = true;
	}

	public void stop() {
		server.stop();
		this.isRunning = false;
	}

	public void addUser(Session session, ClientMessageReceiver endpoint) {
		this.log("New client connecting...");
		this.sessionList.put(session, endpoint);
	}

	public void removeUser(Session session) {
		this.log(this.sessionList.get(session).getUser() + " disconnected");
		this.sessionList.remove(session);
	}

	public void processIncomingMessage(ClientMessage message, Session session) {
		// TODO Auto-generated method stub
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
		final Date date = new Date();
		System.out.println(stringDateFormat.format(date) + ": " + message);
	}

	void sendGame(Session session) {
		this.sendMessage(session, new GameDataMessage(this.game));
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
		this.log("Sent message to " + sessionList.get(session).getUser());
	}

	public static void main(String args[]) {
		GameServer server = new GameServer("localhost", 1100);
		try {
			server.start();
			while (server.isRunning) {
			}
		} catch (final DeploymentException exception) {
			exception.printStackTrace();
			server.stop();
		}
	}

}
