package net.bubbaland.megaciv.client.gui;

import javax.swing.SwingUtilities;
import javax.websocket.ClientEndpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import net.bubbaland.megaciv.client.GameClient;
import net.bubbaland.megaciv.messages.*;
import net.bubbaland.megaciv.messages.client.ClientMessage;
import net.bubbaland.megaciv.messages.server.ServerMessage;

@ClientEndpoint(decoders = { ServerMessage.MessageDecoder.class }, encoders = { ClientMessage.MessageEncoder.class })
public class GuiClient extends GameClient {

	private final GuiController gui;

	public GuiClient(final String serverURL) {
		super(serverURL);
		this.gui = new GuiController(this);
	}

	@OnClose
	public void connectionClosed() {
		super.connectionClosed();
		this.gui.endProgram();
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		super.onOpen(session, config);
	}

	@OnMessage
	public void onMessage(ServerMessage message, Session session) {
		super.onMessage(message, session);
	}

	/**
	 * Handle error in communicating with a client
	 *
	 * @param session
	 */
	@OnError
	public void onError(Session session, Throwable throwable) {
		super.onError(session, throwable);
	}

	public static void main(String[] args) {
		// Schedule a job to create and show the GUI
		final String serverURL;
		if (args.length > 0) {
			serverURL = args[0];
		} else {
			serverURL = "ws://localhost:1100";
		}
		new GuiClient(serverURL);
	}

	public void log(String message) {
		super.log(message);
		if (this.gui != null) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					GuiClient.this.gui.log(message);
				}
			});
		}
	}

}
