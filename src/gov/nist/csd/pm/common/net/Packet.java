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

import gov.nist.csd.pm.common.util.Log;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class implements a PM packet. A PM packet is used to carry PM messages.
 * The byte array representation of a PM packet consists of a 4 byte length
 * field that defines the length of the payload following the length field. The
 * payload consists of n Item blocks.
 * 
 * <pre>
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                            Length                             |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                         Item blocks...                 
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
 * 
 * @author steveq@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:57 $
 * @since 6.0
 */
public class Packet {

	Log log = new Log(Log.Level.INFO, true);

	/***************************************************************************
	 * Constants
	 **************************************************************************/

	public static final int LENGTH_FIELD_SIZE = 4; // bytes

	/***************************************************************************
	 * Variables
	 **************************************************************************/

	/**
	 * The list of items associated with this PM packet.
	 * 
	 * @uml.property name="itemsArray"
	 * @uml.associationEnd multiplicity="(0 -1)"
	 *                     elementType="gov.nist.csd.pm.common.net.Item"
	 */
	private ArrayList<Item> itemsArray = null;

	/**
	 * The payload length of this packet (i.e., the length of all Item objects
	 * in this packet.
	 * 
	 * @uml.property name="payloadLength"
	 */
	private int payloadLength = 0;

	/**
	 * Indicates that a CMD_CODE item has already been added. Note that only one
	 * command code item per packet is permitted.
	 * 
	 * @uml.property name="commandSet"
	 */
	private boolean commandSet = false;

	/**
	 * Indicates that a RESPONSE_TEXT item has been added. Note that no error or
	 * file items are permitted with a text response.
	 * 
	 * @uml.property name="responseTextSet"
	 */
	private boolean responseTextSet = false;

	/**
	 * Indicates that a RESPONSE_ERROR item has already been added. Note that
	 * only one error message item per packet is permitted.
	 * 
	 * @uml.property name="responseErrorSet"
	 */
	private boolean responseErrorSet = false;

	/**
	 * Indicates that a BINARY item has already been added. Note that only one
	 * file item per packet is permitted.
	 * 
	 * @uml.property name="binarySet"
	 */
	private boolean binarySet = false;

	/**
	 * Indicates this packet contains a RESPONSE_ERROR item.
	 * 
	 * @uml.property name="hasError"
	 */
	private boolean hasError = false;

	/**
	 * "Do not respond" indicator.
	 * 
	 * @uml.property name="doNotRespond"
	 */
	private boolean doNotRespond = false;

	/**
	 * The error message associated with the RESPONSE_ERROR item.
	 * 
	 * @uml.property name="errorMessage"
	 */
	private String errorMessage = null;

	/**
	 * Indicates that the packet successfully received a binary stream and wrote
	 * the data to the given output stream.
	 * 
	 * @uml.property name="successfullyReceivedBinaryStream"
	 */
	protected boolean successfullyReceivedBinaryStream = false;

	/***************************************************************************
	 * Constructors
	 **************************************************************************/

	/**
	 * Construct a PM packet. This constructor is used for creating and then
	 * sending a new PM packet.
	 */
	public Packet() {

		this.itemsArray = new ArrayList<Item>();
	}

	/**
	 * Construct a PM packet from a byte array.
	 * 
	 * @param bytes
	 *            The byte array representation of this packet.
	 */
	public Packet(byte[] bytes) {

		this.itemsArray = new ArrayList<Item>();

		int offset = 0;

		// Get the payload length
		byte[] payloadLengthBytes = new byte[LENGTH_FIELD_SIZE];
		System.arraycopy(bytes, offset, payloadLengthBytes, 0,
				LENGTH_FIELD_SIZE);
		offset += LENGTH_FIELD_SIZE;

		int payloadLength = ByteUtil.bytesToInt(payloadLengthBytes);
		int packetLength = LENGTH_FIELD_SIZE + payloadLength;

		for (;;) {

			// Get Item Type
			byte[] itemTypeBytes = new byte[1];
			System.arraycopy(bytes, offset, itemTypeBytes, 0,
					itemTypeBytes.length);
			ItemType itemType = ItemType.getType(itemTypeBytes[0]);
			offset += itemTypeBytes.length;

			// Get Item Length
			byte[] itemLengthBytes = new byte[4];
			System.arraycopy(bytes, offset, itemLengthBytes, 0,
					itemLengthBytes.length);
			int itemLength = ByteUtil.bytesToInt(itemLengthBytes);
			offset += itemLengthBytes.length;

			// Get Payload
			byte[] itemPayloadBytes = new byte[itemLength];
			System.arraycopy(bytes, offset, itemPayloadBytes, 0, itemLength);
			offset += itemLength;

			try {

				addItem(itemType, itemPayloadBytes);

			} catch (PacketException pe) {

				pe.printStackTrace();

			}

			if (offset == packetLength)
				break;

		}
	}

