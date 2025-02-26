package org.springdot.forpan.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springdot.forpan.config.Formats;
import org.springdot.forpan.config.ForpanConfig;
import org.springdot.forpan.cpanel.api.CPanelDomain;
import org.springdot.forpan.util.Lazy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase;
import static org.springdot.forpan.model.RecordState.CHANGED;
import static org.springdot.forpan.model.RecordState.COMMISSIONED;
import static org.springdot.forpan.model.RecordState.DECOMMISSIONED;

public class ForpanModel{
    private final static String RECORDS_FILENAME = "records.json";

    private ModelSource modelSource;
    private List<CPanelDomain> domains;
    private List<FwRecord> records;

    public ForpanModel(){
    }

    public ForpanModel(ModelSource modelSource){
        this.modelSource = modelSource;
    }

    @JsonIgnore
    public List<CPanelDomain> getDomains(){
        return domains;
    }

    public List<FwRecord> getRecords(){
        return records;
    }

    public void setRecords(List<FwRecord> records){
        this.records = records;
    }

    public void addForwarder(String forwarder, CPanelDomain domain, String target){
        modelSource.addForwarder(forwarder,domain,target);
    }

    public void delForwarder(FwRecord rec){
        modelSource.delForwarder(rec);
    }

    public void load(){
        File f = mkFilename("");
        if (f.exists()){
            try{
                ForpanModel fm = mapper.get().readValue(f,ForpanModel.class);
                domains = fm.domains;
                records = fm.records;
            }catch (IOException e){
                throw new RuntimeException(e);
            }
        }
    }

    public void save(){
        File tmpF = mkFilename(".");
        tmpF.getParentFile().mkdirs();
        try{
            mapper.get().writeValue(tmpF,this);
            File newF = mkFilename("");
            if (newF.exists()){ // TODO: saving old record.json files should be configurable?
                FileTime lastModifiedTime = Files.getLastModifiedTime(newF.toPath());
                String ts = Formats.FORMAT_YMDHMSM.format(new Date(lastModifiedTime.toMillis()));
                Files.move(newF.toPath(),mkFilename("."+ts+".").toPath(),ATOMIC_MOVE,REPLACE_EXISTING);
            }

            Files.move(tmpF.toPath(),newF.toPath(),ATOMIC_MOVE,REPLACE_EXISTING);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private static File mkFilename(String prefix){
        return new File(ForpanConfig.getForpanHome(),prefix+"records.json");
    }

    public Date syncFromServer(){
        domains = modelSource.readDomains();

        Date syncTime = new Date();

        Map<String,FwRecord> oldRecs = mkRecMap(records);
        Map<String,FwRecord> newRecs = mkRecMap(modelSource.readRecords());

        List<FwRecord> nrecords = new ArrayList<>();
        if (records != null) nrecords.addAll(records);

        for (var newRec : newRecs.values()){
            var oldRec = oldRecs.get(newRec.getForwarder());
            if (oldRec == null){
                nrecords.add(newRec);
                appendState(newRec,syncTime,COMMISSIONED);
            }else if (!equalsAnyIgnoreCase(oldRec.target,newRec.target)){
                appendState(oldRec,syncTime,CHANGED).details = oldRec.target+" -> "+newRec.target;
                oldRec.target = newRec.target;
            }else{
                appendState(oldRec,syncTime,COMMISSIONED);
            }
            // TODO: log the changes
        }

        for (var oldRec : oldRecs.values()){
            var newRec = newRecs.get(oldRec.getForwarder());
            if (newRec == null){
                appendState(oldRec,syncTime,DECOMMISSIONED);
            }
        }

        records = nrecords;

        save();

        return syncTime;
    }

    private static RecordStateEntry appendState(FwRecord rec, Date time, RecordState state){
        if (rec.states == null){
            rec.states = new ArrayList<>();
        }else if (state != CHANGED){
            int size = rec.states.size();
            if (size > 1 && rec.states.get(size-1).state == state && rec.states.get(size-2).state == state){
                // we only want to keep the first and last entry of a sequence of same states
                RecordStateEntry rse = rec.states.getLast();
                rse.time = time;
                return rse;
            }
        }

        RecordStateEntry rse = new RecordStateEntry(time,state);
        rec.states.add(rse);
        return rse;
    }

    private static Map<String,FwRecord> mkRecMap(List<FwRecord> recs){
        return recs == null
            ? Collections.EMPTY_MAP
            : recs.stream().collect(Collectors.toMap(
                FwRecord::getForwarder,
                Function.identity(),
                (first, second) -> first
              ));
    }

    private static Lazy<ObjectMapper> mapper = Lazy.of(() ->
        new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    );

    public boolean containsForwarder(String forwarder){
        return findForwarder(forwarder) != null;
    }

    public FwRecord findForwarder(String forwarder){
        for (FwRecord rec : records){
            if (equalsAnyIgnoreCase(forwarder,rec.getForwarder())) return rec;
        }
        return null;
    }
}
