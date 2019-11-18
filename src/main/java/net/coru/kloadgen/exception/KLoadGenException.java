package net.coru.kloadgen.exception;

public class KLoadGenException extends Exception {

    public KLoadGenException(String message) {
        super(message);
    }

    public KLoadGenException(Exception exc) {
        super(exc);
    }

}