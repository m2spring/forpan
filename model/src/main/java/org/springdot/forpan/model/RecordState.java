package org.springdot.forpan.model;

public enum RecordState{

    COMMISSIONED,   // forwarder does exist in cPanel

    DECOMMISSIONED, // forwarder does NOT exist in cPanel

    CHANGED         // forwarder has changed target
                    // (if forwarder string itself had changed, it would be a different record)
}
