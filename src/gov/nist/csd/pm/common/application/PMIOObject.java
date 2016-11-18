package gov.nist.csd.pm.common.application;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: Rob Date: 1/19/11 Time: 11:01 AM To change this template use File | Settings | File Templates.
 */
public interface PMIOObject {
    /**
	 * Gets a handle that represents the PM IO Object
	 * @return
	 * @uml.property  name="handle"
	 */
    public String getHandle();

    /**
	 * Returns the name of the PM IO Object
	 * @return
	 * @uml.property  name="name"
	 */
    public String getName();

    /**
	 * Returns the permissions available to the PM IO Object
	 * @return
	 * @uml.property  name="permissions"
	 */
    public String getPermissions();

    /**
	 * Returns an input stream suitable for reading the object data
	 * @return
	 * @uml.property  name="inputStream"
	 */
    public InputStream getInputStream();

    /**
	 * Returns an output stream suitable for writing the object data
	 * @return
	 * @uml.property  name="outputStream"
	 */
    public OutputStream getOutputStream();

    /**
     * Closes the PM IO Object
     */
    public void close();

    /**
     * Returns true if the object is open, false otherwise
     * @return
     */
    public boolean isOpen();

    /**
     * resets the PM IO object to its newly opened state.
     */
    public void reset();
    /**
	 * @uml.property  name="eMPTY_OBJECT"
	 * @uml.associationEnd  
	 */
    public static PMIOObject EMPTY_OBJECT = new PMIOObject(){

        private List<Closeable> closeables = new ArrayList<Closeable>();
        private boolean _isOpen = true;
        private <T extends Closeable> T register(T closeable){
            closeables.add(closeable);
            return closeable;
        }

        @Override
        public String getHandle() {
            return "";
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public String getPermissions() {
            return "";
        }

        @Override
        public InputStream getInputStream() {
            return register(new ByteArrayInputStream(new byte[0]));
        }

        @Override
        public OutputStream getOutputStream() {
            return register(new ByteArrayOutputStream());
        }

        @Override
        public void close() {
            for(Closeable c : closeables){
                try {
                    c.close();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            closeables.clear();
            _isOpen= false;
        }

        @Override
        public boolean isOpen() {
            return _isOpen;
        }

        @Override
        public void reset() {
            close();
            _isOpen = true;
        }
    };
}
