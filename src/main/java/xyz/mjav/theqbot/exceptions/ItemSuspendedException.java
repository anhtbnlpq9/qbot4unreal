package xyz.mjav.theqbot.exceptions;

public class ItemSuspendedException extends RuntimeException {
    public ItemSuspendedException() {
        super();
    }

    public ItemSuspendedException(String string) {
        super(string);
    }

    public ItemSuspendedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ItemSuspendedException(Throwable cause) {
        super(cause);
    }

    protected ItemSuspendedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
