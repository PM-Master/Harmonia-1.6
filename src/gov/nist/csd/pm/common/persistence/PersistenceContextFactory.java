/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.nist.csd.pm.common.persistence;

import gov.nist.csd.pm.common.application.SysCaller;

/**
 * @author  Administrator
 */
public enum PersistenceContextFactory {

    /**
	 * @uml.property  name="iNSTANCE"
	 * @uml.associationEnd  
	 */
    INSTANCE;

    public PersistenceContext getContextForSysCaller(SysCaller sysCaller){
        return new PolicyMachinePersistenceContext(sysCaller);
    }


}
