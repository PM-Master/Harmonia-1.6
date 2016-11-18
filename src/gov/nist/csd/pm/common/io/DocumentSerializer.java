package gov.nist.csd.pm.common.io;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by IntelliJ IDEA. User: R McHugh Date: 7/27/11 Time: 4:44 PM To change this template use File | Settings | File Templates.
 */
public interface DocumentSerializer {
    /**
	 * @uml.property  name="documentType"
	 */
    public String getDocumentType();
    /**
	 * @uml.property  name="documentName"
	 */
    public String getDocumentName();
    /**
	 * @param  name
	 * @uml.property  name="documentName"
	 */
    public void setDocumentName(String name);
    public void loadDocument(InputStream is, String name);
    public void saveDocument(OutputStream os);
}
