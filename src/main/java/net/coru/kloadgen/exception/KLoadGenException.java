package net.coru.kloadgen.exception;

public class KLoadGenException extends RuntimeException {

    public KLoadGenException(String message) {
        super(message);
    }

    public KLoadGenException(Exception exc) {
        super(exc);
    }

    public KLoadGenException(String message, Exception exc) {
        super(message, exc);
    }
}