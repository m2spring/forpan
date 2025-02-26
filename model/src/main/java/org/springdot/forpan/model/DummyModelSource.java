package org.springdot.forpan.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.springdot.forpan.cpanel.api.CPanelDomain;
import org.springdot.forpan.util.Lazy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

public class DummyModelSource implements ModelSource{
    private final List<CPanelDomain> domains = Stream.of(
        "example.com",
        "example.org",
        "example.net"
    ).map(CPanelDomain::new).toList();

    private Lazy<List<FwRecord>> randomRecords = Lazy.of(() -> {
        RandomStringGenerator rsg = new RandomStringGenerator.Builder().withinRange('a','z').build();
        Random rnd = new Random(System.nanoTime());
        List<FwRecord> recs = new ArrayList<>();
        Set<String> fwrds = new HashSet<>();
        String trgt = "target@example.org";

        for (int i=0; i<20; i++){
            while (true){
                String fwdr = rsg.generate(5,10)+"@"+domains.get(rnd.nextInt(domains.size()));
                if (fwrds.add(fwdr)){
                    recs.add(new FwRecord(fwdr,trgt));
                    break;
                }
                System.out.println("hit duplicate "+fwdr); // very unlikely
            }
        }

        return recs;
    });

    private List<FwRecord> records = randomRecords.get();

    @Override
    public List<CPanelDomain> readDomains(){
        return domains;
    }

    @Override
    public List<FwRecord> readRecords(){
        return records;
    }

    @Override
    public void addForwarder(String forwarder, CPanelDomain domain, String target){
        records.add(new FwRecord(forwarder+"@"+domain,target));
    }

    @Override
    public void delForwarder(FwRecord rec){
        records.removeIf(f -> StringUtils.equals(f.forwarder,rec.forwarder));
    }
}
