package Exceptions;

public class DataBaseExecException extends RuntimeException {
    public DataBaseExecException() {

    }

    public DataBaseExecException(String string) {
        super(string);
    }
}
