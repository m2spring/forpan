package org.springdot.forpan.mailscan;

/** A message whose To/Cc header contained one of the scanned-for forwarder addresses. */
public record ForwarderHit(String matchedForwarder, String from, String to, String cc, String subject, String date){
}
