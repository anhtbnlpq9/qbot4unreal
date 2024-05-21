package xyz.mjav.theqbot.exceptions;

public class ParseException extends Exception {
    public ParseException() {
        super();
    }

    public ParseException(String s) {
        super(s);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseException(Throwable cause) {
        super(cause);
    }

    protected ParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
