package org.springdot.forpan.mailscan;

import org.junit.Assume;
import org.junit.Test;
import org.springdot.forpan.model.ForpanModel;
import org.springdot.forpan.model.FwRecord;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springdot.forpan.util.TestUtil.showMethod;

/**
 * Exercises the scanner against the real local Thunderbird profile (accounts listed
 * under config.properties' "mailscan.accounts") and the real configured forwarders.
 * Machine-specific by design (see ThunderbirdProfile).
 *
 * Deliberately avoids printing any message subject/sender/target content: only
 * counts are logged, and the only assertion made about hit content is a
 * self-consistency check (the matched address genuinely appears in that message's
 * To/Cc), so no foreknowledge of real mailbox contents is required.
 */
public class InboxScannerTest{

    @Test
    public void testScanRealInboxesForKnownForwarders() throws Exception{
        showMethod();

        List<File> inboxes = ThunderbirdProfile.configuredInboxFiles();
        Assume.assumeFalse(
            "no accounts configured in "+System.getProperty("user.home")+"/.forpan/config.properties (mailscan.accounts) to scan",
            inboxes.isEmpty()
        );
        for (File inbox : inboxes){
            assertTrue("expected inbox file at "+inbox,inbox.isFile());
        }

        ForpanModel model = new ForpanModel();
        model.load();
        Set<String> forwarders = model.getRecords().stream()
            .map(FwRecord::getForwarder)
            .filter(f -> f != null)
            .map(f -> f.toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet());
        assertFalse("no forwarders configured in "+System.getProperty("user.home")+"/.forpan/records.json to search for",forwarders.isEmpty());

        InboxScanner scanner = new InboxScanner();
        int totalHits = 0;

        for (File inbox : inboxes){
            List<ForwarderHit> hits = scanner.scan(inbox,forwarders);
            totalHits += hits.size();

            for (ForwarderHit hit : hits){
                assertTrue(
                    "matched forwarder should actually appear in the message's To or Cc header",
                    containsIgnoreCase(hit.to(),hit.matchedForwarder()) || containsIgnoreCase(hit.cc(),hit.matchedForwarder())
                );
            }
        }

        System.out.println("scanned "+inboxes.size()+" inbox(es) against "+forwarders.size()+" known forwarders, found "+totalHits+" matching message(s)");
    }

    private boolean containsIgnoreCase(String haystack, String needle){
        return haystack != null && haystack.toLowerCase(Locale.ROOT).contains(needle);
    }
}
