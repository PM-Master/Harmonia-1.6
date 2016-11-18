package gov.nist.csd.pm.common.io;

import com.google.common.base.Throwables;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 8/17/11
 * Time: 7:50 AM
 * To change this template use File | Settings | File Templates.
 */
public final class DocumentSerializers {
    private DocumentSerializers(){

    }
    public static DocumentSerializer serializerFor(final byte [] bytes, final String docname, final String type){
        return new DocumentSerializer(){
            private String name = docname;
            @Override
            public String getDocumentType() {
                return type;
            }

            @Override
            public String getDocumentName() {
                return name;
            }

            @Override
            public void setDocumentName(String name) {
                this.name = name;
            }

            @Override
            public void loadDocument(InputStream is, String name) {
                try {
                    is.read(bytes);
                    if(is.available() > 0){
                        throw new RuntimeException("Overflow in DocumentSerializer for byte array");
                    }
                } catch (IOException e) {
                    Throwables.propagate(e);
                }
            }

            @Override
            public void saveDocument(OutputStream os) {
                try {
                    os.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        };
    }
}
