package gov.nist.csd.pm.user;

import java.util.EventListener;

public interface ProcessFinishedListener extends EventListener {
    void processFinished(Process process);
}
