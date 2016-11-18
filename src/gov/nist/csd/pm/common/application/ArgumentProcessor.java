package gov.nist.csd.pm.common.application;

/**
* Created by IntelliJ IDEA.
* User: Administrator
* Date: 8/20/11
* Time: 4:20 PM
* To change this template use File | Settings | File Templates.
*/
public interface ArgumentProcessor {
    public boolean matches(String[] args, int position);

    public void process(String[] args, int position);

    public boolean processed();

    public ArgumentProcessors.ArgumentValue value();
}
