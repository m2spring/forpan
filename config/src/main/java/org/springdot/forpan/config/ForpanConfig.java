package org.springdot.forpan.config;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ForpanConfig{
    private static final Logger LOG = Logger.getLogger(ForpanConfig.class.getName());

    public final static String DISABLED_RECORD_BACKUP_PROP = "disable.record.backup";
    public final static String MAILSCAN_ACCOUNTS_PROP = "mailscan.accounts";

    private static String forpanHome = null;

    public static void setForpanHome(String d){
        forpanHome = d;
    }

    public static File getForpanHome(){
        if (!StringUtils.isBlank(forpanHome)) return new File(forpanHome);

        String home = System.getenv("FORPAN_HOME");
        return !StringUtils.isBlank(home)
               ? new File(home)
               : new File(System.getProperty("user.home"),".forpan");
    }

    public static Properties getProperties(){
        Properties props = new Properties();
        File fn = getPropertiesFile();
        if (fn.exists()){
            LOG.info("loading config from "+fn);
            try{
                props.load(new FileInputStream(fn));
            }catch (IOException e){
                throw new RuntimeException("while trying to load "+fn, e);
            }
        }
        return props;
    }

    public static File getPropertiesFile(){
        return new File(getForpanHome(),"config.properties");
    }

    public static String getForwarderInitPattern(){
        return getProperties().getProperty("forwarder.init.pattern");
    }

    public static boolean isDisabledRecordBackup(){
        return StringUtils.equals("true",getProperties().getProperty(DISABLED_RECORD_BACKUP_PROP));
    }

    /** Thunderbird account names (e.g. "mail.example.org") whose Inbox should be scanned. */
    public static List<String> getMailscanAccounts(){
        String val = getProperties().getProperty(MAILSCAN_ACCOUNTS_PROP);
        if (StringUtils.isBlank(val)) return List.of();

        return Arrays.stream(val.split(","))
            .map(String::trim)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    }
}
