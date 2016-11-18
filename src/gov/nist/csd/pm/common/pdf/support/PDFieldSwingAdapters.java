/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.nist.csd.pm.common.pdf.support;

import com.google.common.base.Function;
import gov.nist.csd.pm.common.action.ActionRef;
import gov.nist.csd.pm.common.action.FunctionRef;
import gov.nist.csd.pm.common.action.FunctionalityCenter;
import gov.nist.csd.pm.common.pdf.PDFDocumentActionLibrary;
import gov.nist.csd.pm.common.util.swing.DocumentListenerAdapterForAction;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckbox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextbox;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Strings.nullToEmpty;
import static gov.nist.csd.pm.common.action.ActionWrapper.createReference;
import static gov.nist.csd.pm.common.pdf.PDFFunctionalityContexts.PDF_ACROFORM_UI_HOOKING_CONTEXT;

/**
 * This class provides utilities for moving data from a Swing component to a PDF AcroForm and back again.
 * @author Administrator
 */
public final class PDFieldSwingAdapters {
    private PDFieldSwingAdapters(){}



    /**
     * Implements an Adapter design pattern which allows Swing components to talk
     * to PDFields and vice versa.
     * @param <TC> the type of Swing/AWT Component
     * @param <TF> the type of PDField.
     */
    public static interface PDFieldSwingAdapter <TC extends Component, TF extends PDField>{

        /**
         *
         * @return a compatible user interface for the associated PDField
         */
        public TC compatibleUI();

        /**
         * sets the PDField value based on what's in the Component
         */
        public void setFieldFromComponent();
        /**
         * sets the Component based on what's in the PDField value.
         */
        public void setComponentFromField();

        /**
         * Retrns the adapted PDF field for easy reference.
         * @return  the field this adapter wraps.
         */
        public TF getField();
        
        public ActionRef getActionRef();
    }

    /**
	 * Abstract interface for creating PDFieldSwingAdapters.
	 * @param  < TC  >
	 * @param  <TF >
	 */
    public abstract static class AbstractPDFieldSwingAdapter <TC extends Component, TF extends PDField> implements PDFieldSwingAdapter<TC,TF>{


