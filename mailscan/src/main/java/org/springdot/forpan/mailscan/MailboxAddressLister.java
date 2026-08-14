package org.springdot.forpan.mailscan;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Lists the distinct To/Cc addresses found across one or more mbox files, most recently seen first. */
public class MailboxAddressLister{

    private final MboxReader mboxReader = new MboxReader();

    public List<String> listByRecency(Collection<File> mboxFiles) throws IOException{
        Map<String,Instant> latestByAddress = new HashMap<>();

        for (File mboxFile : mboxFiles){
            for (MboxMessage msg : mboxReader.read(mboxFile)){
                Instant date = MailDates.parse(msg.getDate());
                Instant effectiveDate = date != null? date : Instant.MIN;
                for (String addr : AddressExtractor.extract(msg.getTo(),msg.getCc())){
                    latestByAddress.merge(addr,effectiveDate,(a,b) -> a.isAfter(b)? a : b);
                }
            }
        }

        return latestByAddress.entrySet().stream()
            .sorted(Map.Entry.<String,Instant>comparingByValue().reversed())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
}
