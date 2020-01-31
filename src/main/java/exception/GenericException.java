package exception;

public class GenericException extends RuntimeException {

    public GenericException(String message) {
        super(message);
        GenericExceptionMapper exMapper = new GenericExceptionMapper();
        exMapper.prepareErrorMessage(this);
    }
}