	/***************************************************************************
	 * Methods
	 **************************************************************************/

	/**
	 * Add an item without a value. This method is convenient for some item
	 * types that do not have an associated value (e.g., RESPONSE_SUCCESS).
	 * However, caution should be used when using this method since most item
	 * types require an associated value (e.g., CMD_CODE). Using this method for
	 * item types that require an associated value will lead to unexpected
	 * results. This method automatically sets an empty string "" as the value.
	 * 
	 * @param itemType
	 *            The type of the item.
	 * @throws PacketException
	 *             If item type is not compatible with packet type.
	 */
	public void addItem(ItemType itemType) throws PacketException {

		testItemType(itemType);

		try {

			Item item = new Item(itemType, "");
			itemsArray.add(item);
			payloadLength += item.getLength();

		} catch (ItemException ie) {

			throw new PacketException(ie.getMessage());

		}

	}

	/**
	 * Add an item with a string value to this packet.
	 * 
	 * @param itemType
	 *            The type of the item.
	 * @param value
	 *            The value of this item.
	 * @throws PacketException
	 *             If item type is not compatible with packet type.
	 */
	public void addItem(ItemType itemType, String value) throws PacketException {

		if (itemType == ItemType.RESPONSE_DNR) {
			doNotRespond = true;
		} else if (itemType == ItemType.RESPONSE_ERROR) {
			hasError = true;
			//System.out.println("Value is " + value); // Added by Gopi
			if (value == null)
				value = ""; // Added by Gopi
			errorMessage = new String(value);
		}

		testItemType(itemType);

		try {
			Item item = new Item(itemType, value);
			itemsArray.add(item);
			payloadLength += item.getLength();

		} catch (ItemException ie) {
			System.out.println("EXCePTION AT PACKET.java line 237");
			throw new PacketException(ie.getMessage());

		}

	}

	/**
	 * Add an item with a byte array value to this packet.
	 * 
	 * @param itemType
	 *            The type of the item.
	 * @param value
	 *            The value of this item.
	 * @throws PacketException
	 *             If item type is not compatible with packet type.
	 */
	public void addItem(ItemType itemType, byte[] value) throws PacketException {

		if (itemType == ItemType.RESPONSE_ERROR) {
			hasError = true;
			errorMessage = new String(value);
		}

		testItemType(itemType);

		try {

			Item item = new Item(itemType, value);
			itemsArray.add(item);
			payloadLength += item.getLength();

		} catch (ItemException ie) {

			throw new PacketException(ie.getMessage());

		}

	}

	/**
	 * Add a binary stream of size 'payloadLength'. This method is intended for
	 * streams that contain a file or other binary payload. This method should
	 * only be called by the PacketManager.
	 * 
	 * @param payloadLength
	 *            The length for this binary payload stream.
	 */
	protected void addBinaryItem(int payloadLength) throws PacketException {

		testItemType(ItemType.BINARY);

		try {
			// System.out.println("Adding item of length: " + payloadLength);
			Item item = new Item(payloadLength);
			itemsArray.add(item);
			this.payloadLength += item.getLength();

		} catch (ItemException ie) {

			throw new PacketException(ie.getMessage());

		}
	}

