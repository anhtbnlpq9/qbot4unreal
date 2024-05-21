package xyz.mjav.theqbot.exceptions;

public class UserAuthCredException extends Exception {
    public UserAuthCredException() {
        super();
    }

    public UserAuthCredException(String s) {
        super(s);
    }

    public UserAuthCredException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserAuthCredException(Throwable cause) {
        super(cause);
    }

    protected UserAuthCredException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
