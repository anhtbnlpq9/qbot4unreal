package xyz.mjav.theqbot.exceptions;

public class QBotException extends Exception {
   public QBotException() {
      super();
   }

   public QBotException(String message) {
      super(message);
   }

   public QBotException(String message, Throwable cause) {
      super(message, cause);
   }

   public QBotException(Throwable cause) {
      super(cause);
   }

   protected QBotException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
   }
}