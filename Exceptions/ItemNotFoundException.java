package Exceptions;

public class ItemNotFoundException extends RuntimeException {
        public ItemNotFoundException() {

    }

    public ItemNotFoundException(String string) {
        super(string);
    }
}
