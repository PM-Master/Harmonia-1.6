package gov.nist.csd.pm.common.model;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.model.proto.ImmutableProperty;
import gov.nist.csd.pm.common.model.proto.Property;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.net.PacketException;

import javax.annotation.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 9/21/11
 * Time: 4:45 PM
 * To change this template use File | Settings | File Templates.
 */
public final class VirtualObjects {
    private VirtualObjects() {
    }

    public static VirtualObject createFromNameAndSysCaller(String name, SysCaller sysCaller) {
        return new SysCallerVirtualObject(sysCaller, name);
    }

    public static Function<VirtualObject, String> getPath() {
        return new Function<VirtualObject, String>() {
            @Override
            public String apply(@Nullable VirtualObject input) {
                return input != null ? input.path().get() : null;
            }
        };
    }

    /**
	 * @author  Administrator
	 */
    static class SysCallerVirtualObject extends BaseObjects.SysCallerBaseObject implements VirtualObject {

        String[] objInfo = null;
        /**
		 * @uml.property  name="path"
		 * @uml.associationEnd  
		 */
        Property<String> path;
        /**
		 * @uml.property  name="host"
		 * @uml.associationEnd  
		 */
        Property<String> host;


        public SysCallerVirtualObject(SysCaller sysCaller, final String name) {
            super(sysCaller, name);
            path = new ImmutableProperty<String>(String.class, "path", "") {

                @Override
                public String get() {
                    return checkAndGetObjInfo()[SysCaller.IDX_GET_OBJ_INFO_PATH];
                }
            };
            host = new ImmutableProperty<String>(String.class, "host", "") {

                @Override
                public String get() {
                    return checkAndGetObjInfo()[SysCaller.IDX_GET_OBJ_INFO_HOST];
                }
            };
        }

        private String[] checkAndGetObjInfo() {
            if (objInfo == null) {
                Packet p = getSysCaller().getObjInfo(id().get());
                if (p != null && !p.hasError() && p.getItems().size() > 0) {
                    try {
                        objInfo = p.getItemStringValue(0).split(SysCaller.PM_ALT_DELIM_PATTERN);
                    } catch (PacketException e) {
                        Throwables.propagate(e);
                    }
                } else {
                    objInfo = new String[7];
                }
            }
            return objInfo;
        }

        ;

        @Override
        public Property<String> path() {
            return path;
        }

        @Override
        public Property<String> host() {
            return host;
        }


    }
}
