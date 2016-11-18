package gov.nist.csd.pm.application.workflow;

import com.google.common.base.Objects;
import gov.nist.csd.pm.common.application.PMIOObject;
import gov.nist.csd.pm.common.application.PmCreator;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.browser.PmNode;
import gov.nist.csd.pm.common.browser.PmNodeType;
import gov.nist.csd.pm.common.io.DocumentSerializer;
import gov.nist.csd.pm.common.io.DocumentSerializers;
import gov.nist.csd.pm.common.model.ObjectAttribute;
import gov.nist.csd.pm.common.model.ObjectAttributes;
import gov.nist.csd.pm.common.pattern.BuildsForParent;
import gov.nist.csd.pm.common.pattern.Emits;
import gov.nist.csd.pm.common.util.EVERTransition;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 8/15/11
 * Time: 6:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class WorkflowLayout implements PmCreator, Emits<ArrayList<String>> {
    /**
	 * @uml.property  name="parentContainer"
	 * @uml.associationEnd  
	 */
    private PmNode parentContainer;
    /**
	 * @uml.property  name="layoutNodeInfo"
	 * @uml.associationEnd  
	 */
    private PmNode layoutNodeInfo;
    /**
	 * @uml.property  name="childEntities"
	 */
    private List<Builder.PmNodeCreator> childEntities = new ArrayList<Builder.PmNodeCreator>();
    /**
	 * @uml.property  name="transitions"
	 */
    private List<EVERTransition> transitions = new ArrayList<EVERTransition>();


    protected WorkflowLayout() {
        this("");
    }

    protected WorkflowLayout(String name) {
        layoutNodeInfo = PmNode.createContainerNode(name);
    }

    public List<EVERTransition> transitions() {
        return transitions;
    }

    public List<? extends PmNode> children() {
        return childEntities;
    }

    private static final String TEMPLATE = "%s %s cannot be null";
    private static final String CONTEXT = "WorkflowLayout";

    private void validateLayout() {
        checkNotNull(layoutNodeInfo, TEMPLATE, CONTEXT, "layoutNode");
        checkNotNull(parentContainer, TEMPLATE, CONTEXT, "parentContainer (Container/ObjectAttribute)");
    }

    @Override
    public String create(SysCaller sysCaller) {
        String handle = "";
        try {
            validateLayout();
            ObjectAttribute parentAttribute = ObjectAttributes.createFromPmNode(parentContainer, sysCaller);
            ObjectAttribute workflowSpec = ObjectAttributes.createFromPmNode(layoutNodeInfo);

            String parentName = workflowSpec.name().get();
            handle = sysCaller.addContainer(workflowSpec, parentAttribute);
            //sysCaller.setPermissions(workflowSpec.id().get(), PmEngine.PM_OATTR_ASSIGN, PmEngine.PM_OATTR_ASSIGN_TO, PmEngine.PM_OATTR_CREATE_OBJ, PmEngine.PM_OATTR_CREATE_OPSET);
            /*
             * Serban, everything below this either non-working or untested.
             * 1.  the creator.create... calls fail because the necessary permission does not exist
             *     on the newly created workflow object attribute container, see two lines above this comment (line 75).
             *     An example of the call I think will solve this is on the line following that (line 76).
             *     It is not implemented and will currently throw an exception if called.  This was intentional to
             *     serve as a warning.
             * 2.  List<String> script = emit() is untested.  It appears to generate a well formed script though.
             * 3.  sysCaller.addScript(script) is untested.  It will need to be
             *     verified in order to complete the workflow creation subroutine.

             */
            for (Builder.PmNodeCreator creator : childEntities) {
                creator.create(sysCaller);
            }
            List<String> script = emit();
            sysCaller.addScript(script);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return handle;
    }


    @Override
    public ArrayList<String> emit() {
        ArrayList<String> strings = newArrayList("workflow\n");

        for (EVERTransition transition : transitions) {
            strings.add(transition.emit());
            strings.add("\n\n");
        }

        return strings;
    }


    /**
	 * @author  Administrator
	 */
    public static class Builder {

        /**
		 * @uml.property  name="layout"
		 * @uml.associationEnd  
		 */
        WorkflowLayout layout = new WorkflowLayout();

        public Builder named(String name) {
            layout.layoutNodeInfo = PmNode.createContainerNode(name);
            return this;
        }

        public Builder withParent(PmNode parent) {
            layout.parentContainer = parent;
            if (previousTransitionPoint == null) {
                previousTransitionPoint = parent;
            }
            return this;
        }

        public Builder withChildFile(DocumentSerializer data) {
            layout.childEntities.add(creatorForDataStream(data, layout.layoutNodeInfo));
            return this;
        }

        public Builder withChild(PmNode child) {

            layout.childEntities.add(creatorForNode(child));
            return this;
        }

        public Builder withChildren(PmNode... children) {
            for (PmNode nodeInfo : children) {
                withChild(creatorForNode(nodeInfo));
            }
            return this;
        }


        private final List<WorkflowTransitionBuilder> transitionBuilders = new ArrayList();
        /**
		 * @uml.property  name="previousTransitionPoint"
		 * @uml.associationEnd  
		 */
        private PmNode previousTransitionPoint;


        public WorkflowTransitionBuilder withTransition() {
            WorkflowTransitionBuilder builder = new WorkflowTransitionBuilder();
            transitionBuilders.add(builder);
            builder.from(previousTransitionPoint);
            return builder;
        }


        public class WorkflowTransitionBuilder
                extends EVERTransitions.UserTransitionBuilder
                implements BuildsForParent<EVERTransition, Builder> {

            @Override
            public Builder parent() {
                return Builder.this;
            }


            @Override
            public EVERTransitions.UserTransitionBuilder moving(PmNode node) {
                throw new UnsupportedOperationException("workflow transitions only move the workflow itself.  ");
            }


            @Override
            public EVERTransitions.UserTransitionBuilder from(PmNode node) {
                super.from(creatorForNode(node));
                return this;
            }


            @Override
            public EVERTransitions.UserTransitionBuilder to(PmNode to) {
                super.to(creatorForNode(to));
                return this;
            }

            @Override
            public EVERTransitions.UserTransitionBuilder triggeredOn(PmNode on) {
                super.triggeredOn(creatorForNode(on));
                return this;
            }


            private EVERTransitions.UserTransitionBuilder movingInternally(PmNode node) {
                return super.moving(node);
            }

            @Override
            public EVERTransition build() {

                return super.build();
            }

        }

        byte[] getContainerInfoBytes(){
            Properties containerProps = new Properties();
            containerProps.setProperty("type", "workflow");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                containerProps.store(baos, "Container info properties");
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return baos.toByteArray();
        }

        /*
         *     Serban,
         *     At this point no .config file is being generated for the workflow.
         *     This is the java .properties formatted file that
         *     Workflow will use to tell if it is actually looking at a Workflow document.
         *     It yet another one of the many child objects of a Workflow's object attribute container.
         *     The best thing to do will be to add an implicit withChildFile call into the
         *     getLayout() method of the Builder class below (around line 224).  In this call you'd pass
         *     in a DocumentSerializer representing the .config file.  There are examples of using
         *    withChildFile in the Workflow project.
         */
        /**
		 * @return
		 * @uml.property  name="layout"
		 */
        public WorkflowLayout getLayout() {
            //Add a container information file to this container.
            withChildFile(DocumentSerializers.serializerFor(getContainerInfoBytes(), layout.layoutNodeInfo.getName(), "config"));
            for (WorkflowTransitionBuilder builder : transitionBuilders) {
                layout.transitions.add(builder.movingInternally(layout.layoutNodeInfo).build());
            }
            return layout;
        }

        public PmNodeCreator creatorForDataStream(DocumentSerializer is, PmNode parent) {
            String name = is.getDocumentName();
            String extension = is.getDocumentType();
            return new PmNodeCreator(new PmNode(PmNodeType.OBJECT.typeCode(), name, name), is, extension, parent);
        }



        public PmNodeCreator creatorForNode(PmNode node) {
            return new PmNodeCreator(node, null, null, null);
        }

        /**
		 * @author  Administrator
		 */
        class PmNodeCreator extends PmNode implements PmCreator {

            /**
			 * @uml.property  name="is"
			 * @uml.associationEnd  
			 */
            private final DocumentSerializer is;
            private final String ext;


            private PmNodeCreator(PmNode del, DocumentSerializer is, String ext, PmNode parent) {
                super(del.getType(), del.getId(), del.getName());
                this.is = is;
                this.ext = ext;
                setParent(parent);
            }


            @Override
            public String create(SysCaller sysCaller) {
                checkNotNull(sysCaller);

                String name = getName();
                String parentName = layout.layoutNodeInfo.getName();
                String type = "File";
                String parent = "b|" + parentName;
                String mode = "File write";
                String output = Objects.toStringHelper(this)

                        .addValue("Calling createObject3")
                        .add("name", name)
                        .add("type", type)
                        .add("ext", ext)
                        .add("parent", parent)
                        .add("mode", mode).toString();

                String handle = sysCaller.createObject3(name, type, ext, parent, mode, null, null, null, null);
                if (is != null) {
                    PMIOObject pmo = sysCaller.openObject4(name, "File write");
                    is.saveDocument(pmo.getOutputStream());
                }

                sysCaller.closeObject(handle);
                System.out.println(output);

                return name;
            }

            @Override
            public String toString() {
                return "PmNodeCreator{" +
                        "is=" + is +
                        ", ext='" + ext + '\'' +
                        ", name='" + getName() + "\'" +
                        ", id='" + getId() + "\'" +
                        ", type='" + getType() + "\'" +

                        '}';
            }
        }
    }


}
