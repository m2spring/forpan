package org.springdot.forpan.mailscan;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/** Locates the default Thunderbird profile directory via {@code ~/.thunderbird/profiles.ini}. */
public class ThunderbirdProfile{

    public static File findDefaultProfileDir() throws IOException{
        File thunderbirdHome = new File(System.getProperty("user.home"),".thunderbird");
        File iniFile = new File(thunderbirdHome,"profiles.ini");
        if (!iniFile.exists()){
            throw new IllegalStateException("thunderbird profiles.ini not found at "+iniFile);
        }

        Map<String,String> defaultProfileProps = null;
        Map<String,String> currentSection = null;
        boolean currentIsProfileSection = false;

        for (String rawLine : Files.readAllLines(iniFile.toPath())){
            String line = rawLine.trim();
            if (line.startsWith("[") && line.endsWith("]")){
                if (currentIsProfileSection && "1".equals(currentSection.get("Default"))){
                    defaultProfileProps = currentSection;
                }
                currentSection = new HashMap<>();
                currentIsProfileSection = line.startsWith("[Profile");
                continue;
            }
            if (currentSection == null) continue;
            int eq = line.indexOf('=');
            if (eq > 0) currentSection.put(line.substring(0,eq),line.substring(eq+1));
        }
        if (currentIsProfileSection && "1".equals(currentSection.get("Default"))){
            defaultProfileProps = currentSection;
        }

        if (defaultProfileProps == null){
            throw new IllegalStateException("no default profile (Default=1) found in "+iniFile);
        }

        String path = defaultProfileProps.get("Path");
        boolean relative = !"0".equals(defaultProfileProps.getOrDefault("IsRelative","1"));
        return relative? new File(thunderbirdHome,path) : new File(path);
    }

    public static File inboxFile(File profileDir, String account){
        return new File(profileDir,"Mail/"+account+"/Inbox");
    }
}
