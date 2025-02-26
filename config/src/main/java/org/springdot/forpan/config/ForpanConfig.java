package org.springdot.forpan.config;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ForpanConfig{

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
        File fn = new File(getForpanHome(),"config.properties");
        if (fn.exists()){
            System.out.println("loading config from "+fn);
            try{
                props.load(new FileInputStream(fn));
            }catch (IOException e){
                throw new RuntimeException("while trying to load "+fn, e);
            }
        }
        return props;
    }

    public static String getForwarderInitPattern(){
        return getProperties().getProperty("forwarder.init.pattern");
    }
}
