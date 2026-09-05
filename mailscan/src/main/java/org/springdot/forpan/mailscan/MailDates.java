package org.springdot.forpan.mailscan;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/** Parses RFC 2822/5322-style mail "Date" headers. */
public class MailDates{
    private static final Logger LOG = Logger.getLogger(MailDates.class.getName());
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.RFC_1123_DATE_TIME.withLocale(Locale.US);

    // Trailing zone-name comment (e.g. "+0000 (UTC)") is legal obsolete RFC 5322 syntax that
    // RFC_1123_DATE_TIME rejects outright, so strip it before parsing.
    private static final Pattern TRAILING_ZONE_COMMENT = Pattern.compile("\\s*\\([^()]*\\)\\s*$");

    /** @return the parsed instant, or null if headerValue is null or not parseable. */
    public static Instant parse(String headerValue){
        if (headerValue == null) return null;
        String value = TRAILING_ZONE_COMMENT.matcher(headerValue.trim()).replaceFirst("");
        try{
            return ZonedDateTime.parse(value,FORMAT).toInstant();
        }catch (Exception e){
            LOG.log(Level.FINE,"could not parse mail date \""+headerValue+"\": "+e.getMessage());
            return null;
        }
    }
}
