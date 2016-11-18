package gov.nist.csd.pm.application.workflow.model;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import gov.nist.csd.pm.common.application.PMIOObject;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.browser.PmGraphType;
import gov.nist.csd.pm.common.model.BaseObjects;
import gov.nist.csd.pm.common.model.VirtualObject;
import gov.nist.csd.pm.common.model.VirtualObjects;
import gov.nist.csd.pm.common.model.proto.ImmutableProperty;
import gov.nist.csd.pm.common.model.proto.Property;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static com.google.common.base.Functions.compose;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Lists.transform;
import static gov.nist.csd.pm.common.model.VirtualObjects.getPath;
import static gov.nist.csd.pm.common.util.collect.Arrays.getAtIndex;
import static gov.nist.csd.pm.common.util.lang.Strings.endingWith;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 9/15/11
 * Time: 2:25 PM
 * To change this template use File | Settings | File Templates.
 */
public final class Workflows {

    public static final String PROP_KEY_TYPE_OF = "typeof";
    public static final String EXT_INFO = "config";
    public static final String EXT_FORM = "pdf";
    public static final String EXT_ATTACHMENT = "attach";
    public static final String EXT_SIGNATURE = "sign";
    public static final String EXT_REJECT = "reject";

    private Workflows() {

    }

    /**
     * Determines whether or not this container is a workflow container.
     *
     * @param id
     * @return
     */
    public static boolean isWorkflow(SysCaller sysCaller, String id) {
        checkNotNull(sysCaller);
        checkNotNull(id);
        String type = sysCaller.getEntityType(id);
        String name = sysCaller.getNameOfEntityWithIdAndType(id, type);
        List<String[]> members = sysCaller.getMembersOf(name, id, type, PmGraphType.USER.typeCode());
        List<VirtualObject> vObjects = transform(members,
                compose(convertToVirtualObject(sysCaller), getName()));
        return Iterables.any(vObjects, isAWorkflowConfigObject(sysCaller));
    }

    private static Function<String[], String> getName() {
        return getAtIndex(2, "");
    }


    private static Predicate<VirtualObject> isAWorkflowConfigObject(final SysCaller sysCaller) {
        return new Predicate<VirtualObject>() {

            @Override
            public boolean apply(@Nullable VirtualObject input) {
                if (input != null && input.path().get() != null && input.path().get().endsWith(EXT_INFO)) {
                    PMIOObject pmio = sysCaller.openObject4(input.name().get(), "File read");
                    Properties props = new Properties();
                    try {
                        props.load(pmio.getInputStream());
                        return props.getProperty("type", "").equals("workflow");
                    } catch (IOException e) {
                        return false;
                    }
                }
                return false;
            }
        };
    }

    ;

    public static Workflow open(String name, SysCaller sysCaller) {
        return new SysCallerWorkflow(sysCaller, name);
    }

    /**
	 * @author  Administrator
	 */
    static class SysCallerWorkflow extends BaseObjects.SysCallerBaseObject implements Workflow {

        public SysCallerWorkflow(SysCaller sysCaller, final String name) {
            super(sysCaller, name);

        }


        @Override
        public Collection<VirtualObject> children() {
            String id = id().get();
            String name = name().get();
            String type = getSysCaller().getEntityType(id);
            List<String[]> members = getSysCaller().getMembersOf(name, id, type, PmGraphType.USER.typeCode());
            return transform(members, compose(convertToVirtualObject(getSysCaller()), getName()));
        }

        @Override
        public Collection<VirtualObject> attachments() {
            return filter(children(), Predicates.compose(endingWith(EXT_ATTACHMENT), getPath()));
        }


        @Override
        public Collection<VirtualObject> signatureObjects() {
            return filter(children(), Predicates.compose(endingWith(EXT_SIGNATURE), getPath()));
        }

        @Override
        public Collection<VirtualObject> rejectObjects() {
            return filter(children(), Predicates.compose(endingWith(EXT_SIGNATURE), getPath()));
        }

        /**
		 * @uml.property  name="form"
		 * @uml.associationEnd  
		 */
        Property<VirtualObject> form = new ImmutableProperty<VirtualObject>(VirtualObject.class, "form", null) {

            @Override
            public VirtualObject get() {
                return find(children(), Predicates.compose(endingWith(EXT_FORM), getPath()));
            }
        };

        @Override
        public Property<VirtualObject> form() {
            return form;
        }
    }

    private static Function<String, VirtualObject> convertToVirtualObject(final SysCaller sysCaller) {
        return new Function<String, VirtualObject>() {

            @Override
            public VirtualObject apply(@Nullable String name) {
                return VirtualObjects.createFromNameAndSysCaller(name, sysCaller);
            }
        };
    }


}
