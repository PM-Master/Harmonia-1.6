package gov.nist.csd.pm.common.pdf.support;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;

import static gov.nist.csd.pm.common.util.Files.isPDF;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 2/3/11
 * Time: 5:34 PM
 * To change this template use File | Settings | File Templates.
 *
 * Utility class for working with PDDocument's
 *
 */
public abstract class PDDocuments {
    private PDDocuments(){

    }

    public static boolean isValidDocument(String documentPath){
        return isValidDocument(new File(documentPath));
    }

    public static boolean isValidDocument(File documentFile){
        boolean result = false;
        if(isPDF(documentFile)){

        }
        return result;
    }


    /**
     * Checks whether or not the document passed is an empty one.
     * @param doc - the document that is presumably empty.
     * @return - the boolean result indicating emptiness.  true is empty in this case.
     */
    public static boolean emptyDocument(PDDocument doc){
        return doc.getDocumentCatalog().getAllPages().isEmpty();
    }
}
