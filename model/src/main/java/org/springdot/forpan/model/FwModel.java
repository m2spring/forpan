package org.springdot.forpan.model;

import org.springdot.forpan.cpanel.api.CPanelAPI;
import org.springdot.forpan.util.Lazy;

import java.util.List;

public class FwModel{

    public static Lazy<FwModel> instance = Lazy.of(() -> new FwModel());

    private List<FwRecord> records;

    public void reload(){
        // TODO: incremental update of records

        CPanelAPI api = CPanelAPI.mkImpl();
        records = api.getDomains().stream()
            .flatMap(dmn -> api.getForwarders(dmn).stream())
            .map(cfwd -> new FwRecord(cfwd))
            .toList();
    }

    public List<FwRecord> getRecords(){
        return records;
    }
}
