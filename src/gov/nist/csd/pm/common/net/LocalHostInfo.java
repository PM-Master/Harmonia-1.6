package gov.nist.csd.pm.common.net;

import java.net.InetAddress;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/28/11
 * Time: 12:10 PM
 * To change this template use File | Settings | File Templates.
 */
public final class LocalHostInfo {
    private LocalHostInfo(){}
    public static String getLocalHost() {
        String sHostName;
        try {
            InetAddress addr = InetAddress.getLocalHost();
            sHostName = addr.getHostName();
            int end = sHostName.indexOf('.');
            if (end >= 0) {
                sHostName = sHostName.substring(0, end);
            }
            return sHostName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
     public static boolean isLocalHost(String sHost) {
        String sLocalHost = getLocalHost();

        return sLocalHost.equalsIgnoreCase(sHost);
    }
}
