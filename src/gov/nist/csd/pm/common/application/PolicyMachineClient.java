package gov.nist.csd.pm.common.application;

import gov.nist.csd.pm.common.net.Packet;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 9/20/11
 * Time: 1:19 PM
 * Extracted interface for SSLSocketClient
 */
public interface PolicyMachineClient {
    Packet sendReceive(Packet cmd) throws Exception;

    Packet sendReceive(Packet cmd, OutputStream out) throws Exception;

    void download(Packet cmd, OutputStream out) throws Exception;

    void upload(Packet cmd, InputStream in) throws Exception;

    Packet warnAndSend(Packet warningCmd, byte[] bytes) throws Exception;

    Packet warnAndSend(Packet warningCmd, InputStream is, int length) throws Exception;

    void close() throws Exception;
}
