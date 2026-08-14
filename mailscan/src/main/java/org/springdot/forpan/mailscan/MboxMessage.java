package org.springdot.forpan.mailscan;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MboxMessage{
    private final Map<String,String> headers = new HashMap<>();

    void putHeader(String name, String value){
        headers.put(name.toLowerCase(Locale.ROOT),value);
    }

    public String getHeader(String name){
        return headers.get(name.toLowerCase(Locale.ROOT));
    }

    public String getFrom(){
        return getHeader("From");
    }

    public String getTo(){
        return getHeader("To");
    }

    public String getCc(){
        return getHeader("Cc");
    }

    public String getSubject(){
        return getHeader("Subject");
    }

    public String getDate(){
        return getHeader("Date");
    }
}
