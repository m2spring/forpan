package org.springdot.forpan.mailscan;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.springdot.forpan.util.TestUtil.showMethod;

public class MboxReaderTest{

    @Test
    public void testParsesHeadersAndFoldedLines() throws Exception{
        showMethod();

        String mbox =
            "From alice@example.com Wed Jan 14 08:00:00 2026\n" +
            "From: Alice <alice@example.com>\n" +
            "To: forwarder@springdot.org,\n" +
            " Bob <bob@example.com>\n" +
            "Cc: carol@example.com\n" +
            "Subject: hello\n" +
            "\n" +
            "body line 1\n" +
            ">From this is an escaped body line, not a boundary\n" +
            "body line 2\n" +
            "\n" +
            "From dave@example.com Thu Jan 15 09:00:00 2026\n" +
            "From: Dave <dave@example.com>\n" +
            "To: someone.else@example.com\n" +
            "Subject: second message\n" +
            "\n" +
            "second body\n";

        File f = File.createTempFile("mboxreadertest",".mbox");
        f.deleteOnExit();
        Files.writeString(f.toPath(),mbox,StandardCharsets.ISO_8859_1);

        List<MboxMessage> messages = new MboxReader().read(f);

        assertEquals(2,messages.size());

        MboxMessage first = messages.get(0);
        assertEquals("Alice <alice@example.com>",first.getFrom());
        assertEquals("forwarder@springdot.org, Bob <bob@example.com>",first.getTo());
        assertEquals("carol@example.com",first.getCc());
        assertEquals("hello",first.getSubject());

        MboxMessage second = messages.get(1);
        assertEquals("Dave <dave@example.com>",second.getFrom());
        assertEquals("someone.else@example.com",second.getTo());
        assertEquals("second message",second.getSubject());
    }
}