	/**
	 * Tests various conditions for correct item type usage.
	 * 
	 * @param itemType
	 *            The item type to be checked.
	 * @throws PacketException
	 */
	private void testItemType(ItemType itemType) throws PacketException {

		if (itemType == ItemType.CMD_CODE) {

			if (commandSet == false && responseTextSet == false
					&& responseErrorSet == false && binarySet == false) {

				commandSet = true;

			} else if (responseTextSet == true || responseErrorSet == true
					|| binarySet == true) {
				throw new PacketException(
						"Cannot add item of type "
								+ "ItemType.CMD_CODE if a response item has already been addeed.");

			} else if (commandSet == true) {

				throw new PacketException("Cannot add item of type "
						+ "ItemType.CMD_CODE more than once to a single packet");
			}

		} else if (itemType == ItemType.CMD_ARG) {

			if (commandSet == false) {

				throw new PacketException("Cannot add item of type "
						+ "ItemType.CMD_ARG before setting ItemType.CMD_CODE");
			}

		} else if (itemType == ItemType.RESPONSE_TEXT) {

			if (responseErrorSet == false && binarySet == false
					&& commandSet == false) {

				responseTextSet = true;

			} else {

				throw new PacketException(
						"Cannot add item of type "
								+ "ItemType.RESPONSE_TEXT if error or file item has already been addeed.");

			}

		} else if (itemType == ItemType.RESPONSE_ERROR) {

			if (responseErrorSet == false && responseTextSet == false
					&& binarySet == false && commandSet == false) {

				responseErrorSet = true;

			} else if (responseTextSet == true || binarySet == true
					|| commandSet == true) {

				throw new PacketException(
						"Cannot add item of type "
								+ "ItemType.RESPONSE_ERROR if another item has already been addeed.");

			} else if (responseErrorSet == true) {

				throw new PacketException(
						"Cannot add item of type "
								+ "ItemType.RESPONSE_ERROR more than once to a single packet");

			}

		} else if (itemType == ItemType.BINARY) {

			if (binarySet == false && responseTextSet == false
					&& responseErrorSet == false && commandSet == false) {
				binarySet = true;

			} else if (responseTextSet == true || responseErrorSet == true
					|| commandSet == true) {
				throw new PacketException(
						"Cannot add item of type "
								+ "ItemType.RESPONSE_FILE if text or error item has already been addeed.");

			} else if (binarySet == true) {
				throw new PacketException(
						"Cannot add item of type "
								+ "ItemType.RESPONSE_BINARY more than once to a single packet");

			}

		}

	}

	/**
	 * Get the items of this packet.
	 * 
	 * @return The items of this packet.
	 */
	public ArrayList<Item> getItems() {
		return itemsArray;
	}

	public Iterable<String> toStringIterable() {
		return new Iterable<String>() {

			@Override
			public Iterator<String> iterator() {
				return toStringIterator();
			}
		};
	}

	public Iterator<String> toStringIterator() {
		return new Iterator<String>() {
			int size = Packet.this.size();
			int current = 0;

			@Override
			public boolean hasNext() {
				return current < size;
			}

			@Override
			public String next() {
				return getStringValue(current++);
			}

			@Override
			public void remove() {
				throw new RuntimeException(
						"remove not implemented in Packet.getStringIterable");
			}
		};
	}

	public int size() {
		return itemsArray.size();
	}

	public String getStringValue(int i) {
		if (i < 0 || i >= itemsArray.size())
			return null;
		String s = ((Item) itemsArray.get(i)).getValueString();
		if (s.length() <= 0)
			return null;
		return s;
	}

	/**
	 * Get the byte array values of all items in this packet.
	 * 
	 * @return The byte array values of all items in this packet.
	 */
	public ArrayList<byte[]> getByteArrayValues() {

		ArrayList<byte[]> values = new ArrayList<byte[]>();
		for (int i = 0; i < itemsArray.size(); i++) {

			Item item = itemsArray.get(i);

			values.add(item.getValueBytes());

		}

		return values;

	}

	/**
	 * Get the indexed Item block contained in this packet.
	 * 
	 * @param index
	 *            The index of the Item block to retrieve.
	 * @return The indexed Item block from this packet.
	 * @throws PacketException
	 *             if index is out of bounds.
	 */
	public Item getItem(int index) throws PacketException {

		if (index < 0 || index > itemsArray.size()) {
			throw new PacketException("OutOfBoundsIndex into ItemsArray");
		}

		return itemsArray.get(index);
	}

	/**
	 * Get the type of the indexed Item block contained in this packet.
	 * 
	 * @param index
	 *            The index of the Item block.
	 * @return The type of the indexed Item block contained in this packet.
	 * @throws PacketException
	 *             index is out of bounds.
	 */
	public ItemType getItemType(int index) throws PacketException {

		if (index < 0 || index > itemsArray.size()) {
			throw new PacketException("OutOfBoundsIndex into ItemsArray");
		}

		Item item = itemsArray.get(index);
		return item.getType();

	}

