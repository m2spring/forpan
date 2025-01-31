package org.springdot.forpan.gui;

import javafx.geometry.HPos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

class FormAttr<T extends Control>{
    Form parentForm;
    Label label;
    T field;
    Label annotation;
    Consumer<FormAttr<T>> validator;
    String errorMsg;

    FormAttr(GridPane grid, String label, T field, AtomicInteger row){
        this.label = new Label(label+":");
        GridPane.setHalignment(this.label,HPos.RIGHT);
        grid.add(this.label,0,row.get());

        this.field = field;
        grid.add(field,1,row.getAndIncrement());

        this.annotation = new Label();
        this.annotation.setFont(new Font(10.0));
        grid.add(this.annotation,1,row.getAndIncrement());

        var spacer = new Region();
        spacer.setPrefHeight(5);
        grid.add(spacer,1,row.getAndIncrement());

        (switch (field){
            case TextField tfield -> tfield.textProperty();
            case ComboBox combo -> combo.valueProperty();
            default -> throw new IllegalArgumentException("not implemented field "+field);
        }).addListener((observable,ov,nv) -> {
            callValidators();
        });
    }

    private void callValidators(){
        if (parentForm != null){
            for (FormAttr attr : parentForm.attrs){
                if (attr.validator != null){
                    attr.clearAnnotation();
                    try{
                        attr.validator.accept(attr);
                    }catch (AttrValidationErrorException e){
                        attr.setError(e.getMessage());
                    }
                }
            }
            parentForm.validate();
        }
    }

    FormAttr<T> setValidator(Consumer<FormAttr<T>> validator){
        this.validator = validator;
        return this;
    }

    void clearAnnotation(){
        annotation.setText(null);
        field.setStyle(null);
        errorMsg = null;
    }

    void setError(String msg){
        annotation.setText(msg);
        annotation.setTextFill(ERROR_COLOR);
        field.setStyle(
            "-fx-border-color: "+ERROR_RGB+"; "+
            "-fx-border-width: 1px; "+
            "-fx-border-radius: 4px;"+
            "-fx-border-insets: -1px;"
        );
        errorMsg = msg;
    }

    boolean hasError(){
        return !StringUtils.isBlank(errorMsg);
    }

    private final static String ERROR_RGB = "#d32f2f";
    private final static Color ERROR_COLOR = Color.web(ERROR_RGB);
}