        private final TF _field;
        private TC _ui;
        /**
		 * @uml.property  name="_action"
		 * @uml.associationEnd  
		 */
        private ActionRef _action;
        public AbstractPDFieldSwingAdapter(TF field, TC ui){
            _field = field;
            _ui = ui;
            try {
                String name = null == _field ? "" : _field.getFullyQualifiedName();
                ui.setName(name);
                setComponentFromField();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        @Override
        public TC compatibleUI(){
            return _ui;
        }

        @Override
        public TF getField(){
            return _field;
        }
        
        @Override
        public ActionRef getActionRef(){
        	return _action;
        }
        
        protected void setActionRef(ActionRef actionRef){
        	_action = actionRef;
        }

    }


    /**
     * PSFieldSwingAdapter that does nothing.  Provides a default implementation for
     * Swing/AWT components that have not yet had adapters written for them.
     */
    public static class DoNothingSwingAdapter extends AbstractPDFieldSwingAdapter{
        public DoNothingSwingAdapter(PDField field){
            super(field, new JPanel());
        }



        @Override
        public void setFieldFromComponent() {
        }

        @Override
        public void setComponentFromField() {
        }


    }


    /**
     * Get the appropriate adapter for a JComponent
     * @param field A PDField for which an appropriate swing interface is required.
     * @return the component wrapped in a PDFieldSwingAdapter.  This method will
     * never return a null value.  Worst case it will return a DoNothingSwingAdapter
     * which will have no affect on the system.
     */
    public static Component getComponentForField(PDField field){
        FunctionRef ref = FunctionalityCenter.INSTANCE.getFunctionRef(field.getClass(), Component.class, PDF_ACROFORM_UI_HOOKING_CONTEXT);
        return ref != null ? (Component) ref.apply(field) : new DoNothingSwingAdapter(field).compatibleUI();
    }

    /**
     * Default adapter for working with JTextComponent's; JTextField, JTextArea, JPasswordField, etc.
     */
    public static class TextComponentSwingAdapter extends AbstractPDFieldSwingAdapter<JTextComponent,PDField>{

        public TextComponentSwingAdapter(PDField textField){
            super(textField, new JTextArea());
            DocumentListenerAdapterForAction updateListener = new DocumentListenerAdapterForAction(new AbstractAction(){

                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    if(actionEvent.getSource() instanceof DocumentEvent){
                        String text = ((DocumentEvent)actionEvent.getSource()).getDocument().toString();

                        try {
                            getField().setValue(text);
                        } catch (IOException e) {
                            Logger.getLogger(PDFieldSwingAdapters.class.getName()).log(Level.SEVERE, null, e);
                        }
                    }

                }
            });
            compatibleUI().getDocument().addDocumentListener(updateListener);
        }

        @Override
        public void setFieldFromComponent() {
            try {
                getField().setValue(compatibleUI().getText());
            } catch (IOException ex) {
                Logger.getLogger(PDFieldSwingAdapters.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void setComponentFromField() {
            try {
                compatibleUI().setText(getField().getValue());
            } catch (IOException ex) {
                Logger.getLogger(PDFieldSwingAdapters.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


    }


    private static final String PM_SIGNATURE_NAME_KEY  = "_gov_nist_csd_pm_SIGNATURE_NAME";
    private static final String PM_SIGNATURE_NAME_DEFAULT = "NOT SIGNED";
    private static final String PM_SIGNATURE_NAME_PLACEHOLDER = "JOHN Q SIGNATURE";

    public static class SignatureFieldSwingAdapter extends AbstractPDFieldSwingAdapter<PDFSignatureField, PDSignatureField>{




        public SignatureFieldSwingAdapter(PDSignatureField sigField) {
            super(sigField, new PDFSignatureField());
            ActionRef signAction =
            createReference(new AbstractAction() {
            	{
            		putValue(Action.NAME, "SIGN");
            		putValue(ActionRef.ID, PDFDocumentActionLibrary.SIGN_PDF.name());
            		putValue(ActionRef.TARGET, getField());
            	}
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    setFieldFromComponent();

                }
            });
            
            compatibleUI().getSignButton().setAction(signAction);
            setActionRef(signAction);

        };

        @Override
        public void setFieldFromComponent() {
            COSDictionary dict = getField().getDictionary();
            dict.setString(PM_SIGNATURE_NAME_KEY, PM_SIGNATURE_NAME_PLACEHOLDER);
            compatibleUI().setAppearance(PM_SIGNATURE_NAME_PLACEHOLDER);
            compatibleUI().setSigned(true);
        }

        @Override
        public void setComponentFromField() {
            String value = getSignatureFieldValue(getField());
            boolean signed = isSigned(getField());
            compatibleUI().setSigned(signed);
            compatibleUI().setAppearance(value);
        }

        private static boolean isSigned(PDField field){
            return field.getDictionary().getString(PM_SIGNATURE_NAME_KEY) != null;
        }

        private static String getSignatureFieldValue(PDField field) {
            return field.getDictionary().getString(PM_SIGNATURE_NAME_KEY, PM_SIGNATURE_NAME_DEFAULT);
        }


    }


    public static final String YES_OPTION = "yes";
    public static final String NO_OPTION = "no";

    /**
     * Default adapter for a JToggleButton: JCheckBox and JRadioButton
     */
    public static class ToggleButtonSwingAdapter extends AbstractPDFieldSwingAdapter<JToggleButton, PDField>{

        public ToggleButtonSwingAdapter(PDField field){
            super(field, new JCheckBox());
        }

        @Override
        public void setFieldFromComponent() {
            try {
                getField().setValue(compatibleUI().isSelected() ? YES_OPTION : NO_OPTION);
            } catch (IOException ex) {
                Logger.getLogger(PDFieldSwingAdapters.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void setComponentFromField() {
            try {
                String value = nullToEmpty(getField().getValue()).toLowerCase();
                compatibleUI().setSelected(value.equals(YES_OPTION));
            } catch (IOException ex) {
                Logger.getLogger(PDFieldSwingAdapters.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


    }

    public static final Comparator<Object> EQUALITY_COMPARATOR = new Comparator(){

        @Override
        public int compare(Object t, Object t1) {
            return t.equals(t1) ? 0 : -1;
        }

    };





    static {


        FunctionalityCenter fc = FunctionalityCenter.INSTANCE;
        fc.addFunctionality(new Function<PDCheckbox, Component>(){

            @Override
            public Component apply(@Nullable PDCheckbox pdCheckbox) {
                PDFieldSwingAdapter adapt = new ToggleButtonSwingAdapter(pdCheckbox);
                return adapt.compatibleUI();
            }
        }, PDCheckbox.class, Component.class, PDF_ACROFORM_UI_HOOKING_CONTEXT);

        fc.addFunctionality(new Function<PDTextbox, Component>(){

            @Override
            public Component apply(@Nullable PDTextbox pdCheckbox) {
                PDFieldSwingAdapter adapt = new TextComponentSwingAdapter(pdCheckbox);
                return adapt.compatibleUI();
            }
        }, PDTextbox.class, Component.class, PDF_ACROFORM_UI_HOOKING_CONTEXT);

        fc.addFunctionality(new Function<PDSignatureField, Component>(){

            @Override
            public Component apply(@Nullable PDSignatureField pdCheckbox) {
                PDFieldSwingAdapter adapt = new SignatureFieldSwingAdapter(pdCheckbox);
                return adapt.compatibleUI();
            }
        }, PDSignatureField.class, Component.class, PDF_ACROFORM_UI_HOOKING_CONTEXT);

        fc.addFunctionality(new Function<PDField, Component>(){

            @Override
            public Component apply(@Nullable PDField pdCheckbox) {
                PDFieldSwingAdapter adapt = new DoNothingSwingAdapter(pdCheckbox);
                return adapt.compatibleUI();
            }
        }, PDField.class, Component.class, PDF_ACROFORM_UI_HOOKING_CONTEXT);



    }




  







}
