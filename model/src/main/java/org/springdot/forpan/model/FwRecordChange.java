package org.springdot.forpan.model;

import org.apache.commons.lang3.StringUtils;
import org.springdot.forpan.util.Util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FwRecordChange{
    private List<String> diff = new ArrayList<>();
    private boolean coreAttrChanged; // if true, requires an update on the server side

    public static FwRecordChange create(FwRecord oldRec, FwRecord newRec){
        FwRecordChange rc = new FwRecordChange();

        for (Field f : FwRecord.class.getDeclaredFields()){
            if (f.getType() == String.class){
                String oldVal = normalize(f,oldRec);
                String newVal = normalize(f,newRec);
                if (!StringUtils.equals(oldVal,newVal)){
                    rc.diff.add(f.getName()+": "+Util.escapeJava(oldVal)+" -> "+Util.escapeJava(newVal));
                }
            }
        }

        rc.coreAttrChanged =  !StringUtils.equals(oldRec.getForwarder(),newRec.getForwarder())
                           || !StringUtils.equals(oldRec.getTarget(),newRec.getTarget());

        return rc;
    }

    private static String normalize(Field f, FwRecord rec){
        try{
            Object o = f.get(rec);
            return o == null? "" : (String)o;
        }catch (IllegalAccessException e){
            throw new RuntimeException("while accessing field "+f.getName(),e);
        }
    }

    public boolean hasChange(){
        return diff.size() > 0;
    }

    public boolean hasCoreAttrChange(){
        return coreAttrChanged;
    }

    public String getDetails(){
        return diff.stream().collect(Collectors.joining("\n"));
    }
}
