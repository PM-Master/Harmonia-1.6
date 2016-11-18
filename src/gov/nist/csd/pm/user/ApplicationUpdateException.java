package gov.nist.csd.pm.user;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 3/7/11
 * Time: 5:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationUpdateException extends Exception {
    /**
	 * @uml.property  name="_appName"
	 */
    private final String _appName;
    private static final String APP_UPDATE_EXCEPTION_FORMAT = "Could not update the application (%s)";
    public ApplicationUpdateException(String appName, Exception cause){
        super(String.format(APP_UPDATE_EXCEPTION_FORMAT, appName), cause);
        _appName = checkNotNull(appName);
    }
}
