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

    /** True if Thunderbird's X-Mozilla-Status marks this message deleted (pending compaction). */
    public boolean isDeleted(){
        String status = getHeader("X-Mozilla-Status");
        if (status == null) return false;
        try{
            return (Integer.parseInt(status.trim(),16) & 0x0008) != 0;
        }catch (NumberFormatException e){
            return false;
        }
    }
}
