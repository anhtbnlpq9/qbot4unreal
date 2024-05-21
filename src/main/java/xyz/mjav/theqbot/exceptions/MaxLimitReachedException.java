package xyz.mjav.theqbot.exceptions;

public class MaxLimitReachedException extends RuntimeException {
    public MaxLimitReachedException() {
        super();
    }

    public MaxLimitReachedException(String string) {
        super(string);
    }

    public MaxLimitReachedException(String message, Throwable cause) {
        super(message, cause);
    }

    public MaxLimitReachedException(Throwable cause) {
        super(cause);
    }

    protected MaxLimitReachedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
