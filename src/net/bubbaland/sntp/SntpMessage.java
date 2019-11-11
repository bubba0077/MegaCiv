/*******************************************************************************
 * File: Message.java Author: Morteza Ansarinia <ansarinia@me.com> Created on: November 9, 2013 Project: No Time
 * Protocol <http://time.onto.ir> Copyright: See the file "LICENSE" for the full license governing this code.
 *******************************************************************************/
package net.bubbaland.sntp;

import java.util.Arrays;

/**
 * SNTP v4 - RFC 2030 More info: http://www.eecis.udel.edu/~mills/database/reports/ntp4/ntp4.pdf
 */

/**
 * RFC 2030,Section 4
 *
 * 1 2 3 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ |LI | VN |Mode | Stratum | Poll | Precision |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ | Root Delay |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ | Root Dispersion |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ | Reference Identifier |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ | | | Reference Timestamp (64) | | |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ | | | Originate Timestamp (64) | | |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ | | | Receive Timestamp (64) | | |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ | | | Transmit Timestamp (64) | | |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ | Key Identifier (optional) (32) |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ | | | | | Message Digest (optional) (128) | | | | |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */

public class SntpMessage {

	/** Leap Indicator (2 bits). */
	private byte	leap;

	/** Version Number (3 bits). */
	private byte	version;

	/**
	 * @param version
	 *            the version to set
	 */
	public void setVersion(final byte version) {
		this.version = version;
	}

	/**
	 * @param mode
	 *            the mode to set
	 */
	public void setMode(final byte mode) {
		this.mode = mode;
	}

	/**
	 * @param stratum
	 *            the stratum to set
	 */
	public void setStratum(final byte stratum) {
		this.stratum = stratum;
	}

	/**
	 * @param refId
	 *            the refId to set
	 */
	public void setRefId(final byte[] refId) {}

	/**
	 * @param xmtTime
	 *            the xmtTime to set
	 */
	public void setXmtTime(final NtpTimestamp xmtTime) {
		this.xmtTime = xmtTime;
	}

	/** Mode (3 bits). */
	private byte	mode;

	/** Stratum. */
	private byte	stratum;

	/** Poll Interval. */
	private byte	poll;

	/** Precision. */
	private byte	precision;

	/**
	 * @param leap
	 *            the leap to set
	 */
	public void setLeap(final byte leap) {
		this.leap = leap;
	}

	/**
	 * @param poll
	 *            the poll to set
	 */
	public void setPoll(final byte poll) {
		this.poll = poll;
	}

	/**
	 * @param precision
	 *            the precision to set
	 */
	public void setPrecision(final byte precision) {
		this.precision = precision;
	}

	/**
	 * @param delay
	 *            the delay to set
	 */
	public void setDelay(final double delay) {}

	/**
	 * @param dispersion
	 *            the dispersion to set
	 */
	public void setDispersion(final double dispersion) {}

	/**
	 * @param refTime
	 *            the refTime to set
	 */
	public void setRefTime(final NtpTimestamp refTime) {
		this.refTime = refTime;
	}

	/**
	 * @param orgTime
	 *            the orgTime to set
	 */
	public void setOrgTime(final NtpTimestamp orgTime) {
		this.orgTime = orgTime;
	}

	/**
	 * @param recTime
	 *            the recTime to set
	 */
	public void setRecTime(final NtpTimestamp recTime) {
		this.recTime = recTime;
	}

	/**
	 * @param dstTime
	 *            the dstTime to set
	 */
	public void setDstTime(final NtpTimestamp dstTime) {}

	/** Reference Timestamp (NTP format). */
	@SuppressWarnings("unused")
	private NtpTimestamp	refTime;

	/** Originate Timestamp (NTP format). */
	private NtpTimestamp	orgTime;

	/** Receive Timestamp (NTP format). */
	private NtpTimestamp	recTime;

	/**
	 * @return the orgTime
	 */
	public NtpTimestamp getOrgTime() {
		return this.orgTime;
	}

	/**
	 * @return the recTime
	 */
	public NtpTimestamp getRecTime() {
		return this.recTime;
	}

	/**
	 * @return the xmtTime
	 */
	public NtpTimestamp getXmtTime() {
		return this.xmtTime;
	}

	/** Transmit Timestamp (NTP format). */
	private NtpTimestamp xmtTime;

	public SntpMessage() {

	}

	public SntpMessage(final byte[] data) {
		this.leap = (byte) ( data[0] >> 6 );
		this.version = (byte) ( ( data[0] & 0x38 ) >> 3 );
		this.mode = (byte) ( data[0] & 0x07 );
		this.stratum = data[1];
		this.poll = data[2];
		this.precision = data[3];
		this.refTime = new NtpTimestamp(Arrays.copyOfRange(data, 16, 24));
		this.orgTime = new NtpTimestamp(Arrays.copyOfRange(data, 24, 32));
		this.recTime = new NtpTimestamp(Arrays.copyOfRange(data, 32, 40));
		this.xmtTime = new NtpTimestamp(Arrays.copyOfRange(data, 40, 48));
	}

	public byte[] toByteArray() {
		final byte[] message = new byte[48];

		message[0] = (byte) ( this.leap << 6 | this.version << 3 | this.mode );
		message[1] = this.stratum;
		message[2] = this.poll;
		message[3] = this.precision;

		byte[] bOrigin = null;
		byte[] bRecv = null;
		byte[] bTrans = null;

		if (this.orgTime != null) {
			bOrigin = this.orgTime.toByteArray();
		}
		if (this.recTime != null) {
			bRecv = this.recTime.toByteArray();
		}
		if (this.xmtTime != null) {
			bTrans = this.xmtTime.toByteArray();
		}

		for (int i = 0; i < 8; i++) {
			message[24 + i] = message[32 + i] = message[32 + i] = 0;

			if (bOrigin != null) {
				message[24 + i] = bOrigin[i];
			}
			if (bRecv != null) {
				message[32 + i] = bRecv[i];
			}
			if (bTrans != null) {
				message[40 + i] = bTrans[i];
			}
		}

		return message;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("SNTP Message(").append(this.version).append(")");
		// TODO
		return sb.toString();
	}

}
