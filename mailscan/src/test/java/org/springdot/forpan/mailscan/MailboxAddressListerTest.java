package org.springdot.forpan.mailscan;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.springdot.forpan.util.TestUtil.showMethod;

public class MailboxAddressListerTest{

    @Test
    public void testOrdersByMostRecentOccurrenceAndDedups() throws Exception{
        showMethod();

        // apple appears twice: an old occurrence, then a newer one (after banana's message).
        // If the lister kept the *first*-seen date instead of the most recent one, apple
        // would incorrectly sort below banana.
        String mbox =
            "From x@example.com Sat Jan 10 08:00:00 2026\n" +
            "Date: Sat, 10 Jan 2026 08:00:00 +0000\n" +
            "To: apple@example.com\n" +
            "\n" +
            "body\n" +
            "From x@example.com Sun Jan 11 08:00:00 2026\n" +
            "Date: Sun, 11 Jan 2026 08:00:00 +0000\n" +
            "To: banana@example.com\n" +
            "\n" +
            "body\n" +
            "From x@example.com Mon Jan 12 08:00:00 2026\n" +
            "Date: Mon, 12 Jan 2026 08:00:00 +0000\n" +
            "To: apple@example.com\n" +
            "\n" +
            "body\n" +
            "From x@example.com Tue Jan 13 08:00:00 2026\n" +
            "Date: Tue, 13 Jan 2026 08:00:00 +0000\n" +
            "To: cherry@example.com\n" +
            "\n" +
            "body\n";

        File f = File.createTempFile("mailboxaddresslistertest",".mbox");
        f.deleteOnExit();
        Files.writeString(f.toPath(),mbox,StandardCharsets.ISO_8859_1);

        List<String> addresses = new MailboxAddressLister().listByRecency(List.of(f));

        assertEquals(List.of("cherry@example.com","apple@example.com","banana@example.com"),addresses);
    }

    @Test
    public void testSkipsDeletedMessages() throws Exception{
        showMethod();

        // dahlia's only occurrence is marked deleted (X-Mozilla-Status bit 0x0008) and must
        // not appear in the result, even though it is the most recent message in the file.
        String mbox =
            "From x@example.com Mon Jan 12 08:00:00 2026\n" +
            "Date: Mon, 12 Jan 2026 08:00:00 +0000\n" +
            "To: apple@example.com\n" +
            "\n" +
            "body\n" +
            "From x@example.com Tue Jan 13 08:00:00 2026\n" +
            "Date: Tue, 13 Jan 2026 08:00:00 +0000\n" +
            "X-Mozilla-Status: 0009\n" +
            "To: dahlia@example.com\n" +
            "\n" +
            "body\n";

        File f = File.createTempFile("mailboxaddresslistertest",".mbox");
        f.deleteOnExit();
        Files.writeString(f.toPath(),mbox,StandardCharsets.ISO_8859_1);

        List<String> addresses = new MailboxAddressLister().listByRecency(List.of(f));

        assertEquals(List.of("apple@example.com"),addresses);
    }
}
