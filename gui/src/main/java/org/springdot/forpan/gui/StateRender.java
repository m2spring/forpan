package org.springdot.forpan.gui;

import org.springdot.forpan.model.RecordState;

record StateRender(
    String icon,
    String color,
    String tooltip){

    static StateRender get(RecordState rs){
        return switch (rs){
            case COMMISSIONED -> new StateRender("far-eye","#28a745",rs.toString());
            case DECOMMISSIONED -> new StateRender("far-eye-slash","#6c757d",rs.toString());
            default -> new StateRender("fas-question","#ff0000","unknown RecordState: "+rs);
        };
    }
}
