package org.springdot.forpan.mailscan;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Pulls bare email addresses out of raw header values (e.g. "Name &lt;addr&gt;, addr2"). */
public class AddressExtractor{
    private static final Pattern ADDRESS = Pattern.compile("[\\w.+-]+@[\\w.-]+");

    public static Set<String> extract(String... headerValues){
        Set<String> addrs = new LinkedHashSet<>();
        for (String h : headerValues){
            if (h == null) continue;
            Matcher m = ADDRESS.matcher(h);
            while (m.find()){
                addrs.add(m.group().toLowerCase(Locale.ROOT));
            }
        }
        return addrs;
    }
}
