package Exceptions;

public class ItemSuspendedException extends RuntimeException {
    public ItemSuspendedException() {

    }

    public ItemSuspendedException(String string) {
        super(string);
    }
}
