package net.bubbaland.megaciv.client;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import net.bubbaland.megaciv.messages.*;

@ServerEndpoint(decoders = { ClientMessage.MessageDecoder.class }, encoders = {
		ServerMessage.MessageEncoder.class }, value = "/")
public class ClientMessageReceiver {

	private GameClient client;

	public ClientMessageReceiver(GameClient client) {
		this.client = client;
	}

	/**
	 * Initial hook when a client first connects (TriviaServerEndpoint() is automatically called as well)
	 *
	 * @param session
	 * @param config
	 */
	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
	}

	/**
	 * Handle a message from the client
	 *
	 * @param message
	 * @param session
	 */
	@OnMessage
	public void onMessage(ClientMessage message, Session session) {
		client.processIncomingMessage(message, session);
	}

	/**
	 * Handle error in communicating with a client
	 *
	 * @param session
	 */
	@OnError
	public void onError(Session session, Throwable throwable) {
		client.communicationsError(session, throwable);
	}

	/**
	 * Handle a client disconnection
	 *
	 * @param session
	 */
	@OnClose
	public void onClose(Session session) {
		client.connectionClosed();
	}

}
