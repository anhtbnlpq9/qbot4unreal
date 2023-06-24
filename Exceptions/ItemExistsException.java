package Exceptions;

public class ItemExistsException extends RuntimeException {
        public ItemExistsException() {

    }

    public ItemExistsException(String string) {
        super(string);
    }
}
