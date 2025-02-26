package org.springdot.forpan.cpanel.api;

import org.apache.commons.lang3.StringUtils;
import org.springdot.forpan.config.ForpanConfig;

import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;

public class CPanelAccessDetails{

    private final static String ENDPOINT = "cpanel.endpoint";
    private final static String USER = "cpanel.user";
    private final static String PASS = "cpanel.pass";

    private Properties config;

    public CPanelAccessDetails(){
        config = ForpanConfig.getProperties();
        for (String pn : config.stringPropertyNames()){
            String val = System.getenv(pn.toUpperCase().replace('.','_'));
            if (val != null) config.put(pn,val);
        }
    }

    /**
     * For test automation.
     */
    public CPanelAccessDetails(String endpoint){
        config = new Properties();
        config.put(ENDPOINT,endpoint);
        config.put(USER,"resu");
        config.put(PASS,"sapp");
    }

    public CPanelAccessDetails setEndpoint(String ep){
        config.put(ENDPOINT,ep);
        return this;
    }

    public String getEndpoint(){
        return get(ENDPOINT);
    }

    public String getUser(){
        return get(USER);
    }

    public String getPass(){
        return get(PASS);
    }

    public boolean isConfigured(){
        return StringUtils.isNoneBlank(getEndpoint(),getUser(),getPass());
    }

    public String getStatus(){
        return getStatus(ENDPOINT,USER,PASS);
    }

    private String get(String pn){
        Object val = config.get(pn);
        return val == null? null : ""+val;
    }

    private String getStatus(String... pns){
        return Arrays.stream(pns)
            .map(pn -> {
                var val = get(pn);
                return pn+": "
                    +(StringUtils.isBlank(val)? "missing" : (PASS.equals(pn)? "*****" : val));
            })
            .collect(Collectors.joining("\n"));
    }
}
