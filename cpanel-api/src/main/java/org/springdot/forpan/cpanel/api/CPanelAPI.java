package org.springdot.forpan.cpanel.api;

import org.springdot.forpan.cpanel.api.impl.CPanelImpl;

import java.util.List;

public interface CPanelAPI{

    boolean isConfigured();

    String ping() throws Exception;

    List<CPanelDomain> getDomains();
    List<CPanelForwarder> getForwarders(CPanelDomain domain);

    // until we do some real DI
    public static CPanelAPI mkImpl(CPanelAccessDetails ad){
        return new CPanelImpl(ad);
    }
}
