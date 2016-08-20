package net.bubbaland.sntp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

/**
 * SNTP client algorithm, specified in RFC 4330.
 */
public class SntpClient {

	/**
	 * @return the offset
	 */
	public long getOffset() {
		return (long) this.offset;
	}

	// Weight of the most recent packet on the offset
	private static double	avgWeight	= 0.5;

	private int				port;
	private int				pollInterval;
	private String			host;

	private double			offset;
	private Timer			timer;

	public SntpClient(String host, int port, int pollInterval) {
		this.port = port;
		this.host = new String(host);
		this.pollInterval = pollInterval;

		this.offset = -1;
		this.timer = null;
	}

	public void start() {
		this.timer = new Timer();

		this.timer.schedule(new TimerTask() {
			public void run() {
				try {
					SntpClient.this.sync();
				} catch (Exception exception) {
					System.out.println("Error communicating with SNTP server");
					exception.printStackTrace();
				}
			}
		}, 0, this.pollInterval);
	}

	public void stop() {
		this.timer.cancel();
		this.timer = null;
	}

	public void sync() throws Exception {
		if (port <= 0 || host == null) {
			throw new IllegalArgumentException("Invalid parameters!");
		}

		final DatagramSocket socket = new DatagramSocket();
		socket.setSoTimeout(0);
		final InetAddress hostAddr = InetAddress.getByName(host);

		// Create request
		SntpMessage req = new SntpMessage();
		req.setVersion((byte) 4);
		req.setMode((byte) 3); // client
		req.setStratum((byte) 3);
		req.setRefId("LOCL".getBytes());
		req.setXmtTime(NtpTimestamp.now()); // returns as originate timestamp
		final byte[] buffer = req.toByteArray();
		req = null;

		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, hostAddr, port);
		socket.send(packet);

		packet = new DatagramPacket(buffer, buffer.length);
		socket.receive(packet);

		// Set the time response arrived
		final NtpTimestamp destTime = NtpTimestamp.now();

		SntpMessage resp = new SntpMessage(packet.getData());
		socket.close();

		// Timestamp Name ID When Generated
		// ------------------------------------------------------------
		// Originate Timestamp T1 time request sent by client
		// Receive Timestamp T2 time request received by server
		// Transmit Timestamp T3 time reply sent by server
		// Destination Timestamp T4 time reply received by client
		//
		// The roundtrip delay d, and system clock offset t are defined as:
		//
		// d = (T4 - T1) - (T3 - T2) t = ((T2 - T1) + (T3 - T4)) / 2

		double t =
				( ( resp.getRecTime().value - resp.getOrgTime().value ) + ( resp.getXmtTime().value - destTime.value ) )
						/ 2;

		// System clock offset in millis
		long newOffset = (long) ( t * 1000 );
		if (this.offset == -1) {
			this.offset = newOffset;
		} else {
			this.offset = ( 1 - avgWeight ) * this.offset + avgWeight * newOffset;
		}

		// System.out.println("Average Offset: " + this.offset + " ms");
	}


	public int getPollInteval() {
		return pollInterval;
	}


	public void setPollInteval(int pollInteval) {
		this.pollInterval = pollInteval;
	}

}