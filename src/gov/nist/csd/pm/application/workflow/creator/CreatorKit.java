package gov.nist.csd.pm.application.workflow.creator;


import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import gov.nist.csd.pm.application.workflow.WorkflowKit;
import gov.nist.csd.pm.common.action.*;
import gov.nist.csd.pm.common.application.Notification;
import gov.nist.csd.pm.common.application.NotificationCenter;
import gov.nist.csd.pm.common.application.NotificationSubscriber;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.browser.BrowsingKit;
import gov.nist.csd.pm.common.browser.PmNode;
import gov.nist.csd.pm.common.browser.UserBrowserListener;
import gov.nist.csd.pm.common.pdf.PDFDocumentActionLibrary;
import gov.nist.csd.pm.common.pdf.PDFFormView;
import gov.nist.csd.pm.common.pdf.PDFFunctionalityContexts;
import gov.nist.csd.pm.common.pdf.notifications.SelectionForSignatureConversion;
import gov.nist.csd.pm.common.pdf.support.PDFields;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Predicates.isNull;
import static com.google.common.collect.Lists.newArrayList;
import static gov.nist.csd.pm.common.pdf.support.PDFields.withFullyQualifiedName;
import static gov.nist.csd.pm.common.util.collect.Arrays.isNullOrEmpty;
import static gov.nist.csd.pm.common.util.swing.SwingShortcuts.*;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/7/11
 * Time: 3:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreatorKit extends WorkflowKit {

    public static final String SUBMIT_WORKFLOW_ACTION_ID = "SubmitWorkflowAction";

    /**
	 * @uml.property  name="submitWorkflowAction"
	 * @uml.associationEnd  readOnly="true"
	 */
    @Inject
    @Named(SUBMIT_WORKFLOW_ACTION_ID)
    Action submitWorkflowAction;

    protected static final String TO_BE_SIGNED_BY = "To be signed by ";

    /**
	 * @author  Administrator
	 */
    private static class AddSignatureAction extends AbstractAction {
        /**
		 * @uml.property  name="selectedField"
		 */
        PDField selectedField;
        /**
		 * @uml.property  name="formView"
		 * @uml.associationEnd  
		 */
        PDFFormView formView;


        /**
		 * @return
		 * @uml.property  name="selectedField"
		 */
        public PDField getSelectedField() {
            return selectedField;
        }

        /**
		 * @param selectedField
		 * @uml.property  name="selectedField"
		 */
        public void setSelectedField(PDField selectedField) {
            this.selectedField = selectedField;
        }

        /**
		 * @return
		 * @uml.property  name="formView"
		 */
        public PDFFormView getFormView() {
            return formView;
        }

        /**
		 * @param formView
		 * @uml.property  name="formView"
		 */
        public void setFormView(PDFFormView formView) {
            this.formView = formView;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            List<Object> objects = newArrayList(selectedField, formView);
            if (Iterables.any(objects, isNull())) return;
            PDAcroForm form = selectedField.getAcroForm();
            try {
                final List<PDField> fields = form.getFields();

                PDField sigField = Iterables.find(fields, new Predicate<PDField>() {
                    private Predicate<PDField> replacementCheck = withFullyQualifiedName(selectedField.getFullyQualifiedName());

                    @Override
                    public boolean apply(@Nullable PDField pdField) {
                        return replacementCheck.apply(pdField);
                    }
                });
                if (sigField != null) {
                    PDFields.swapTextWithSignature(sigField, fields);
                    formView.reinitializeForm();
                    formView.revalidate();
                    formView.repaint();
                }
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            System.out.println("Hello work.");
        }


    }


    /**
	 * @uml.property  name="createSignatureFieldAction"
	 * @uml.associationEnd  
	 */
    private AddSignatureAction createSignatureFieldAction = new AddSignatureAction();

    @Override
    public void startup(ActionPublisher host) {
        super.startup(host);
        ActionRef addSignatureAction = Actions.findByID(host.publishedActions(), PDFDocumentActionLibrary.ADD_SIGNATURE.name());
        final Action originalSignatureAction = addSignatureAction;
        addSignatureAction.pushAction(createSignatureFieldAction);

        NotificationCenter.INSTANCE.subscribe(
                new NotificationSubscriber() {
                    @Override
                    public void receivedNotification(Notification n) {
                        originalSignatureAction.setEnabled(!isNullOrEmpty(n.getUserData()));
                        processSelectedFields(n.getUserData());
                    }
                },
                SelectionForSignatureConversion.TYPE);

        applicationActionGroup.insertActionAfter(Actions.actionWithId(PDFDocumentActionLibrary.SAVE_AS.name()), submitWorkflowAction);
    }


    private void processSelectedFields(Object[] fields) {
        if (isNullOrEmpty(fields)) return;
        if (fields[0] instanceof PDField) {
            createSignatureFieldAction.setSelectedField((PDField) fields[0]);
        }
        if (fields[1] instanceof PDFFormView) {
            createSignatureFieldAction.setFormView((PDFFormView) fields[1]);
        }

    }


    @Override
    public void registered(ActionPublisher host) {
        super.registered(host);
    }

    @Override
    public void actionAdded(ActionPublisher host, ActionRef action) {
        super.actionAdded(host, action);
    }

    @Override
    public void actionRemoved(ActionPublisher host, ActionRef action) {
        super.actionRemoved(host, action);
    }

    @Override
    public void shutdown(ActionPublisher host) {
        super.shutdown(host);
    }

    /**
	 * Functionality
	 * @uml.property  name="_sysCaller"
	 * @uml.associationEnd  
	 */


    @Inject
    SysCaller _sysCaller;



    @Inject
    public CreatorKit(FunctionalityCenter fc) {


        fc.addListener(
                new FunctionalityCenter.FunctionalityListener() {

                    @Override
                    public void functionalityAdded(FunctionRef function) {
                        if (function.takes().isAssignableFrom(PDSignatureField.class)) {
                            FunctionRef<PDSignatureField, Component> fnr = (FunctionRef<PDSignatureField, Component>) function;
                            fnr.push(new CreatorSignFieldFunction());
                        }
                    }

                    @Override
                    public void functionalityRemoved(FunctionRef function) {

                    }
                },
                Predicates.alwaysTrue(),
                PDFFunctionalityContexts.PDF_ACROFORM_UI_HOOKING_CONTEXT);

        fc.addListener(
                new FunctionalityCenter.FunctionalityListener() {

                    @Override
                    public void functionalityAdded(FunctionRef function) {
                        if (function.takes().isAssignableFrom(PDFFormView.class)) {
                            FunctionRef<PDFFormView, JComponent> pdfViewFn = (FunctionRef<PDFFormView, JComponent>) function;
                            pdfViewFn.push(new SelectInPDFFormViewFunction());
                        }
                    }

                    @Override
                    public void functionalityRemoved(FunctionRef function) {

                    }
                },
                Predicates.alwaysTrue(),
                PDFFunctionalityContexts.PDF_FORM_VIEW_HOOKING_CONTEXT);

    }




    private class SelectInPDFFormViewFunction implements Function<PDFFormView, JComponent> {

        @Override
        public JComponent apply(@Nullable PDFFormView pdfFormView) {
            final JLayeredPane lp = new JLayeredPane();
            final OverView overView = new OverView();
            final ComponentListener resizeContainerListener = new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent componentEvent) {

                    resizeComponentsToDimension(
                            calculateNeededBoundsForContainer(lp).getSize(),
                            lp, overView);
                }



                @Override
                public void componentMoved(ComponentEvent componentEvent) {
                    resizeComponentsToDimension(calculateNeededBoundsForContainer(lp).getSize(),
                            overView);
                }
            };
            final OverViewMouseAdapter ovma = new OverViewMouseAdapter(pdfFormView, overView);
            pdfFormView.addComponentListener(resizeContainerListener);
            pdfFormView.addContainerListener(new ContainerListener() {
                @Override
                public void componentAdded(ContainerEvent containerEvent) {
                    containerEvent.getChild().addMouseListener(ovma);
                }

                @Override
                public void componentRemoved(ContainerEvent containerEvent) {

                }
            });
            pdfFormView.addMouseListener(ovma);

            pdfFormView.addPageLoadingListener(overView);

            lp.add(pdfFormView, (Integer) (JLayeredPane.DEFAULT_LAYER + 50));
            lp.add(overView, (Integer) (JLayeredPane.DEFAULT_LAYER + 75));
            return lp;
        }
    }


    private static Rectangle calculateNeededBoundsForContainer(Container cont) {
        Rectangle currentBounds = cont.getBounds();
        for (Component comp : cont.getComponents()) {

            currentBounds.add(comp.getBounds());
        }

        return currentBounds;
    }


    private static Map<Class<?>, Function<Shape, Annotation>> optimizedAnnotationCreators =
            new HashMap<Class<?>, Function<Shape, Annotation>>() {
                Function<Shape, Annotation> rectangleOptimizer = new Function<Shape, Annotation>() {
                    @Override
                    public Annotation apply(@Nullable Shape shape) {
                        return new RectangleAnnotation(shape);
                    }
                };

                {
                    put(Rectangle2D.Float.class, rectangleOptimizer);
                    put(Rectangle2D.Double.class, rectangleOptimizer);
                    put(Rectangle.class, rectangleOptimizer);
                }
            };

    public static Annotation createAnnotationFor(Shape shape) {
        if (optimizedAnnotationCreators.containsKey(shape.getClass())) {
            return optimizedAnnotationCreators.get(shape.getClass()).apply(shape);
        } else {
            return new ShapeAnnotation(shape);
        }
    }

    private class CreatorSignFieldFunction implements Function<PDSignatureField, Component> {

        @Override
        public Component apply(@Nullable final PDSignatureField pdSignature) {
            if(pdSignature == null) return jLabel("NULL Signature provided");
            COSDictionary dict = pdSignature.getDictionary();
            final String signatureAssignment = dict.getString(PDF_SIGNATURE_ASSIGNMENT_KEY);
            String signatureDesc = signatureAssignment == null ? "Unassigned" : TO_BE_SIGNED_BY + signatureAssignment;

            final JLabel signatureDescLabel =  jLabel(signatureDesc);
            Action browseAction = BrowsingKit.browseUserAction(_sysCaller, new UserBrowserListener() {

                @Override
                public void userSelected(PmNode selectedNode) {
                    COSDictionary dict = pdSignature.getDictionary();
                    String name = selectedNode.getName();
                    dict.setString(PDF_SIGNATURE_ASSIGNMENT_KEY, name);
                    signatureDescLabel.setText(TO_BE_SIGNED_BY + name);
                }
            });
            JPanel panel = new JPanel();
            return build(panel, withLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS)),
                    addComponent(signatureDescLabel),
                    addComponent(Box.createHorizontalStrut(5)),
                    addComponent(jButton(browseAction)));


        }
    }




}
