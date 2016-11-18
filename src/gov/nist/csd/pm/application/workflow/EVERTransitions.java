package gov.nist.csd.pm.application.workflow;

import gov.nist.csd.pm.common.browser.PmNode;
import gov.nist.csd.pm.common.model.User;
import gov.nist.csd.pm.common.pattern.Builds;
import gov.nist.csd.pm.common.util.EVERScript;
import gov.nist.csd.pm.common.util.EVERTransition;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 8/15/11
 * Time: 6:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class EVERTransitions {





    /**
	 * @author  Administrator
	 */
    public static class UserTransitionBuilder implements Builds<EVERTransition> {
         /**
		 * @uml.property  name="utrans"
		 * @uml.associationEnd  
		 */
        UserTriggerTransition utrans = new UserTriggerTransition(null, null, null, null, null);
        public UserTransitionBuilder from(PmNode node){
            utrans = new UserTriggerTransition(utrans.user(), utrans.what(), node, utrans.to(), utrans.trigger());
            return this;
        }

        public UserTransitionBuilder to(PmNode node ){
            utrans = new UserTriggerTransition(utrans.user(), utrans.what(), utrans.from(),node, utrans.trigger());

             return this;
        }

        public UserTransitionBuilder moving(PmNode node){
            utrans = new UserTriggerTransition(utrans.user(), node, utrans.from(), utrans.to(), utrans.trigger());

            return this;
        }

        public UserTransitionBuilder triggeredOn(PmNode node){
            utrans = new UserTriggerTransition(utrans.user(), utrans.what(), utrans.from(), utrans.to(), node);
            return this;
        }

         public UserTransitionBuilder triggeredBy(User user){
            utrans = new UserTriggerTransition(user, utrans.what(), utrans.from(), utrans.to(), utrans.trigger());
            return this;
        }

         public UserTransitionBuilder from(String objectAttributeName){
            return from(PmNode.createObjectAttributeNode(objectAttributeName));
        }

        public UserTransitionBuilder to(String objectAttributeName ){
            return to(PmNode.createObjectAttributeNode(objectAttributeName));
        }

        public UserTransitionBuilder moving(String objectAttribute){
            return moving(PmNode.createObjectAttributeNode(objectAttribute));
        }

        public UserTransitionBuilder triggeredOn(String objectName){
            return triggeredOn(PmNode.createObjectNode(objectName));
        }

        @Override
        public EVERTransition build(){
            return utrans;
        }

    }



    public static UserTransitionBuilder universal(){
        return new UserTransitionBuilder().triggeredBy(User.ANY);
    }

    public static UserTransitionBuilder user(){
        return new UserTransitionBuilder();
    }


    /**
	 * @author  Administrator
	 */
    private static class UserTriggerTransition implements EVERTransition {

        /**
		 * @uml.property  name="user"
		 * @uml.associationEnd  
		 */
        private final User user;
        /**
		 * @uml.property  name="from"
		 * @uml.associationEnd  
		 */
        private final PmNode from;
        /**
		 * @uml.property  name="to"
		 * @uml.associationEnd  
		 */
        private final PmNode to;
        /**
		 * @uml.property  name="trigger"
		 * @uml.associationEnd  
		 */
        private final PmNode trigger;
        /**
		 * @uml.property  name="what"
		 * @uml.associationEnd  
		 */
        private final PmNode what;



        public UserTriggerTransition(User user, PmNode what, PmNode from, PmNode to, PmNode triggeredBy) {
            this.from = from;
            this.to = to;
            this.trigger = triggeredBy;
            this.what = what;
            this.user = user;
        }

        public User user(){
            return user;
        }

          public PmNode from() {
            return from;
        }

        public PmNode to() {
            return to;
        }

        public PmNode trigger() {
            return trigger;
        }

        public PmNode what(){
            return what;
        }

        String TEMPLATE = "%s should not be null";


        public void checkNulls(){
            checkNotNull(trigger(), TEMPLATE, "trigger");
            checkNotNull(what(), TEMPLATE, "target(what)");
            checkNotNull(from(), TEMPLATE, "from(source)");
            checkNotNull(to(), TEMPLATE, "to(destination)");
        }

        @Override
        public String emit(){

            checkNulls();
            //A null user implies that any user can trigger this action.
            User checkedUser = user() != null ? user() : User.ANY;
            return EVERScript.emit().moveOnWrite(checkedUser, trigger(), what(), from(), to());
        }





    }

}
