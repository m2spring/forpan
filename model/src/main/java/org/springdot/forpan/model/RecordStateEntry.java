package org.springdot.forpan.model;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

import static org.springdot.forpan.config.Formats.FORMAT_YMDHMSM;

public class RecordStateEntry{
    public Date time;
    public RecordState state;
    public String details;

    public RecordStateEntry(){
    }

    public RecordStateEntry(Date time, RecordState state){
        this.time = time;
        this.state = state;
    }

    @Override
    public String toString(){
        String s = (time == null ? null : FORMAT_YMDHMSM.format(time))+" "+state;
        if (!StringUtils.isBlank(details)) s += " "+details;
        return s;
    }
}
