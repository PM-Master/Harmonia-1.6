package gov.nist.csd.pm.common.util;

import static gov.nist.csd.pm.common.net.Packet.failurePacket;
import gov.nist.csd.pm.common.config.ServerConfig;
import gov.nist.csd.pm.common.net.ItemType;
import gov.nist.csd.pm.common.net.Packet;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class UtilMethods {

	/**
	 * @uml.property  name="dfUpdate"
	 */
	public static DateFormat dfUpdate;
	
	// Timestamp of last update of data and relations.
	/**
	 * @uml.property  name="sLastUpdateTimestamp"
	 */
	public static String sLastUpdateTimestamp;
	
	// Argument n is a byte with a small positive or zero value - actually
	// between 0 and 15 - representing the decimal value of a hex digit.
	// The method returns a byte that corresponds to the character representing
	// the hex digit. For example, if n = 7, the method returns 0x37, i.e. '7'.
	// If n = 12, the method returns 'C'.
	public static byte byte2HexDigit(byte n) {
		if (n < 10) {
			return (byte) ('0' + n);
		} else {
			return (byte) ('A' + n - 10);
		}
	}

	public static String byteArray2HexString(byte[] inp, int length) {
		byte[] buf = new byte[2 * length];
		int inpix, outix;
		int n;
		byte q, r;

		for (inpix = outix = 0; inpix < length; inpix++) {
			n = inp[inpix] & 0x000000FF;
			q = (byte) (n / 16);
			r = (byte) (n % 16);
			buf[outix++] = byte2HexDigit(q);
			buf[outix++] = byte2HexDigit(r);
		}
		return new String(buf);
	}

	public static String byteArray2HexString(byte[] inp) {
		byte[] buf = new byte[2 * inp.length];
		int inpix, outix;
		int n;
		byte q, r;

		for (inpix = outix = 0; inpix < inp.length; inpix++) {
			n = inp[inpix] & 0x000000FF;
			q = (byte) (n / 16);
			r = (byte) (n % 16);
			buf[outix++] = byte2HexDigit(q);
			buf[outix++] = byte2HexDigit(r);
		}
		return new String(buf);
	}

	public static byte hexDigit2Byte(char c) {
		if (c >= '0' && c <= '9') {
			return (byte) (c - '0');
		} else if (c >= 'a' && c <= 'f') {
			return (byte) (c - 'a' + 10);
		} else if (c >= 'A' && c <= 'F') {
			return (byte) (c - 'A' + 10);
		} else {
			return (byte) 0;
		}
	}

	public static byte[] hexString2ByteArray(String inp) {
		//int len;
		int inpix, outix;

		if (inp == null || inp.length() == 0 || inp.length() % 2 != 0) {
			return null;
		}
		byte[] out = new byte[inp.length() / 2];

		for (inpix = 0, outix = 0; inpix < inp.length(); ) {
			int msn = hexDigit2Byte(inp.charAt(inpix++));
			int lsn = hexDigit2Byte(inp.charAt(inpix++));

			out[outix++] = (byte) ((msn << 4) | lsn);
		}
		return out;
	}
	
	public static boolean hostNameIsValid(String sName) {
		if (sName == null || sName.length() == 0) {
			return false;
		}
		String s = sName.trim();
		if (s.length() == 0) {
			return false;
		}
		char[] buf = s.toCharArray();
		for (char aBuf : buf) {
			if (Character.isLetterOrDigit(aBuf)) {
				continue;
			}
			if (aBuf == '_' || aBuf == '.') {
				continue;
			}
			if (aBuf == '-') {
				continue;
			}
			return false;
		}
		return true;
	}
	
	/**
	 * @uml.property  name="myRandom"
	 */
	public static Random myRandom = new Random();

	public static String generateRandomName() {
		byte[] bytes = new byte[4];
		myRandom.nextBytes(bytes);
		return byteArray2HexString(bytes);
	}
	


	public static Packet isTimeToRefresh(String sClientId, String sSessId,
			String sClientTimestamp) {
		Date dateEngine, dateClient;
		Packet result = new Packet();

		try {
			dateEngine = dfUpdate.parse(sLastUpdateTimestamp);
			dateClient = dfUpdate.parse(sClientTimestamp);
			if (dateEngine.compareTo(dateClient) <= 0) {
				result.addItem(ItemType.RESPONSE_TEXT, "no");
			} else {
				result.addItem(ItemType.RESPONSE_TEXT, "yes");
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception in engine's isTimeToRefresh: "
					+ e.getMessage());
		}
		return result;
	}

	


	public static String setToString(Set<String> set) {
		if (set == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		Iterator<String> iter = set.iterator();
		boolean firstTime = true;
		while (iter.hasNext()) {
			if (firstTime) {
				firstTime = false;
			} else {
				sb.append(",");
			}
			sb.append(iter.next());
		}
		return sb.toString();
	}

	public static HashSet<String> stringToSet(String sArg) {
		HashSet<String> set = new HashSet<String>();
		if (sArg != null) {
			String[] pieces = sArg.split(",");
			for (int i = 0; i < pieces.length; i++) {
				String t = pieces[i].trim();
				if (t.length() > 0) {
					set.add(t);
				}
			}
		}
		return set;
	}




}
