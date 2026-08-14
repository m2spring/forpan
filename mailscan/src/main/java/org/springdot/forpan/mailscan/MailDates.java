package org.springdot.forpan.mailscan;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Parses RFC 2822/5322-style mail "Date" headers. */
public class MailDates{
    private static final Logger LOG = Logger.getLogger(MailDates.class.getName());
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.RFC_1123_DATE_TIME.withLocale(Locale.US);

    /** @return the parsed instant, or null if headerValue is null or not parseable. */
    public static Instant parse(String headerValue){
        if (headerValue == null) return null;
        try{
            return ZonedDateTime.parse(headerValue.trim(),FORMAT).toInstant();
        }catch (Exception e){
            LOG.log(Level.FINE,"could not parse mail date \""+headerValue+"\": "+e.getMessage());
            return null;
        }
    }
}
