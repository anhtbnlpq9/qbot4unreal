package xyz.mjav.theqbot.exceptions;

public class ItemExistsException extends Exception {
    public ItemExistsException() {
        super();
    }

    public ItemExistsException(String string) {
        super(string);
    }

    public ItemExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public ItemExistsException(Throwable cause) {
        super(cause);
    }

    protected ItemExistsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
