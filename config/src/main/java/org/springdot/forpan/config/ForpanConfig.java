package org.springdot.forpan.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ForpanConfig{

    private final static String CONFIG_PROPS_FILE = ".forpan/config.properties";

    public static Properties getProperties(){
        Properties props = new Properties();
        File fn = new File(System.getProperty("user.home"), CONFIG_PROPS_FILE);
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
