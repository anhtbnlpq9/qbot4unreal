package xyz.mjav.theqbot.exceptions;

public class ServerNotFoundException extends Exception {
    public ServerNotFoundException() {
        super();
    }

    public ServerNotFoundException(String s) {
        super(s);
    }

    public ServerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerNotFoundException(Throwable cause) {
        super(cause);
    }

    protected ServerNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
