package org.springdot.forpan.model;

import org.apache.commons.lang3.StringUtils;
import org.springdot.forpan.cpanel.api.CPanelDomain;

import java.util.List;

public interface ModelSource{
    List<CPanelDomain> readDomains();
    List<FwRecord> readRecords();
    void addForwarder(String forwarder, CPanelDomain domain, String target);
    void delForwarder(FwRecord rec);

    static ModelSource getInstance(){
        return (StringUtils.equals("true",System.getenv("FORPAN_DUMMY_MODEL")))
               ? new DummyModelSource()
               : new RemoteModelSource();
    }
}
