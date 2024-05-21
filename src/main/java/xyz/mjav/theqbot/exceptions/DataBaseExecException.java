package xyz.mjav.theqbot.exceptions;

public class DataBaseExecException extends Exception {
    public DataBaseExecException() {
        super();
    }

    public DataBaseExecException(String string) {
        super(string);
    }

    public DataBaseExecException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataBaseExecException(Throwable cause) {
        super(cause);
    }

    protected DataBaseExecException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
