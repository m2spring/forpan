package org.springdot.forpan.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springdot.forpan.cpanel.api.CPanelAPI;
import org.springdot.forpan.cpanel.api.CPanelAccessDetails;
import org.springdot.forpan.cpanel.api.CPanelDomain;
import org.springdot.forpan.cpanel.api.CPanelForwarder;
import org.springdot.forpan.util.Lazy;

import java.util.List;

public class RemoteModelSource implements ModelSource{
    private CPanelAPI api;

    public RemoteModelSource(){
        this(new CPanelAccessDetails());
    }

    public RemoteModelSource(CPanelAccessDetails ad){
        if (!ad.isConfigured()) throw new RuntimeException("CPanelAccessDetails not configured\n"+ad.getStatus());
        this.api = CPanelAPI.mkImpl(ad);
    }

    @Override
    public List<FwRecord> readRecords(){
        return domains.get().stream()
            .flatMap(domain -> api.getForwarders(domain).stream())
            .map(FwRecord::new)
            .toList();
    }

    @JsonIgnore
    @Override
    public List<CPanelDomain> readDomains(){
        return domains.get();
    }

    @Override
    public void createForwarder(FwRecord rec){
        api.addForwarder(new CPanelForwarder(
            rec.getForwarder(),
            rec.getTarget()
        ));
    }

    @Override
    public void removeForwarder(FwRecord rec){
        api.delForwarder(new CPanelForwarder(rec.forwarder,rec.target));
    }

    public Lazy<List<CPanelDomain>> domains = Lazy.of(() -> api.getDomains());
}
