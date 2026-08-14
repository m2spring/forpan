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
 * recently seen first.
 *
 * Prints real addresses, so it requires an explicit opt-in via the CONFIRM_LIST_ADDRESSES
 * environment variable (set to anything non-blank) - without it, the test is skipped
 * rather than run, so it never fires as a side effect of an unrelated "mvn test".
 *
 * Mailboxes come from the "mailbox.paths" system property (comma-separated file paths)
 * if given, otherwise from the accounts configured under config.properties'
 * "mailscan.accounts".
 */
public class ListMailboxAddressesTest{
    private static final String CONFIRM_ENV_VAR = "CONFIRM_LIST_ADDRESSES";

    @Test
    public void testListAddresses() throws Exception{
        showMethod();

        String confirm = System.getenv(CONFIRM_ENV_VAR);
        Assume.assumeTrue(
            "set "+CONFIRM_ENV_VAR+" to confirm you want real email addresses printed - e.g. "+
                CONFIRM_ENV_VAR+"=1 ./list-addresses.sh ...",
            confirm != null && !confirm.isBlank()
        );

        List<File> mboxFiles = resolveMboxFiles();
        Assume.assumeFalse(
            "no mailbox.paths system property given, and no mailscan.accounts configured in "+
                System.getProperty("user.home")+"/.forpan/config.properties - run this via list-addresses.sh",
            mboxFiles.isEmpty()
        );

        List<String> addresses = new MailboxAddressLister().listByRecency(mboxFiles);

        System.out.println("found "+addresses.size()+" distinct address(es), most recent first:");
        addresses.forEach(a -> System.out.println("  "+a));
    }

    private List<File> resolveMboxFiles() throws Exception{
        String pathsProp = System.getProperty("mailbox.paths");
        if (pathsProp != null && !pathsProp.isBlank()){
            return Arrays.stream(pathsProp.split(","))
                .map(String::trim)
                .map(File::new)
                .toList();
        }
        return ThunderbirdProfile.configuredInboxFiles();
    }
}
