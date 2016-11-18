package gov.nist.csd.pm.application.workflow;

import gov.nist.csd.pm.common.action.PMKit;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 9/15/11
 * Time: 1:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class WorkflowKit extends PMKit {
    public static final String PDF_REJECTION_REFERENCE_KEY = String.format("_%s_%s", WorkflowKit.class.getPackage().getName().replace(".", "_"), "PDF_REJ_REF");
    public static final String PDF_SIGNATURE_REFERENCE_KEY = String.format("_%s_%s", WorkflowKit.class.getPackage().getName().replace(".","_"), "PDF_SIG_REF");
    public static final String PDF_SIGNATURE_ASSIGNMENT_KEY = String.format("_%s_%s", WorkflowKit.class.getPackage().getName().replace(".","_"), "PDF_SIG_ASSIGN_REF");




}
