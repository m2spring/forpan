package org.springdot.forpan.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * A collection for FormAttrs.
 */
class Form{
    List<FormAttr> attrs;
    Consumer<Long /*nofErrAttrs*/> validator;

    Form(FormAttr... attrs){
        this.attrs = new ArrayList<>(Arrays.asList(attrs));
        for (FormAttr attr : attrs){
            if (attr.parentForm !=null) throw new IllegalArgumentException(attr.label+" attr already in a form");
            attr.parentForm = this;
        }
    }

    Form setValidator(Consumer<Long /*nofErrAttrs*/> validator){
        this.validator = validator;
        validate();
        return this;
    }

    void validate(){
        validator.accept(attrs.stream().filter(FormAttr::hasError).count());
    }
}
