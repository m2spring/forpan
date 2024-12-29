package org.springdot.forpan.model;

import org.springdot.forpan.cpanel.api.CPanelAPI;
import org.springdot.forpan.cpanel.api.CPanelAccessDetails;
import org.springdot.forpan.cpanel.api.CPanelDomain;
import org.springdot.forpan.cpanel.api.CPanelForwarder;
import org.springdot.forpan.util.Lazy;

import java.util.List;

class RemoteModel implements FwModel{
    private List<FwRecord> records;

    @Override
    public void syncFromServer(){
        records = domains.get().stream()
            .flatMap(domain -> api.get().getForwarders(domain).stream())
            .map(FwRecord::new)
            .toList();
    }

    @Override
    public List<CPanelDomain> getDomains(){
        return domains.get();
    }

    @Override
    public List<FwRecord> getRecords(){
        return records;
    }

    @Override
    public void addForwarder(String forwarder, CPanelDomain domain, String target){
        api.get().addForwarder(new CPanelForwarder(forwarder+"@"+domain,target));
    }

    @Override
    public void delForwarder(FwRecord rec){
        api.get().delForwarder(new CPanelForwarder(rec.forwarder,rec.target));
    }

    public Lazy<CPanelAPI> api = Lazy.of(() -> {
        CPanelAccessDetails ad = new CPanelAccessDetails();
        if (!ad.isConfigured()) throw new RuntimeException("CPanelAccessDetails not configured\n"+ad.getStatus());
        return CPanelAPI.mkImpl(ad);
    });

    public Lazy<List<CPanelDomain>> domains = Lazy.of(() -> api.get().getDomains());
}
