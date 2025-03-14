package org.springdot.forpan.model;

import org.springdot.forpan.config.ForpanConfig;

class ModelTestBase{

    protected void setForpanHome(String method){
        ForpanConfig.setForpanHome(
            System.getProperty("buildDirectory")+
                "/"+this.getClass().getSimpleName()+
                "/"+method
        );
    }
}
