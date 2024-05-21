package xyz.mjav.theqbot.exceptions;

public class InvalidFormatException extends Exception {
    public InvalidFormatException() {
        super();
    }

    public InvalidFormatException(String s) {
        super(s);
    }

    public InvalidFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidFormatException(Throwable cause) {
        super(cause);
    }

    protected InvalidFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
