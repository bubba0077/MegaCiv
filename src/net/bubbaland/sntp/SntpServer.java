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

	private final int		port;

	private DatagramPacket	packet;
	private DatagramSocket	socket;

	public void run() {
		try {
			this.response = new SntpMessage();
			this.response.setStratum((byte) 1);
			this.response.setPrecision((byte) -6);
			this.response.setDelay(0.0);
			this.response.setRefId("LOCL".getBytes());
			this.buffer = this.response.toByteArray();

			System.out.println("SNTP Server started on port " + this.port + "!");
			this.packet = new DatagramPacket(this.buffer, this.buffer.length);
			this.socket = new DatagramSocket(this.port);

			while (true) {
				this.socket.receive(this.packet);

				// System.out.println("SNTP Request from " + packet.getAddress() + ":" + packet.getPort());

				this.buffer = this.packet.getData();
				this.request = new SntpMessage(this.buffer);

				this.response.setOrgTime(this.request.getXmtTime());
				this.response.setRecTime(NtpTimestamp.now());
				this.response.setXmtTime(NtpTimestamp.now());

				this.buffer = this.response.toByteArray();
				final DatagramPacket resp = new DatagramPacket(this.buffer, this.buffer.length,
						this.packet.getAddress(), this.packet.getPort());
				this.socket.send(resp);
			}
		}

		catch (final Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public SntpServer(final int port) {
		this.port = port;
	}

}
