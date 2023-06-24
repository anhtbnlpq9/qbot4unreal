package Exceptions;

public class UserAuthException extends RuntimeException {
    public UserAuthException() {

    }

    public UserAuthException(String string) {
        super(string);
    }
}
