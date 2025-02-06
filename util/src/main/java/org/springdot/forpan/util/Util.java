package org.springdot.forpan.util;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;

public class Util{

    public static String getenv(String name, String defaultVal){
        var val = System.getenv(name);
        return StringUtils.isBlank(val)? defaultVal : val;
    }

    public static String escapeJava(String s){
        return (s == null)? "null" : "\""+StringEscapeUtils.escapeJava(s)+"\"";
    }

    public static void callIfIntPropertyIsSet(String propertyName, Consumer<Integer> consumer){
        Object prop = System.getProperty(propertyName);
        if (prop != null){
            try{
                int val = Integer.parseInt(""+prop);
                consumer.accept(val);
            }catch (NumberFormatException e){
                throw new RuntimeException("while parsing property "+escapeJava(propertyName)+": "+e.getMessage(),e);
            }
        }
    }
}
