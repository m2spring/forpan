package org.springdot.forpan.model;

import org.apache.commons.lang3.StringUtils;
import org.springdot.forpan.cpanel.api.CPanelDomain;

import java.util.List;

public interface FwModel{
    void syncFromServer();
    List<CPanelDomain> getDomains();
    List<FwRecord> getRecords();
    void addForwarder(String forwarder, CPanelDomain domain, String target);
    void delForwarder(FwRecord rec);

    static FwModel getInstance(){
        return (StringUtils.equals("true",System.getenv("FORPAN_DUMMY_MODEL")))
               ? new DummyModel()
               : new RemoteModel();
    }
}
