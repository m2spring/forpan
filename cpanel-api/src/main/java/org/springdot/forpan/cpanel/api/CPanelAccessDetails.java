package org.springdot.forpan.cpanel.api;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CPanelAccessDetails{


    private final static String ENDPOINT = "CPANEL_ENDPOINT";
    private final static String USER = "CPANEL_USER";
    private final static String PASS = "CPANEL_PASS";

    private final static String[] VARNAMES = {ENDPOINT,USER,PASS};

    private Map<String,String> values = new HashMap<>();

    public CPanelAccessDetails(){
        for (String vn : VARNAMES){
            values.put(vn,System.getenv(vn));
        }
    }

    public CPanelAccessDetails setEndpoint(String ep){
        values.put(ENDPOINT,ep);
        return this;
    }

    public String getEndpoint(){
        return values.get(ENDPOINT);
    }

    public String getUser(){
        return values.get(USER);
    }

    public String getPass(){
        return values.get(PASS);
    }

    public boolean isConfigured(){
        return StringUtils.isNoneBlank(getEndpoint(),getUser(),getPass());
    }

    public String getStatus(){
        return getStatus(ENDPOINT,USER,PASS);
    }

    private static String getStatus(String... vns){
        return Arrays.stream(vns)
            .map(vn -> {
                var val = System.getenv(vn);
                return vn+": "
                    +(StringUtils.isBlank(val)? "missing" : (PASS.equals(vn)? "*****" : val));
            })
            .collect(Collectors.joining("\n"));
    }
}
