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

@ClientEndpoint(decoders = { ServerMessage.MessageDecoder.class }, encoders = { ClientMessage.MessageEncoder.class })
public class GuiClient extends GameClient {

	private final GuiController gui;

	public GuiClient(final String serverURL, GuiController gui) {
		super(serverURL);
		this.gui = gui;
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
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				GuiClient.this.gui.updateGui(true);
			}
		});
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

	public void log(String message) {
		super.log(message);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				GuiClient.this.gui.setStatusBarText(message);
			}
		});
	}

}
