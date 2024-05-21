package xyz.mjav.theqbot.exceptions;

public class ChannelNotFoundException extends Exception {
    public ChannelNotFoundException() {
        super();
    }

    public ChannelNotFoundException(String s) {
        super(s);
    }

    public ChannelNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChannelNotFoundException(Throwable cause) {
        super(cause);
    }

    protected ChannelNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
