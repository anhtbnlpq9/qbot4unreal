package xyz.mjav.theqbot.exceptions;

public class ChanlevModIsZeroException extends RuntimeException {
    public ChanlevModIsZeroException() {
        super();
    }

    public ChanlevModIsZeroException(String s) {
        super(s);
    }

    public ChanlevModIsZeroException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChanlevModIsZeroException(Throwable cause) {
        super(cause);
    }

    protected ChanlevModIsZeroException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
