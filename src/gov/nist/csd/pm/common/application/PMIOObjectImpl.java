package gov.nist.csd.pm.common.application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/1/11
 * Time: 11:25 AM
 * To change this template use File | Settings | File Templates.
 */
public class PMIOObjectImpl implements PMIOObject {

    /**
	 * @uml.property  name="_handle"
	 */
    private String _handle;
    /**
	 * @uml.property  name="_name"
	 */
    private final String _name;
    /**
	 * @uml.property  name="_perms"
	 */
    private final String _perms;
    /**
	 * @uml.property  name="_sysCaller"
	 * @uml.associationEnd  
	 */
    private final SysCaller _sysCaller;
    /**
	 * @uml.property  name="_isOpen"
	 */
    private boolean _isOpen = false;

    public PMIOObjectImpl(SysCaller sysCaller, String name, String perms) {
        _sysCaller = sysCaller;
        _name = name;
        _perms = perms;
        _handle = open();


    }

    private String open(){
        String handle = _sysCaller.openObject3(_name, _perms);
        if(handle != null){
            _isOpen = true;
        }
        return handle;
    }

    @Override
    public String getHandle() {
        return _handle;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public String getPermissions() {
        return _perms;
    }
    @Override
    public void reset(){
        close();
        open();
    }

    @Override
    public void close() {
        try {

            if (_pmInputStream != null) {
                _pmInputStream.close();
                _pmInputStream = null;
            }
            if (_pmOutputStream != null) {
                _pmOutputStream.close();
                _pmOutputStream = null;
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        _sysCaller.closeObject(getHandle());
        _isOpen = false;
    }

    @Override
    public boolean isOpen() {
        return _isOpen;
    }

    private class PMObjectInputStream extends InputStream {


        @Override
        public int read() throws IOException {
            byte[] buf = new byte[1];
            int response = read(buf);
            return response < 0 ? response : buf[0];
        }

        @Override
        public int read(byte[] b) throws IOException {
            if (b == null) {
                return 0;
            }
            return read(b, 0, b.length);
        }


        @Override
        public int read(byte[] b, int offset, int length) throws IOException {
            if (!isOpen()) {
                throw new IOException(String.format("PM Object is not open! (%s)", getHandle()));
            }
            int totalCopied = complete() ? -1 : 0;

            while (length > 0 && !complete()) {
                int copied = copyAvailableBytes(b, offset, length);
                if (copied > 0) {
                    length -= copied;
                    offset += copied;
                    totalCopied += copied;
                } else {
                    //If no bytes were copied, assume the end of file has been reached.
                    System.out.println("Total copied was " + totalCopied);
                    totalCopied = totalCopied > 0 ? totalCopied : -1;
                    break;
                }
            }
            return totalCopied;
        }

        private int backbufferAvailable() {
            if (!complete()) {
                reloadBackbufferIfDepleted();
            }
            return backBuffer == null ? 0 : backBuffer.length - bboffset;
        }

        @Override
        public int available() throws IOException {

            return backbufferAvailable();
        }

        private byte[] backBuffer;
        private int bboffset = 0;
        private int accumulated = 0;
        private boolean complete = false;

        private boolean backbufferDepleted() {
            return backBuffer == null || bboffset == backBuffer.length;
        }

        private void reloadBackbufferIfDepleted() {
            if (backbufferDepleted() && !complete) {
                backBuffer = _sysCaller.readObject3(getHandle());
                bboffset = 0;
                if (backBuffer == null || backBuffer.length == 0) {
                    System.out.println("Null or empty payload returned");
                    complete = true;
                } else {
                    accumulated += backBuffer.length;
                }
            }

        }

        private boolean complete() {
            return complete;
        }

        protected int copyAvailableBytes(byte[] targetArray, int offset, int length) throws IOException {
            int lengthAvailableInTarget = Math.min(targetArray.length - offset, length);
            if (lengthAvailableInTarget < 0) {
                throw new IOException("No available space in target array");
            }
            reloadBackbufferIfDepleted();
            if (complete()) {
                return -1;
            } else {
                System.out.printf("Got data buffer of %d bytes with accumulated %d bytes\n", backBuffer.length, accumulated);
                int transferAmount = Math.min(backbufferAvailable(), lengthAvailableInTarget);
                System.arraycopy(backBuffer, bboffset, targetArray, offset, transferAmount);
                bboffset += transferAmount;
                return transferAmount;
            }
        }

    }


    private static final int DEFAULT_BUFFER_SIZE = 1024;

    private class PMObjectOutputStream extends OutputStream {


        @Override
        public void write(int i) throws IOException {
            write(new byte[]{(byte) i});
        }

        @Override
        public void write(byte[] b) throws IOException {
            if (b != null) {
                write(b, 0, b.length);
            }
        }

        @Override
        public void write(byte[] b, int offset, int length) throws IOException {
            if (!isOpen()) {
                throw new IOException(String.format("PM Object is not open! (%s)", getHandle()));
            }
            if (length < b.length) {
                byte[] window = new byte[length];
                System.arraycopy(b, offset, window, 0, length);
                b = window;
            }
            _sysCaller.writeObject3(getHandle(), b);
        }


        @Override
        public void close() throws IOException {
            super.close();
            _sysCaller.closeObject(getHandle());

        }


    }

    /**
	 * @uml.property  name="_pmInputStream"
	 */
    private InputStream _pmInputStream;

    @Override
    public InputStream getInputStream() {
        if (_pmInputStream == null) {
            _pmInputStream = new PMObjectInputStream();
        }
        return _pmInputStream;
    }

    /**
	 * @uml.property  name="_pmOutputStream"
	 */
    private OutputStream _pmOutputStream;

    @Override
    public OutputStream getOutputStream() {
        if (_pmOutputStream == null) {
            _pmOutputStream = new PMObjectOutputStream();
        }
        return _pmOutputStream;
    }
}
