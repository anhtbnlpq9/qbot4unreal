package xyz.mjav.theqbot.exceptions;

public class ChanlevChanNoRightException extends RuntimeException {
    public ChanlevChanNoRightException() {
        super();
    }

    public ChanlevChanNoRightException(String s) {
        super(s);
    }

    public ChanlevChanNoRightException(String message, Throwable cause) {
        super(message, cause);
     }

     public ChanlevChanNoRightException(Throwable cause) {
        super(cause);
     }

     protected ChanlevChanNoRightException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
     }

}
