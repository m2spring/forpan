package org.springdot.forpan.gui;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springdot.forpan.config.ForpanConfig;
import org.springdot.forpan.util.Lazy;

import java.io.File;
import java.io.IOException;

/**
 * Persist UI state across invocation.
 */
public class Memo{

    public String domain;
    public String target;

    static Memo load(){
        File f = getMemoFile();
        if (f.exists()){
            try{
                return mapper.get().readValue(f,Memo.class);
            }catch (IOException e){
                throw new RuntimeException(e.getMessage()+" while reading "+f,e);
            }
        }
        return new Memo();
    }

    void save(){
        File f = getMemoFile();
        f.getParentFile().mkdirs();
        try{
            mapper.get().writeValue(f,this);
        }catch (IOException e){
            throw new RuntimeException(e.getMessage()+" while writing "+f,e);
        }
    }

    private static File getMemoFile(){
        return new File(ForpanConfig.getForpanHome(),"memo.json");
    }

    private static Lazy<ObjectMapper> mapper = Lazy.of(() ->
        new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false)
    );
}
