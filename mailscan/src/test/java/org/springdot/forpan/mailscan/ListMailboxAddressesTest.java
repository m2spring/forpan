package org.springdot.forpan.mailscan;

import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.springdot.forpan.util.TestUtil.showMethod;

/**
 * Not a correctness test - a runnable entry point (invoke via list-addresses.sh) that
 * lists the distinct To/Cc email addresses found across one or more mbox files, most
 * recently seen first, given via the "mailbox.paths" system property (comma-separated
 * file paths).
 */
public class ListMailboxAddressesTest{

    @Test
    public void testListAddresses() throws Exception{
        showMethod();

        String pathsProp = System.getProperty("mailbox.paths");
        Assume.assumeTrue(
            "no mailbox.paths system property given - run this via list-addresses.sh",
            pathsProp != null && !pathsProp.isBlank()
        );

        List<File> mboxFiles = Arrays.stream(pathsProp.split(","))
            .map(String::trim)
            .map(File::new)
            .toList();

        List<String> addresses = new MailboxAddressLister().listByRecency(mboxFiles);

        System.out.println("found "+addresses.size()+" distinct address(es), most recent first:");
        addresses.forEach(a -> System.out.println("  "+a));
    }
}
