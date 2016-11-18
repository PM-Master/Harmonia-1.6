package gov.nist.csd.pm.common.util;

import com.google.common.base.Joiner;
import gov.nist.csd.pm.common.browser.PmNode;
import gov.nist.csd.pm.common.model.User;

import java.util.UUID;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 8/11/11
 * Time: 12:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class EVERScript {

    protected static final String TAG_RULE_LABEL_UUID       = "<RULE_LABEL_UUID>";
    protected static final String TAG_USER_PART             = "<USER_PART>";
    protected static final String TAG_OPERATION             = "<OPERATION>";
    protected static final String TAG_TRIGGER_OBJECT_NAME   = "<TRIGGER_OBJECT_NAME>";
    protected static final String TAG_TARGET_OBJECT_NAME    = "<TARGET_OBJECT_NAME>";
    protected static final String TAG_SOURCE_CONTAINER      = "<SOURCE_CONTAINER>";
    protected static final String TAG_DESTINATION_CONTAINER = "<DESTINATION_CONTAINER>";

    public static EVERScript.Emitter emit() {
        return new Emitter();
    }


    public static class Emitter {

        private Emitter() {

        }

        public String moveOnWrite(PmNode targetObject, PmNode sourceContainer, PmNode destContainer) {
            return moveOnWrite(targetObject, targetObject, sourceContainer, destContainer);
        }

        public String moveOnWrite(String targetObject, String sourceContainer, String destContainer) {
            return moveOnWrite(targetObject, targetObject, sourceContainer, destContainer);
        }
        public String moveOnWrite(User byUser, PmNode triggerObject, PmNode targetObject, PmNode sourceContainer, PmNode destContainer) {
            return moveOnWrite(byUser.name().get(), triggerObject.getName(), targetObject.getName(), sourceContainer.getName(), destContainer.getName());
        }


        public String moveOnWrite(PmNode triggerObject, PmNode targetObject, PmNode sourceContainer, PmNode destContainer) {
            return moveOnWrite(triggerObject.getName(), targetObject.getName(), sourceContainer.getName(), destContainer.getName());
        }

        public String moveOnWrite(String triggerObject, String targetObject, String sourceContainer, String destContainer){
            return moveOnWrite("", triggerObject, targetObject, sourceContainer, destContainer);
        }

        public String moveOnWrite(String userName, String triggerObject, String targetObject, String sourceContainer, String destContainer) {
            String userpart = isNullOrEmpty(userName) ? "any user" : "user " + userName;
            String uuid = UUID.randomUUID().toString();
            return Joiner.on("\n").join(moveOnWrite)
                    .replaceAll(TAG_RULE_LABEL_UUID,        uuid)
                    .replaceAll(TAG_USER_PART,              userpart)
                    .replaceAll(TAG_OPERATION,              "Object write")
                    .replaceAll(TAG_TRIGGER_OBJECT_NAME,    triggerObject)
                    .replaceAll(TAG_TARGET_OBJECT_NAME,     targetObject)
                    .replaceAll(TAG_SOURCE_CONTAINER,       sourceContainer)
                    .replaceAll(TAG_DESTINATION_CONTAINER,  destContainer);

        }


        /**
         * Core text for evr move on write command
         */
        private static final String[] moveOnWrite = new String[]{
                String.format("%s:  when %s performs \"%s\"",                      TAG_RULE_LABEL_UUID, TAG_USER_PART, TAG_OPERATION),
                String.format("\ton object \"%s\" of attribute \"%s\"",            TAG_TRIGGER_OBJECT_NAME, TAG_SOURCE_CONTAINER),
                              "\tdo",
                String.format("\t\tassign object attribute \"%s\"",                TAG_TARGET_OBJECT_NAME),
                String.format("\t\tto object attribute \"%s\"",                    TAG_DESTINATION_CONTAINER),
                String.format("\t\tdelete assignment of object attribute \"%s\"",  TAG_TARGET_OBJECT_NAME),
                String.format("\t\tto object attribute \"%s\"",                    TAG_SOURCE_CONTAINER)
        };
    }
}
