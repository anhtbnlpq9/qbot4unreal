package xyz.mjav.theqbot.exceptions;

public class ItemErrorException extends Exception {
    public ItemErrorException() {
        super();
    }

    public ItemErrorException(String s) {
        super(s);
    }

    public ItemErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ItemErrorException(Throwable cause) {
        super(cause);
    }

    protected ItemErrorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
