package com.serhet.cdrviewer.exception;

public class CdrNotFoundException extends RuntimeException{

    // ust classtan constructor yarat

    public CdrNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public CdrNotFoundException(Throwable cause) {
        super(cause);
    }

    public CdrNotFoundException(String message) {
        super(message);
    }
}
