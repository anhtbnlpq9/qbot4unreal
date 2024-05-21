package xyz.mjav.theqbot.exceptions;

public class ConfigFileErrorException extends Exception {
    public ConfigFileErrorException() {
        super();
    }

    public ConfigFileErrorException(String s) {
        super(s);
    }

    public ConfigFileErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigFileErrorException(Throwable cause) {
        super(cause);
    }

    protected ConfigFileErrorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
