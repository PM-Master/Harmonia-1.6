package gov.nist.csd.pm.common.model;

import gov.nist.csd.pm.common.application.NullSysCaller;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.browser.PmNode;
import gov.nist.csd.pm.common.browser.PmNodeType;
import gov.nist.csd.pm.common.model.proto.ImmutableProperty;
import gov.nist.csd.pm.common.model.proto.ObjectProxy;
import gov.nist.csd.pm.common.model.proto.Property;
import gov.nist.csd.pm.common.model.proto.SysCallerImmutableProperty;
import gov.nist.csd.pm.common.net.Packet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Objects.firstNonNull;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 9/15/11
 * Time: 3:22 PM
 * To change this template use File | Settings | File Templates.
 */
public final class BaseObjects {
    private BaseObjects(){}



    public static PmNodeType typeOfBaseObject(BaseObject obj){

        PmNodeType returnType = PmNodeType.INVALID;
        if(obj instanceof BaseObject){
            returnType = PmNodeType.OBJECT;
        }
        if(obj instanceof Connector){
            returnType = PmNodeType.CONNECTOR;
        }

        if(obj instanceof User){
            returnType = PmNodeType.USER;

        }
        if(obj instanceof UserAttribute){
            returnType = PmNodeType.USER_ATTRIBUTE;

        }
        if(obj instanceof Policy){
            returnType = PmNodeType.POLICY;

        }
        if(obj instanceof ObjectAttribute){
            returnType = PmNodeType.OBJECT_ATTRIBUTE;
        }
        if(obj instanceof Container){
            returnType = PmNodeType.CONTAINER;
        }
        return returnType;

    }
    public static PmNode pmNodeFromBase(BaseObject obj){
        return new PmNode(typeOfBaseObject(obj).typeCode(), obj.id().get(), obj.name().get());
    }
    public static BaseObject baseObjectForType(PmNodeType type){

        BaseObject returnObject = null;
        switch(type){
           case USER:
               returnObject = ObjectProxy.getProxyFor(User.class);
               break;
           case USER_ATTRIBUTE:
               returnObject = ObjectProxy.getProxyFor(UserAttribute.class);
               break;
           case OBJECT:
               returnObject = ObjectProxy.getProxyFor(BaseObject.class);
               break;
           case OBJECT_ATTRIBUTE:
               returnObject = ObjectProxy.getProxyFor(ObjectAttribute.class);
               break;
           case POLICY:
               returnObject = ObjectProxy.getProxyFor(Policy.class);
               break;
           case CONTAINER:
               returnObject = ObjectProxy.getProxyFor(Container.class);
               break;
           case CONNECTOR:
               returnObject = ObjectProxy.getProxyFor(Connector.class);
               break;
        }
        return returnObject;
    }
    public static Class<?> baseObjectClassForType(PmNodeType type){
        Class<?> returnClass = BaseObject.class;
        switch(type){
           case USER:
               returnClass = User.class;
               break;
           case USER_ATTRIBUTE:
               returnClass = UserAttribute.class;
               break;
           case OBJECT:
               returnClass = BaseObject.class;
               break;
           case OBJECT_ATTRIBUTE:
               returnClass = ObjectAttribute.class;
               break;
           case POLICY:
               returnClass = Policy.class;
               break;
           case CONTAINER:
               returnClass = Container.class;
               break;
           case CONNECTOR:
               returnClass = Connector.class;
               break;
        }
        return returnClass;
    }

