package gov.nist.csd.pm.common.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 9/2/11
 * Time: 3:45 PM
 * To change this template use File | Settings | File Templates.
 */
public final class InputStreams {
    private InputStreams(){

    }

    private static final int BUFFER_SIZE = 4096;

    public static byte[] getAllFromInputStream(InputStream stream){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int read = 0;
        try {
            while((read = stream.read(buffer)) > 0){
                baos.write(buffer, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return baos.toByteArray();
    }
}
