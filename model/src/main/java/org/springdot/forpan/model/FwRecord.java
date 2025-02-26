package org.springdot.forpan.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springdot.forpan.cpanel.api.CPanelForwarder;

import java.util.List;

import static org.springdot.forpan.util.Util.escapeJava;

public class FwRecord{
    String forwarder;
    String target;
    List<RecordStateEntry> states;

    public FwRecord(){
    }

    FwRecord(CPanelForwarder cpf){
        this.forwarder = cpf.forwarder();
        this.target = cpf.target();
    }

    FwRecord(String forwarder, String target){
        this.forwarder = forwarder;
        this.target = target;
    }

    public String getForwarder(){
        return forwarder;
    }

    public void setForwarder(String forwarder){
        this.forwarder = forwarder;
    }

    public String getTarget(){
        return target;
    }

    public void setTarget(String target){
        this.target = target;
    }

    public List<RecordStateEntry> getStates(){
        return states;
    }

    public void setStates(List<RecordStateEntry> states){
        this.states = states;
    }

    @JsonIgnore
    public RecordState getLastState(){
        return states == null? null : states.getLast().state;
    }

    @JsonIgnore
    public String getDomain(){
        if (forwarder != null){
            int p = forwarder.lastIndexOf('@');
            if (p > -1) return forwarder.substring(p+1);
        }
        return null;
    }

    @Override
    public String toString(){
        return "new FwRecord("+escapeJava(forwarder)+","+escapeJava(target)+"),";
    }
}
