package xyz.mjav.theqbot.exceptions;

public class UserFlagNoAccessRightException extends RuntimeException {
    public UserFlagNoAccessRightException() {
        super();
    }

    public UserFlagNoAccessRightException(String s) {
        super(s);
    }

    public UserFlagNoAccessRightException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserFlagNoAccessRightException(Throwable cause) {
        super(cause);
    }

    protected UserFlagNoAccessRightException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
