package Exceptions;

public class MaxLimitReachedException extends RuntimeException {
    
    public MaxLimitReachedException() {

    }

    public MaxLimitReachedException(String string) {
        super(string);
    }
}
