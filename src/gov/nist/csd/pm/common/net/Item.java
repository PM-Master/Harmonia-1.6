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

/**
 * This class implements a PM item block. The byte representation of a PM item
 * block consists of a 1 byte item type field, a 4 byte payload length field,
 * and a variable byte payload field. Note that the length of an Item is
 * restricted to a Java int (2,147,483,647 bytes).
 * 
 * <pre>
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |   ItemType    |                    Length                                              
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *                 |                   Payload...                        
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
 * 
 * @author steveq@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:57 $
 * @since 6.0
 */
public class Item {

	/***************************************************************************
	 * Constants
	 **************************************************************************/

	/** The fixed length of the item type field (in bytes). */
	public static final int ITEM_TYPE_FIELD_SIZE = 1;

	/** The fixed length of the length field (in bytes). */
	public static final int LENGTH_FIELD_SIZE = 4;

	/***************************************************************************
	 * Variables
	 **************************************************************************/

	/**
	 * The type of this item block.
	 * @uml.property  name="itemType"
	 * @uml.associationEnd  
	 */
	private ItemType itemType = null;

	/**
	 * The payload length of this object.
	 * @uml.property  name="payloadLength"
	 */
	private int payloadLength = 0;

	/**
	 * The payload of this item block. This variable is intended for only items that are not of type ItemType.BINARY.
	 * @uml.property  name="value" multiplicity="(0 -1)" dimension="1"
	 */
	private byte[] value = null;

	/***************************************************************************
	 * Constructor
	 **************************************************************************/

	/**
	 * Construct an item with a text payload. Note that an empty string ""
	 * should be used in lieu of null for <tt>value</tt>.
	 * 
	 * @param itemType
	 *            The type of this item block.
	 * @param value
	 *            The String payload of this item block.
	 * @exception ItemException
	 *                if <tt>itemType</tt> or <tt>value</tt> is null or if
	 *                <tt>itemType</tt> is of type
	 *                <tt>ItemType.RESPONSE_FILE</tt>.
	 */
	protected Item(ItemType itemType, String value) throws ItemException {

		if (itemType == null || value == null)
			throw new ItemException("The constructor Item(ItemType itemType, "
					+ "String value) requires"
					+ " a non-null itemType and value.");

		this.itemType = itemType;
		this.value = value.getBytes();
		this.payloadLength = this.value.length;

	}

	/**
	 * Construct an item with a byte array payload. Note that <tt>value</tt>
	 * should never be null.
	 * 
	 * @param itemType
	 *            The type of this item block.
	 * @param value
	 *            The binary payload of this item block.
	 * 
	 * @exception ItemException
	 *                if <tt>itemType</tt> or <tt>value</tt> is null.
	 */
	protected Item(ItemType itemType, byte[] value) throws ItemException {

		if (itemType == null || value == null)
			throw new ItemException(
					"The constructor Item(ItemType itemType, byte[] value) requires"
							+ " a non-null itemType and value.");

		this.itemType = itemType;
		this.value = value;
		this.payloadLength = value.length;

	}

	/**
	 * Construct an item with binary payload stream of length 'payloadLength'.
	 * This method automatically sets the item type to ItemType.BINARY.
	 * 
	 * @param payloadLength
	 *            The length of the binary payload stream.
	 * @exception ItemException
	 *                If file is null.
	 */
	protected Item(int payloadLength) throws ItemException {

		this.itemType = ItemType.BINARY;
		this.payloadLength = payloadLength;

	}

	/***************************************************************************
	 * Methods
	 **************************************************************************/

	/**
	 * Get the type of this item block.
	 * 
	 * @return The type of this item block.
	 */
	public ItemType getType() {

		return itemType;

	}

	/**
	 * Get the value (payload) of this item as a String. This method will return
	 * null if item is of type <tt>ItemType.BINARY</tt>.
	 * 
	 * @return The value of this item as a String or null if item is of type
	 *         <tt>ItemType.BINARY</tt>.
	 */
	public String getValueString() {

		if (itemType != ItemType.BINARY)
			return new String(value);
		else
			return null;

	}

	/**
	 * Get the value (payload) of this item as a byte array. This method will
	 * return null if item is of type <tt>ItemType.BINARY</tt> or
	 * <tt>ItemType.NULL</tt>.
	 * 
	 * @return The value of this item as a byte array or null if this item is of
	 *         type <tt>ItemType.BINARY</tt>.
	 */
	public byte[] getValueBytes() {

		if (itemType != ItemType.BINARY)
			return value;
		else
			return null;

	}

	/**
	 * Get the byte array representation of this item including the 1-byte item
	 * type field, the 4-byte length field, and payload. This method is intended
	 * for items that are not of type ItemType.BINARY. Calling this method on an
	 * item of type ItemType.BINARY will return null.
	 * 
	 * @return The byte array representation of this item block.
	 */
	protected byte[] getBytes() {

		byte[] result = null;

		if (value != null) {

			try {
				
				// Set byte array to exact size needed
				byte[] itemBytes = new byte[Item.ITEM_TYPE_FIELD_SIZE
						+ Item.LENGTH_FIELD_SIZE + this.payloadLength];

				// Get the header for this item
				byte[] itemHeaderBytes = getItemHeader();
				
				System.arraycopy(itemHeaderBytes, 0, itemBytes,
						0, itemHeaderBytes.length);

				// // Set item type field
				// int offset = 0;
				// byte itemTypeByte = 0;
				// itemTypeByte |= itemType.intValue();
				// itemBytes[offset] = itemTypeByte;
				// offset += Item.ITEM_TYPE_FIELD_SIZE;
				//
				// // Set length field
				// byte[] payloadLengthBytes =
				// ByteUtil.intToBytes(this.payloadLength);
				// System.arraycopy(payloadLengthBytes, 0, itemBytes, offset,
				// Item.LENGTH_FIELD_SIZE);
				// offset += Item.LENGTH_FIELD_SIZE;

				// Set content field
				System.arraycopy(value, 0, itemBytes, itemHeaderBytes.length,
						this.payloadLength);

				result = itemBytes;
			} catch (ItemException ie) {
				ie.printStackTrace();
			}

		}

		return result;

	}

	/**
	 * Get the length of this Item including the 1-byte ItemType field, the
	 * 4-byte Item payload length field, and the length of the Item payload.
	 * Note that file lengths are cast to int (so file lengths must be within
	 * the length of 2,147,483,647 bytes (i.e., a Java int).
	 * 
	 * @return The length of this Item.
	 */
	public int getLength() {

		return ITEM_TYPE_FIELD_SIZE + LENGTH_FIELD_SIZE + this.payloadLength;
	}

	/**
	 * Get the 1-byte ItemType and 4-byte length fields of an item. Note that
	 * this method currently supports a maximum payload length of 2,147,483,647
	 * bytes (i.e., a Java int).
	 * 
	 * @return The byte array representation of the ItemType and length fields.
	 * @throws ItemException
	 *             If ItemType is not BINARY.
	 */
	protected byte[] getItemHeader() throws ItemException {

		byte[] bytes = new byte[ITEM_TYPE_FIELD_SIZE + LENGTH_FIELD_SIZE];

		byte itemTypeByte = 0;
		itemTypeByte |= itemType.intValue();

		bytes[0] = itemTypeByte;

		byte[] lengthField = new byte[LENGTH_FIELD_SIZE];
		lengthField = ByteUtil.intToBytes(this.payloadLength);
		System.arraycopy(lengthField, 0, bytes, ITEM_TYPE_FIELD_SIZE,
				LENGTH_FIELD_SIZE);

		return bytes;

	}

}
