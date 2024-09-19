package org.springdot.forpan.cpanel.api;

public class CPanelServerException extends RuntimeException{

    public CPanelServerException(){
        super();
    }

    public CPanelServerException(String message){
        super(message);
    }

    public CPanelServerException(String message, Throwable cause){
        super(message, cause);
    }

    public CPanelServerException(Throwable cause){
        super(cause);
    }

    protected CPanelServerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace){
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
