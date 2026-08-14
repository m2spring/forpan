package org.springdot.forpan.mailscan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses a classic mbox file (as used by Thunderbird's local and IMAP-cache folders)
 * into per-message headers. Message bodies are skipped: only the header block up to
 * the first blank line of each message is retained.
 */
public class MboxReader{

    public List<MboxMessage> read(File mboxFile) throws IOException{
        List<MboxMessage> messages = new ArrayList<>();

        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(mboxFile),StandardCharsets.ISO_8859_1))){
            MboxMessage current = null;
            boolean inHeaders = false;
            String pendingName = null;
            StringBuilder pendingValue = null;

            String line;
            while ((line = r.readLine()) != null){
                if (isMessageBoundary(line)){
                    flushHeader(current,pendingName,pendingValue);
                    pendingName = null;
                    pendingValue = null;
                    current = new MboxMessage();
                    messages.add(current);
                    inHeaders = true;
                    continue;
                }

                if (current == null || !inHeaders) continue;

                if (line.isEmpty()){
                    flushHeader(current,pendingName,pendingValue);
                    pendingName = null;
                    pendingValue = null;
                    inHeaders = false;
                    continue;
                }

                if ((line.startsWith(" ") || line.startsWith("\t")) && pendingValue != null){
                    pendingValue.append(' ').append(line.trim());
                    continue;
                }

                flushHeader(current,pendingName,pendingValue);
                int colon = line.indexOf(':');
                if (colon > 0){
                    pendingName = line.substring(0,colon).trim();
                    pendingValue = new StringBuilder(line.substring(colon+1).trim());
                }else{
                    pendingName = null;
                    pendingValue = null;
                }
            }

            flushHeader(current,pendingName,pendingValue);
        }

        return messages;
    }

    private boolean isMessageBoundary(String line){
        return line.startsWith("From ");
    }

    private void flushHeader(MboxMessage msg, String name, StringBuilder value){
        if (msg != null && name != null){
            msg.putHeader(name,value.toString());
        }
    }
}
