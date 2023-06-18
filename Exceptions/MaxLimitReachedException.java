package Exceptions;

public class MaxLimitReachedException extends Exception {
    
    public MaxLimitReachedException() {

    }

    public MaxLimitReachedException(String string) {
        super(string);
    }
}