	/**
	 * Get the string value of the indexed Item block contained in this packet.
	 * 
	 * @param index
	 *            The index of the Item block.
	 * @return The string value of the indexed Item block contained in this
	 *         packet.
	 * @throws PacketException
	 *             if index is out of bounds.
	 */
	public String getItemStringValue(int index) throws PacketException {

		if (index < 0 || index > itemsArray.size()) {
			throw new PacketException("OutOfBoundsIndex into ItemsArray");
		}

		Item item = itemsArray.get(index);
		return item.getValueString();

	}

	/**
	 * Get the byte array value of the indexed Item block contained in this
	 * packet.
	 * 
	 * @param index
	 *            The index of the Item block.
	 * @return The byte array value of the indexed Item block contained in this
	 *         packet.
	 * @throws PacketException
	 *             if index is out of bounds.
	 */
	public byte[] getItemBytesValue(int index) throws PacketException {

		if (index < 0 || index > itemsArray.size()) {
			throw new PacketException("OutOfBoundsIndex into ItemsArray");
		}

		Item item = itemsArray.get(index);
		return item.getValueBytes();

	}

	/**
	 * Get the byte array representation of this packet.
	 * 
	 * @return The byte array representation of this packet.
	 */
	public byte[] getBytes() {

		int offset = 0;
		byte[] bytes = new byte[LENGTH_FIELD_SIZE + payloadLength];
		byte[] payloadLengthBytes = ByteUtil.intToBytes(payloadLength);
		System.arraycopy(payloadLengthBytes, 0, bytes, offset,
				LENGTH_FIELD_SIZE);
		offset += LENGTH_FIELD_SIZE;

		for (int i = 0; i < itemsArray.size(); i++) {

			Item item = itemsArray.get(i);

			byte[] itemBytes = item.getBytes();
			System.arraycopy(itemBytes, 0, bytes, offset, itemBytes.length);
			offset += itemBytes.length;

		}

		return bytes;
	}

	/**
	 * Get the payload length of this packet.
	 * 
	 * @return The payload length of this packet.
	 * @uml.property name="payloadLength"
	 */
	public int getPayloadLength() {

		return this.payloadLength;

	}

	/**
	 * Indicates whether this packet contains a RESPONSE_ERROR message.
	 * 
	 * @return True if this packet contains an error message, false otherwise.
	 */
	public boolean hasError() {
		return hasError;
	}

	public boolean doNotRespond() {
		return doNotRespond;
	}

	/**
	 * Get an error message associated with this packet.
	 * 
	 * @return The error message if it exists, otherwise return null.
	 * @uml.property name="errorMessage"
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Indicates if the packet contains a BINARY stream that was successfully
	 * received and written to the given output stream.
	 * 
	 * @return True if the binary stream was successfully received, false
	 *         otherwise.
	 * 
	 */
	public boolean successfullyReceivedBinaryStream() {

		return successfullyReceivedBinaryStream;

	}

	/**
	 * Indicates if packet has an empty payload.
	 * 
	 * @return True if payload length is 0, false otherwise.
	 */
	public boolean isEmpty() {

		if (payloadLength == 0)
			return true;
		else
			return false;
	}

	/**
	 * Indicates if the packet contains a RESPONSE_TEXT message.
	 */
	public boolean hasText() {

		return responseTextSet;

	}

	public void print(boolean bDebug, String sDirection) {
		if (!bDebug)
			return;

		System.out.println(sDirection + "-----Packet start-----");
		for (int i = 0; i < itemsArray.size(); i++) {
			Item item = itemsArray.get(i);
			ItemType t = item.getType();
			String v = item.getValueString();
			if (v == null)
				System.out.println(sDirection + t.toPrefix());
			else
				System.out.println(sDirection + t.toPrefix() + " " + v);
		}
		System.out.println(sDirection + "-----Packet end-------");
	}

	public static Packet nullPacket() {
		return new Packet();
	}

	public static Packet getSuccessPacket() {
		return getSuccessPacket("");
	}

	public static Packet getSuccessPacket(String s) {
		Packet p = new Packet();
		try {
			p.addItem(ItemType.RESPONSE_SUCCESS, s);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return p;
	}

	public static Packet dnrPacket() {
		Packet p = new Packet();
		try {
			p.addItem(ItemType.RESPONSE_DNR, "");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return p;
	}

	public static Packet failurePacket() {
		return failurePacket("");
	}

	public static Packet failurePacket(String message) {
		Packet p = new Packet();
		try {
			p.addItem(ItemType.RESPONSE_ERROR, message);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return p;
	}
}
