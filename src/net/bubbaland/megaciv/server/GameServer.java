package net.bubbaland.megaciv.server;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import javax.websocket.DeploymentException;
import javax.websocket.Session;

import org.glassfish.tyrus.server.Server;

import net.bubbaland.megaciv.*;

public class GameServer extends Server {

	private Game										game;

	private boolean										isRunning;

	private Hashtable<Session, ServerMessageReceiver>	sessionList;

	// Date format to use inside backup files
	static public final SimpleDateFormat				stringDateFormat	= new SimpleDateFormat(
			"yyyy MMM dd HH:mm:ss");

	public void addUser(Session session, ServerMessageReceiver endpoint) {
		GameServer.log("New client connecting...");
		this.sessionList.put(session, endpoint);
	}

	public void removeUser(Session session) {
		GameServer.log(this.sessionList.get(session).getUser() + " disconnected");
		this.sessionList.remove(session);
	}

	public void processIncomingMessage(ClientMessage message, Session session) {
		// TODO Auto-generated method stub
	}

	public void communicationsError(Session session, Throwable throwable) {
		GameServer.log("Error while communicating with " + this.sessionList.get(session).getUser().getUserName() + ":");
		throwable.printStackTrace();
	}

	/**
	 * Print a message with timestamp to the console.
	 *
	 * @param message
	 *            The message
	 */
	public static void log(String message) {
		final Date date = new Date();
		System.out.println(stringDateFormat.format(date) + ": " + message);
	}

	public static void main(String args[]) {
		GameServer server = new GameServer();
		try {
			server.start();
			server.isRunning = true;
			while (server.isRunning) {
			}
		} catch (final DeploymentException exception) {
			exception.printStackTrace();
			server.stop();
		}
	}

}
