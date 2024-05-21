package xyz.mjav.theqbot.exceptions;

public class NickNotFoundException extends Exception {
    public NickNotFoundException() {
        super();
    }

    public NickNotFoundException(String s) {
        super(s);
    }

    public NickNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NickNotFoundException(Throwable cause) {
        super(cause);
    }

    protected NickNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
