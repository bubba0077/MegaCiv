package net.bubbaland.megaciv.client;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.websocket.Session;

import net.bubbaland.megaciv.messages.ClientMessage;

public class GameClient {

	// Format for log timestamps
	private static SimpleDateFormat TIMESTAMP_FORMAT;

	public GameClient() {
	}

	public void processIncomingMessage(ClientMessage message, Session session) {
		// TODO Auto-generated method stub
	}

	public void communicationsError(Session session, Throwable throwable) {
		// TODO Auto-generated method stub
	}

	public void connectionClosed() {
		// TODO Auto-generated method stub
	}

	/**
	 * Display message in the status bar and in console
	 *
	 * @param message
	 *            Message to log
	 */
	public void log(String message) {
		final String timestamp = TIMESTAMP_FORMAT.format(new Date());
		// Print message to console
		System.out.println(timestamp + " " + message);
	}

}
