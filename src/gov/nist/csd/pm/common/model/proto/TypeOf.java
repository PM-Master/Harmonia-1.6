package gov.nist.csd.pm.common.model.proto;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 8/9/11
 * Time: 6:13 PM
 * To change this template use File | Settings | File Templates.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TypeOf {
    Class<?> value();
}
