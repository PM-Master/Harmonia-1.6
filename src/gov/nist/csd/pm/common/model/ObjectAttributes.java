package gov.nist.csd.pm.common.model;

import gov.nist.csd.pm.common.application.NullSysCaller;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.browser.PmNode;
import gov.nist.csd.pm.common.browser.PmNodeType;
import gov.nist.csd.pm.common.model.proto.MutableProperty;
import gov.nist.csd.pm.common.model.proto.ObjectProxy;
import gov.nist.csd.pm.common.model.proto.Property;
import gov.nist.csd.pm.common.model.proto.SysCallerImmutableProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/20/11
 * Time: 4:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class ObjectAttributes {

    /**
     * Creates an Object attribute object from a PmNode.  For id or name missing in the PmNode
     * this method will attempt to retrieve that information from the supplied SysCaller.
     * @param node
     * @param sysCaller
     * @return
     */
    public static ObjectAttribute createFromPmNode(PmNode node, SysCaller sysCaller){
        if(!node.getNodeType().equals(PmNodeType.OBJECT_ATTRIBUTE)){
            throw new RuntimeException("Type of node provided should have Object Attribute type");
        }
        ObjectAttribute oattr = ObjectProxy.getProxyFor(ObjectAttribute.class);
        String name = node.getName();
        String id = node.getId();
        if(name == null && id != null){
            name = sysCaller.getNameOfEntityWithIdAndType(id, node.getType());
        }
        if(id == null && name != null){
            id = sysCaller.getIdOfEntityWithNameAndType(name, node.getType());
        }
        oattr.name().set(name);
        oattr.id().set(id);

        return oattr;
    };

    /**
     * Basic utility method to create an object attribute from a PmNode with object attribute type.
     * a runtime exception will  be thrown if the type supplied does not match the object attribute defined type.
     * @param node
     * @return
     */
    public static ObjectAttribute createFromPmNode(PmNode node){
        return createFromPmNode(node, new NullSysCaller());
    }

    public static ObjectAttribute createFromNameAndSysCaller(String name, SysCaller sysCaller){
        return new SysCallerObjectAttribute(sysCaller, name);
    }


    /**
	 * @author  Administrator
	 */
    static class SysCallerObjectAttribute extends BaseObjects.SysCallerBaseObject implements ObjectAttribute{


        protected static final String ASSOCIATED_OBJECT = "associatedObject";
        protected static final String TO_CONNECTOR = "toConnector";
        protected static final String TO_POLICY = "toPolicy";
        protected static final String FROM_OP_SET = "fromOpSet";
        protected static final String PROPERTIES = "properties";
        protected static final String KEY = "key";
        protected static final String TEMPLATE_ID = "templateId";

        public SysCallerObjectAttribute(SysCaller sysCaller, String name) {
            super(sysCaller, name);
        }


        private List<ObjectAttribute> comps = newArrayList();

        @Override
        public List<ObjectAttribute> getComponents() {
            return comps;
        }
        /**
		 * @uml.property  name="tid"
		 * @uml.associationEnd  
		 */
        private Property<String> tid = new MutableProperty<String>(String.class, TEMPLATE_ID);
        @Override
        public Property<String> templateId() {
            return tid;
        }
        /**
		 * @uml.property  name="key"
		 * @uml.associationEnd  
		 */
        private Property<String> key = new MutableProperty<String>(String.class, KEY);

        @Override
        public Property<String> key() {
            return key;
        }


        /**
		 * @uml.property  name="propsProperty"
		 * @uml.associationEnd  
		 */
        private Property<Map<String, String>> propsProperty =
                new SysCallerImmutableProperty(Map.class, PROPERTIES, getSysCaller()) {
            @Override
            protected Map<String, String> getWithSysCaller(SysCaller sysCaller) {
                return sysCaller.getPropertiesFor(id().get());
            }
        };
        @Override
        public Property<Map<String, String>> properties() {
            return propsProperty;
        }

        /**
		 * @uml.property  name="fromOpSet"
		 * @uml.associationEnd  
		 */
        private Property<OperationSet> fromOpSet = new MutableProperty<OperationSet>(OperationSet.class, FROM_OP_SET);
        @Override
        public Property<OperationSet> fromOpSet() {
            return fromOpSet;
        }
        /**
		 * @uml.property  name="toPolicy"
		 * @uml.associationEnd  
		 */
        private Property<Policy> toPolicy = new MutableProperty<Policy>(Policy.class, TO_POLICY);

        @Override
        public Property<Policy> toPolicy() {
            return toPolicy;
        }
        /**
		 * @uml.property  name="toConnector"
		 * @uml.associationEnd  
		 */
        private Property<Connector> toConnector = new MutableProperty<Connector>(Connector.class, TO_CONNECTOR);

        @Override
        public Property<Connector> toConnector() {
            return toConnector;
        }
        private List<ObjectAttribute> fromAttributes = new ArrayList<ObjectAttribute>();

        @Override
        public List<ObjectAttribute> fromAttributes() {
            return fromAttributes;
        }

        private List<ObjectAttribute> toAttributes = new ArrayList<ObjectAttribute>();

        @Override
        public List<ObjectAttribute> toAttributes() {
            return toAttributes;
        }

        /**
		 * @uml.property  name="associatedObject"
		 * @uml.associationEnd  
		 */
        Property<BaseObject> associatedObject = new MutableProperty<BaseObject>(BaseObject.class, ASSOCIATED_OBJECT);
        @Override
        public Property<BaseObject> associatedObject() {
            return associatedObject;
        }
    }



}