       /**
	 * @author  Administrator
	 */
    public static class SysCallerBaseObject implements BaseObject{
        protected static final String INCLUDES_ASCENDANTS = "includesAscendants";
        protected static final String OTHER_INFO = "otherInfo";
        protected static final String ID = "id";
        protected static final String NAME = "name";
        protected static final String DESCRIPTION = "description";
        protected static final String PM_CLASS = "pmClass";
        /**
		 * @uml.property  name="sysCaller"
		 * @uml.associationEnd  
		 */
        private final SysCaller sysCaller;
        private final String name;
        public SysCallerBaseObject(SysCaller sysCaller, final String name){
            this.sysCaller = firstNonNull(sysCaller, new NullSysCaller());
            this.name = firstNonNull(name, "");
            pmClassProperty = new SysCallerImmutableProperty<String>(String.class, PM_CLASS, sysCaller) {
            @Override
            protected String getWithSysCaller(SysCaller sysCaller) {
                return getObjInfoValue(sysCaller, name());
            }

        };
            descProperty =   new SysCallerImmutableProperty<String>(String.class, DESCRIPTION, sysCaller) {
            @Override
            protected String getWithSysCaller(SysCaller sysCaller) {
                return getObjInfoValue(sysCaller, name());
            }
        };
            idProperty =  new SysCallerImmutableProperty<String>(String.class, ID, sysCaller) {
            @Override
            protected String getWithSysCaller(SysCaller sysCaller) {
                return sysCaller.getIdOfEntityWithNameAndType(name, ObjectAttribute.TYPE_CODE);
            }
        };
            otherInfo = new SysCallerImmutableProperty<String>(String.class, OTHER_INFO, sysCaller) {
            @Override
            protected String getWithSysCaller(SysCaller sysCaller) {
                return getObjInfoValue(sysCaller, name());
            }
        };

            includesAscendants = new SysCallerImmutableProperty<Boolean>(Boolean.class, INCLUDES_ASCENDANTS, sysCaller) {
            @Override
            protected Boolean getWithSysCaller(SysCaller sysCaller) {
                String res = getObjInfoValue(sysCaller, name()).toLowerCase();
                return "yes".equals(res);
            }
        };

            nameProperty = new ImmutableProperty<String>(String.class, NAME, name);
        }

        Map<String, String> objInfo = Collections.synchronizedMap(new HashMap<String, String>());

        protected String getObjInfoValue(SysCaller sysCaller, String key){
            if(!objInfo.containsKey(key)){
                Packet packet = sysCaller.getObjInfo(id().get());
                String item = packet.getStringValue(1);
                String[] returnedInfo = item.split(SysCaller.PM_FIELD_DELIM);
                String klass = returnedInfo[SysCaller.IDX_GET_OBJ_INFO_CLASS];
                String inc = returnedInfo[SysCaller.IDX_GET_OBJ_INFO_INCLUDES];
                objInfo.put(PM_CLASS, klass);
                objInfo.put(INCLUDES_ASCENDANTS, inc);


            }
            return objInfo.get(key);
        }

        /**
		 * @return
		 * @uml.property  name="sysCaller"
		 */
        protected SysCaller getSysCaller(){
            return sysCaller;
        }

        /**
		 * @uml.property  name="pmClassProperty"
		 * @uml.associationEnd  
		 */
        private final Property<String> pmClassProperty;
        @Override
        public Property<String> pmClass() {
            return pmClassProperty;
        }

        /**
		 * @uml.property  name="descProperty"
		 * @uml.associationEnd  
		 */
        private final Property<String> descProperty;
        @Override
        public Property<String> description() {
            return descProperty;
        }

        /**
		 * @uml.property  name="nameProperty"
		 * @uml.associationEnd  
		 */
        private final Property<String> nameProperty;

        @Override
        public Property<String> name() {
            return nameProperty;
        }

        /**
		 * @uml.property  name="idProperty"
		 * @uml.associationEnd  
		 */
        private final Property<String> idProperty;
        @Override
        public Property<String> id() {
            return idProperty;
        }


        /**
		 * @uml.property  name="otherInfo"
		 * @uml.associationEnd  
		 */
        private final Property<String> otherInfo;
        @Override
        public Property<String> otherInfo() {
            return otherInfo;
        }

        /**
		 * @uml.property  name="includesAscendants"
		 * @uml.associationEnd  
		 */
        private final Property<Boolean> includesAscendants;
        @Override
        public Property<Boolean> includesAscendants() {
            return includesAscendants;
        }
    }
}
