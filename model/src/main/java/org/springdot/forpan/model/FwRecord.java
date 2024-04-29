package org.springdot.forpan.model;

import org.springdot.forpan.cpanel.api.CPanelForwarder;

public class FwRecord{
    String forwarder;
    String target;

    FwRecord(CPanelForwarder cpf){
        this.forwarder = cpf.forwarder();
        this.target = cpf.target();
    }

    public String getForwarder(){
        return forwarder;
    }

    public String getTarget(){
        return target;
    }

    public String getDomain(){
        if (forwarder != null){
            int p = forwarder.lastIndexOf('@');
            if (p > -1) return forwarder.substring(p+1);
        }
        return null;
    }
}
