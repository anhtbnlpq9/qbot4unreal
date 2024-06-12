package xyz.mjav.theqbot.exceptions;

public class BeiNotFoundException extends ItemNotFoundException {
    public BeiNotFoundException() {
        super();
    }

    public BeiNotFoundException(String string) {
        super(string);
    }

    public BeiNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeiNotFoundException(Throwable cause) {
        super(cause);
    }

    protected BeiNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
