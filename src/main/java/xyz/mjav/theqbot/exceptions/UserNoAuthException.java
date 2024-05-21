package xyz.mjav.theqbot.exceptions;

public class UserNoAuthException extends RuntimeException {
    public UserNoAuthException() {
        super();
    }

    public UserNoAuthException(String s) {
        super(s);
    }

    public UserNoAuthException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNoAuthException(Throwable cause) {
        super(cause);
    }

    protected UserNoAuthException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
