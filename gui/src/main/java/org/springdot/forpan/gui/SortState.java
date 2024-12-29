package org.springdot.forpan.gui;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

class SortState{
    TableColumn col;
    TableColumn.SortType type;

    private SortState(){
    }

    private SortState(TableColumn col){
        this.col = col;
        this.type = col.getSortType();
    }

    static SortState get(TableView table){
        ObservableList<TableColumn> so = table.getSortOrder();
        return (so.size() < 1)? new SortState() : new SortState(so.get(0));
    }

    void apply(TableView table){
        if (col != null){
            table.getSortOrder().add(col);
            col.setSortType(type);
            col.setSortable(true);
        }
    }
}
