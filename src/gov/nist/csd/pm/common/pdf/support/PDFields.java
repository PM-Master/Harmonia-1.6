package gov.nist.csd.pm.common.pdf.support;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/26/11
 * Time: 10:29 AM
 * To change this template use File | Settings | File Templates.
 */
public final class PDFields {
    private PDFields() {
    }

    public static PDRectangle getPDRectangleForField(PDField field) {
        COSBase rectArray = field.getDictionary().getDictionaryObject(
                "Rect");
        PDRectangle pdrect = new PDRectangle(0, 0);
        if (rectArray instanceof COSArray) {
            pdrect = new PDRectangle((COSArray) rectArray);
        } else if (rectArray != null && rectArray.getCOSObject() instanceof COSArray) {
            pdrect = new PDRectangle((COSArray) rectArray.getCOSObject());
        }
        return pdrect;
    }

    public static PDSignatureField swapTextWithSignature(PDField box, List<PDField> fields) {
            PDSignatureField sig = null;
            try {
                PDAcroForm acroForm;
                acroForm = box.getAcroForm();
                acroForm.getDictionary().setDirect(true);
                PDSignature sigobj = new PDSignature();
                sig = new PDSignatureField(acroForm);
                sig.getDictionary().setDirect(true);
                sig.setSignature(sigobj);
                sig.getWidget().setPage(box.getWidget().getPage());
                sig.setPartialName(box.getPartialName());
                sig.setAlternateFieldName(box.getAlternateFieldName());
                sig.getDictionary().setItem(COSName.RECT, box.getDictionary().getItem(COSName.RECT));
                sig.getDictionary().setNeedToBeUpdate(true);
                acroForm.getDictionary().setInt(COSName.SIG_FLAGS, 3);
                acroForm.getDictionary().setNeedToBeUpdate(true);
                fields.remove(box);
                fields.add(sig);
            } catch (IOException ioe) {

            }
            return sig;

        }


    public static Predicate<PDField> withFullyQualifiedName(final String name){
        return new Predicate<PDField>(){

            @Override
            public boolean apply(@Nullable PDField pdField) {
                try {
                    return pdField != null ? pdField.getFullyQualifiedName().equals(name) : false;
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                return false;
            }
        };
    }

    public static List<PDField> fromDocument(PDDocument document){
        return fromDocument().apply(document);
    }

    public static Function<PDDocument, List<PDField>> fromDocument(){
        return new Function<PDDocument, List<PDField>>(){

            @Override
            public List<PDField> apply(@Nullable PDDocument pdDocument) {
                if(pdDocument != null &&
                        pdDocument.getDocumentCatalog() != null &&
                        pdDocument.getDocumentCatalog().getAcroForm() != null) {
                    try {
                        return pdDocument.getDocumentCatalog().getAcroForm().getFields();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        };
    }

    public static Predicate<PDField> withDictionaryKey(final String key){
        return new Predicate<PDField>(){

            @Override
            public boolean apply(@Nullable PDField pdField) {
                if(pdField == null){return false;}
                return pdField.getDictionary().getString(key) != null;
            }
        } ;
    }

    public static Function<PDField, String> extractDictionaryString(final String key){
        return new Function<PDField, String>(){

            @Override
            public String apply(@Nullable PDField pdField) {
                return pdField != null ? pdField.getDictionary().getString(key) : null;
            }
        };
    }
}


