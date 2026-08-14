package org.springdot.forpan.mailscan;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/** Scans an mbox inbox for messages addressed (To/Cc) to one of a given set of forwarder addresses. */
public class InboxScanner{

    private final MboxReader mboxReader = new MboxReader();

    public List<ForwarderHit> scan(File mboxFile, Collection<String> forwarderAddresses) throws IOException{
        Set<String> wanted = forwarderAddresses.stream()
            .map(a -> a.toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet());

        List<ForwarderHit> hits = new ArrayList<>();
        for (MboxMessage msg : mboxReader.read(mboxFile)){
            String to = msg.getTo();
            String cc = msg.getCc();
            for (String addr : AddressExtractor.extract(to,cc)){
                if (wanted.contains(addr)){
                    hits.add(new ForwarderHit(addr,msg.getFrom(),to,cc,msg.getSubject(),msg.getDate()));
                    break;
                }
            }
        }
        return hits;
    }
}
