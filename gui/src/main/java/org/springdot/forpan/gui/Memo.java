package org.springdot.forpan.gui;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springdot.forpan.util.Lazy;

import java.io.File;
import java.io.IOException;

/**
 * Persist UI state across invocation.
 */
public class Memo{

    private final static File MEMO_FILE = new File(System.getProperty("user.home"),".forpan/memo.json");

    public String domain;
    public String target;

    static Memo load(){
        if (MEMO_FILE.exists()){
            try{
                return mapper.get().readValue(MEMO_FILE,Memo.class);
            }catch (IOException e){
                throw new RuntimeException(e.getMessage()+" while reading "+MEMO_FILE,e);
            }
        }
        return new Memo();
    }

    void save(){
        MEMO_FILE.getParentFile().mkdirs();
        try{
            mapper.get().writeValue(MEMO_FILE,this);
        }catch (IOException e){
            throw new RuntimeException(e.getMessage()+" while writing "+MEMO_FILE,e);
        }
    }

    private static Lazy<ObjectMapper> mapper = Lazy.of(() ->
        new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false)
    );
}
