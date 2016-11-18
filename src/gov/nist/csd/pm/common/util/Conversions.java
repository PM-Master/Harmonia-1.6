/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.csd.pm.common.util;

import java.util.Iterator;
import java.util.Set;

/**
 * @author Administrator
 */
public class Conversions {

    private static final int PM_MAX_BLOCK_SIZE = 1024;

    /**
     * @param set
     * @return
     * @deprecated - Use com.google.common.base.Joiner
     */
    public static String setToString(Set set) {
        if (set == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        Iterator iter = set.iterator();
        boolean firstTime = true;
        while (iter.hasNext()) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(",");
            }
            sb.append((String) iter.next());
        }
        return sb.toString();
    }

    public static byte[] hexString2ByteArray(String inp) {
        byte[] bytes = new byte[inp.length()];
        hexString2ByteArray(inp, 0, bytes, 0);
        return bytes;
    }

    // Convert the hex string starting at offset inpoff in inp to a byte aray
    // starting at offset off in out.
    public static boolean hexString2ByteArray(String inp, int inpoff,
                                              byte[] out, int off) {

        int len;
        int inpix, outix;

        if (inp == null || inp.length() <= inpoff
                || (inp.length() - inpoff) % 2 != 0) {
            return false;
        }

        for (inpix = inpoff, outix = off; inpix < inp.length(); ) {

            int msn = Character.digit(inp.charAt(inpix++), 16);
            int lsn = Character.digit(inp.charAt(inpix++), 16);

            out[outix++] = (byte) ((msn << 4) | lsn);
        }
        return true;
    }

    private static byte hexDigit2Byte(char c) {
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

    private static byte byte2HexDigit(byte n) {
        if (n < 10) {
            return (byte) ('0' + n);
        } else {
            return (byte) ('A' + n - 10);
        }
    }

    // We take advantage of the fact that the argument length <=
    // PM_MAX_BLOCK_SIZE.
    private static char[] workBuf = new char[2 * PM_MAX_BLOCK_SIZE];

    public static String byteArray2HexString(byte[] inp, int off, int length) {
        int inpix, outix;
        int n;
        Byte q, r;

        for (inpix = off, outix = 0; inpix - off < length; inpix++) {
            n = inp[inpix] & 0x000000FF;
            q = (byte) (n / 16);
            r = (byte) (n % 16);

            workBuf[outix++] = Character.forDigit(q, 16);
            workBuf[outix++] = Character.forDigit(r, 16);
        }
        return new String(workBuf, 0, length * 2);
    }
}
