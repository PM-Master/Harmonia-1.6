package gov.nist.csd.pm.application.openoffice;

import com.sun.star.io.BufferSizeExceededException;
import com.sun.star.io.NotConnectedException;
import com.sun.star.io.XOutputStream;

import java.io.OutputStream; 

/**
 * @author steveq@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:03:00 $
 * @since 1.5
 */
public class OOOutputStream implements XOutputStream { 

    /**
	 * @uml.property  name="outputStream"
	 */
    private OutputStream outputStream; 

    /** Creates a new instance of OOOutputStream 
     * @param outputStream*/ 
    public OOOutputStream(OutputStream outputStream) { 
        this.outputStream = outputStream; 
    } 

    public void writeBytes(byte[] values) 
        throws NotConnectedException, BufferSizeExceededException, com.sun.star.io.IOException { 
        try { 
            outputStream.write(values); 
        } catch ( java.io.IOException ioe ) { 
            throw(new com.sun.star.io.IOException(ioe.getMessage())); 
        } 
    } 

    public void closeOutput() 
        throws NotConnectedException, BufferSizeExceededException, com.sun.star.io.IOException { 
        try { 
            outputStream.flush(); 
            outputStream.close(); 
        } catch ( java.io.IOException ioe ) { 
            throw(new com.sun.star.io.IOException(ioe.getMessage())); 
        } 
    } 

    public void flush() { 
        try { 
            outputStream.flush(); 
        } catch ( java.io.IOException ignored ) {} 
    } 
} 
