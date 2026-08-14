package org.springdot.forpan.mailscan;

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
 * Exercises the scanner against the real local Thunderbird profile and the real
 * configured forwarders. Machine-specific by design (see ThunderbirdProfile) - this
 * is the "concrete implementation" step before the account/inbox choice is wired
 * into forpan's config file.
 *
 * Deliberately avoids printing any message subject/sender/target content: only
 * counts are logged, and the only assertion made about hit content is a
 * self-consistency check (the matched address genuinely appears in that message's
 * To/Cc), so no foreknowledge of real mailbox contents is required.
 */
public class InboxScannerTest{
    private static final String ACCOUNT = "mail.springdot-2.org";

    @Test
    public void testScanRealInboxForKnownForwarders() throws Exception{
        showMethod();

        File profileDir = ThunderbirdProfile.findDefaultProfileDir();
        File inbox = ThunderbirdProfile.inboxFile(profileDir,ACCOUNT);
        assertTrue("expected inbox file at "+inbox,inbox.isFile());

        ForpanModel model = new ForpanModel();
        model.load();
        Set<String> forwarders = model.getRecords().stream()
            .map(FwRecord::getForwarder)
            .filter(f -> f != null)
            .map(f -> f.toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet());
        assertFalse("no forwarders configured in "+System.getProperty("user.home")+"/.forpan/records.json to search for",forwarders.isEmpty());

        List<ForwarderHit> hits = new InboxScanner().scan(inbox,forwarders);

        System.out.println("scanned "+ACCOUNT+"/Inbox against "+forwarders.size()+" known forwarders, found "+hits.size()+" matching message(s)");

        for (ForwarderHit hit : hits){
            assertTrue(
                "matched forwarder should actually appear in the message's To or Cc header",
                containsIgnoreCase(hit.to(),hit.matchedForwarder()) || containsIgnoreCase(hit.cc(),hit.matchedForwarder())
            );
        }
    }

    private boolean containsIgnoreCase(String haystack, String needle){
        return haystack != null && haystack.toLowerCase(Locale.ROOT).contains(needle);
    }
}
