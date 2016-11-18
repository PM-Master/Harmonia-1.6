/* This software was developed by employees of the National Institute of
 * Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 15 United States Code Section 105, works of NIST
 * employees are not subject to copyright protection in the United States
 * and are considered to be in the public domain.  As a result, a formal
 * license is not needed to use the software.
 * 
 * This software is provided by NIST as a service and is expressly
 * provided "AS IS".  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
 * OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
 * AND DATA ACCURACY.  NIST does not warrant or make any representations
 * regarding the use of the software or the results thereof including, but
 * not limited to, the correctness, accuracy, reliability or usefulness of
 * the software.
 * 
 * Permission to use this software is contingent upon your acceptance
 * of the terms of this agreement.
 */
package gov.nist.csd.pm.common.net;

import java.nio.ByteBuffer;

/**
 * This class implements byte utility methods.
 * 
 * @author steveq@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:57 $
 * @since 1.5
 */
public class ByteUtil {

	/***************************************************************************
	 * Constants
	 **************************************************************************/

	/** The maximum number of bytes in a UDP packet. */
	public static final int MAX_UDP_PACKET_SIZE = 65537;

	/** Number of bytes in a Java short. */
	public static final int NUM_BYTES_IN_SHORT = 2;

	/** Number of bytes in a Java int. */
	public static final int NUM_BYTES_IN_INT = 4;

	/** Number of bytes in a Java long. */
	public static final int NUM_BYTES_IN_LONG = 8;

	/** Maximum cache value available. */
	private static long maxValueCache[] = new long[64];

	static {
		for (int i = 1; i < 64; i++) {
			maxValueCache[i] = (long) (((long) 1 << i) - 1);
		}
	}

	/***************************************************************************
	 * Methods
	 **************************************************************************/

	/**
	 * Convert a Java short to a 2-byte array.
	 * 
	 * @param s
	 *            A short value.
	 * @return A 2-byte array representing the short value.
	 */
	public static byte[] shortToBytes(short s) {

		ByteBuffer byteBuffer = ByteBuffer.allocate(NUM_BYTES_IN_SHORT);
		byteBuffer.putShort(s);
		return byteBuffer.array();

	}

	/**
	 * Convert a byte array to a Java short.
	 * 
	 * @param bytes
	 *            A byte array.
	 * @return A Java short.
	 */
	public static short bytesToShort(byte[] bytes) {

		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		return byteBuffer.getShort();

	}

	/**
	 * Convert a short to a hex representation.
	 * 
	 * @param s
	 *            A Java short.
	 * @return A hex representation of the short.
	 */
	public static String shortToHex(short s) {

		// Java doesn't have a method for converting a short to hex, so
		// use an int instead.
		int i = (int) s;
		return Integer.toHexString(i);

	}

	/**
	 * Convert a hex representation to a Java short.
	 * 
	 * @param s
	 *            A hex representation.
	 * @return A Java short.
	 */
	public static short hexToShort(String s) {

		return Short.parseShort(s, 16);

	}

	/**
	 * Convert a Java int to a 4-byte array.
	 * 
	 * @param i
	 *            A Java int value.
	 * @return A 4-byte array representing the int value.
	 */
	public static byte[] intToBytes(int i) {

		ByteBuffer byteBuffer = ByteBuffer.allocate(NUM_BYTES_IN_INT);
		byteBuffer.putInt(i);
		return byteBuffer.array();

	}

	/**
	 * Convert a byte array to a Java int.
	 * 
	 * @param bytes
	 *            A byte array.
	 * @return A Java int.
	 */
	public static int bytesToInt(byte[] bytes) {

		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		return byteBuffer.getInt();

	}

	/**
	 * Convert an int to a hex representation.
	 * 
	 * @param i A Java int.
	 * @return A hex representation of the int.
	 */
	public static String intToHex(int i) {

		return Integer.toHexString(i);

	}

	/**
	 * Convert a hex representation to a Java int.
	 * 
	 * @param s
	 *            A hex representation.
	 * @return A Java int.
	 */
	public static int hexToInt(String s) {

		return Integer.parseInt(s, 16);

	}

	/**
	 * Convert a Java long to a 8-byte array.
	 * 
	 * @param l
	 *            A Java long value.
	 * @return A 8-byte array representing the int value.
	 */
	public static byte[] longToBytes(long l) {

		ByteBuffer byteBuffer = ByteBuffer.allocate(NUM_BYTES_IN_LONG);
		byteBuffer.putLong(l);
		return byteBuffer.array();

	}

	/**
	 * Convert a byte array to a Java long.
	 * 
	 * @param bytes
	 *            A byte array.
	 * @return A Java long.
	 */
	public static long bytesToLong(byte[] bytes) {

		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		return byteBuffer.getLong();

	}

	/**
	 * Convert a long to a hex representation.
	 * 
	 * @param l A Java long.
	 * @return A hex representation of the long.
	 */
	public static String longToHex(long l) {

		return Long.toHexString(l);

	}

	/**
	 * Convert a hex representation to a Java long.
	 * 
	 * @param s
	 *            A hex representation.
	 * @return A Java long.
	 */
	public static long hexToLong(String s) {

		return Long.parseLong(s, 16);

	}

	/**
	 * Get a byte array in a printable binary form.
	 * 
	 * @param bytes
	 *            The byte to be writen.
	 * @return A String reprentation of the byte.
	 */
	public static String writeBytes(byte[] bytes) {

		StringBuffer stringBuffer = new StringBuffer();

		for (int i = 0; i < bytes.length; i++) {

			// New line every 4 bytes
			if (i % 4 == 0) {

				stringBuffer.append("\n");
			}

			stringBuffer.append(writeBits(bytes[i]) + " ");
		}

		return stringBuffer.toString();

	}

	/**
	 * Get a byte array in a printable binary form.
	 * 
	 * @param bytes
	 *            The byte to be writen.
	 * @return A String reprentation of the byte.
	 */
	public static String writeBytes(byte[] bytes, int packetLength) {

		StringBuffer stringBuffer = new StringBuffer();

		for (int i = 0; i < packetLength; i++) {

			// New line every 4 bytes
			if (i % 4 == 0) {

				stringBuffer.append("\n");
			}

			stringBuffer.append(writeBits(bytes[i]) + " ");

		}

		return stringBuffer.toString();

	}

	/**
	 * Get a byte in a printable binary form.
	 * 
	 * @param b
	 *            The byte to be writen.
	 * @return A String reprentation of the byte.
	 */
	public static String writeBits(byte b) {

		StringBuffer stringBuffer = new StringBuffer();
		int bit = 0;

		for (int i = 7; i >= 0; i--) {

			bit = (b >>> i) & 0x01;
			stringBuffer.append(bit);

		}

		return stringBuffer.toString();

	}

	/**
	 * Get the maximum value for the number of unsigned bits.
	 * 
	 * @param i
	 *            The number of unsigned bits.
	 * @return The maximum value for the number of unsigned bits.
	 */
	public static int getMaxIntValueForNumBits(int i) {

		if (i >= 32)
			throw new RuntimeException("Number of bits exceeds Java int.");
		else
			return (int) maxValueCache[i];

	}

	/**
	 * Get the maximum value for the number of unsigned bits.
	 * 
	 * @param i
	 *            The number of unsigned bits.
	 * @return The maximum value for the number of unsigned bits.
	 */
	public static long getMaxLongValueForNumBits(int i) {

		if (i >= 64)
			throw new RuntimeException("Number of bits exceeds Java long.");
		else 
			return maxValueCache[i];

	}

}
