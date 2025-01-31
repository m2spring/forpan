package org.springdot.forpan.gui;

public class AttrValidationErrorException extends RuntimeException{

    public AttrValidationErrorException(){
    }

    public AttrValidationErrorException(String message){
        super(message);
    }

    public AttrValidationErrorException(String message, Throwable cause){
        super(message, cause);
    }

    public AttrValidationErrorException(Throwable cause){
        super(cause);
    }

    public AttrValidationErrorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace){
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
