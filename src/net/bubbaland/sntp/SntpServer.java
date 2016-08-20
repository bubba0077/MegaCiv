/*******************************************************************************
 * File: Server.java Author: Morteza Ansarinia <ansarinia@me.com> Created on: November 9, 2013 Project: No Time Protocol
 * <http://time.onto.ir> Copyright: See the file "LICENSE" for the full license governing this code.
 *******************************************************************************/
package net.bubbaland.sntp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class SntpServer {

	private SntpMessage		response;
	private SntpMessage		request;
	private byte[]			buffer;

	private int				port;

	private DatagramPacket	packet;
	private DatagramSocket	socket;

	public void run() {
		try {
			response = new SntpMessage();
			response.setStratum((byte) 1);
			response.setPrecision((byte) -6);
			response.setDelay(0.0);
			response.setRefId("LOCL".getBytes());
			buffer = response.toByteArray();

			System.out.println("SNTP Server started!");
			packet = new DatagramPacket(buffer, buffer.length);
			socket = new DatagramSocket(port);

			while (true) {
				socket.receive(packet);

				System.out.println("SNTP Request from " + packet.getAddress() + ":" + packet.getPort());

				buffer = packet.getData();
				request = new SntpMessage(buffer);

				response.setOrgTime(request.getXmtTime());
				response.setRecTime(NtpTimestamp.now());
				response.setXmtTime(NtpTimestamp.now());

				buffer = response.toByteArray();
				DatagramPacket resp = new DatagramPacket(buffer, buffer.length, packet.getAddress(), packet.getPort());
				socket.send(resp);
			}
		}

		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public SntpServer(int port) {
		this.port = port;
	}

}
