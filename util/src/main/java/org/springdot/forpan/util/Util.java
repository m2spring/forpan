package org.springdot.forpan.util;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

public class Util{

    public static String getenv(String name, String defaultVal){
        var val = System.getenv(name);
        return StringUtils.isBlank(val)? defaultVal : val;
    }

    public static String escapeJava(String s){
        return (s == null)? "null" : "\""+StringEscapeUtils.escapeJava(s)+"\"";
    }
}
