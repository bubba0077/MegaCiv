package net.bubbaland.megaciv.server;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import net.bubbaland.megaciv.*;
import net.bubbaland.megaciv.messages.ClientMessage;
import net.bubbaland.megaciv.messages.ServerMessage;

@ServerEndpoint(decoders = { ClientMessage.MessageDecoder.class }, encoders = {
		ServerMessage.MessageEncoder.class }, value = "/")
public class ServerMessageReceiver {

	private User				user;

	private static GameServer	server	= new GameServer();

	public ServerMessageReceiver() {
		this.user = new User();
	}

	public User getUser() {
		return this.user;
	}

	/**
	 * Initial hook when a client first connects (TriviaServerEndpoint() is automatically called as well)
	 *
	 * @param session
	 * @param config
	 */
	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		server.addUser(session, this);
	}

	/**
	 * Handle a message from the client
	 *
	 * @param message
	 * @param session
	 */
	@OnMessage
	public void onMessage(ClientMessage message, Session session) {
		server.processIncomingMessage(message, session);
	}

	/**
	 * Handle error in communicating with a client
	 *
	 * @param session
	 */
	@OnError
	public void onError(Session session, Throwable throwable) {
		server.communicationsError(session, throwable);
	}

	/**
	 * Handle a client disconnection
	 *
	 * @param session
	 */
	@OnClose
	public void onClose(Session session) {
		server.removeUser(session);
	}

}
