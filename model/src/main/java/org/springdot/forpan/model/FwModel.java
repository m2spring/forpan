package org.springdot.forpan.model;

import org.springdot.forpan.cpanel.api.CPanelDomain;
import org.springdot.forpan.cpanel.api.CPanelForwarder;

import java.util.List;

public interface FwModel{
    void syncFromServer();
    List<CPanelDomain> getDomains();
    List<FwRecord> getRecords();
    void addForwarder(String forwarder, CPanelDomain domain, String target);
    void delForwarder(FwRecord rec);

    static FwModel getInstance(){
        return (System.getenv("CPANEL_ENDPOINT") != null)
            ? new RemoteModel()
            : new DummyModel();
    }
}
