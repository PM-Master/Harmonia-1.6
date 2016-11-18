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
 * This class defines an enumeration of PM item types.  The following types are: <ul> <li><tt>BINARY</tt> A binary payload stream sent by either a client or a server. <li><tt>CMD_ARG</tt> A command argument sent by a client to a server. <li><tt>CMD_CODE</tt> A command sent by a client to a server. <li><tt>RESPONSE_ERROR</tt> A response sent by a server to a client  indicating that an error occurred on the server with respect to the client's previous request. <li><tt>RESPONSE_SUCCESS</tt> A response sent by a server to a client indicating that an operation was successful with respect to the client's previous request. <li><tt>RESPONSE_TEXT</tt> A response sent by a server to a client containing data relevant to the client's previous request. </ul>
 * @author  steveq@nist.gov
 * @version  $Revision: 1.1 $, $Date: 2008/07/16 17:02:57 $
 * @since  6.0
 */
public enum ItemType {

	
	/* If you add more items here, be sure to also add in getType() below! */
	
	/* Since commands and responses are basically the same now that we
	 * are allowing clients to send files to the server, we can simplify this
	 * by not distinguishing between 'CMD' and 'RESPONSE' and just have
	 * 'MESSAGE' or 'MSG'.  However, because Serban has already started
	 * integration with PM, I will hold off on making this change.
	 */
	/**
	 * @uml.property  name="bINARY"
	 * @uml.associationEnd  
	 */
	BINARY(0, "BINARY"),  	// For file or binary stream payloads
	/**
	 * @uml.property  name="cMD_ARG"
	 * @uml.associationEnd  
	 */
	CMD_ARG(1, "CMD_ARG"), 
	/**
	 * @uml.property  name="cMD_CODE"
	 * @uml.associationEnd  
	 */
	CMD_CODE(2, "Used to indicate a command."),
	/**
	 * @uml.property  name="rESPONSE_ERROR"
	 * @uml.associationEnd  
	 */
	RESPONSE_ERROR(3, "RESPONSE_ERROR"),
	/**
	 * @uml.property  name="rESPONSE_SUCCESS"
	 * @uml.associationEnd  
	 */
	RESPONSE_SUCCESS(4, "RESPONSE_SUCCESS"),
	/**
	 * @uml.property  name="rESPONSE_TEXT"
	 * @uml.associationEnd  
	 */
	RESPONSE_TEXT(5, "RESPONSE_TEXT"), 
	/**
	 * @uml.property  name="rESPONSE_DNR"
	 * @uml.associationEnd  
	 */
	RESPONSE_DNR(6, "RESPONSE_DNR"); // do not respond 

	/**
	 * The int value for this item type.
	 * @uml.property  name="type"
	 */
	private int type = -1;

	/**
	 * The description of this item type.
	 * @uml.property  name="description"
	 */
	private String description = "";

	/**
	 * Construct an item type.
	 * 
	 * @param type
	 *            The int value of this item type.
	 * @param description
	 *            The description of this item type.
	 */
	ItemType(int type, String description) {

		this.type = type;
		this.description = description;

	}

	/**
	 * Get the int value of this item type.
	 * 
	 * @return The int value of this item type.
	 */
	protected int intValue() {

		return type;

	}

	/**
	 * Get the description of this item type.
	 * 
	 * @return The description of this item type.
	 */
	public String toString() {

		return description;

	}

        public String toPrefix() {
          switch (type) {
            case 0:
                  return "BIN";
            case 1:
                  return "ARG";
            case 2:
                  return "CMD";
            case 3:
                  return "ERR";
            case 4:
                  return "SUC";
            case 5:
                  return "TXT";
            case 6:
                  return "DNR";
            default:
                  return "UNK";
          }
        }

        /**
	 * Get the item type given its byte representation.
	 * 
	 * @param b The byte representation of the item type.
	 * @return The item type for the byte representation.
	 */
	public static ItemType getType(byte b) {
		
		int i = b & 0xFF;
		
		switch (i) {
		
		case 0:
			return ItemType.BINARY;
		case 1:
			return ItemType.CMD_ARG;
		case 2:
			return ItemType.CMD_CODE;
		case 3:
			return ItemType.RESPONSE_ERROR;
		case 4:
			return ItemType.RESPONSE_SUCCESS;
		case 5:
			return ItemType.RESPONSE_TEXT;
		case 6:
			return ItemType.RESPONSE_DNR;
		default:
			return null;
		
		}
		
	}

}