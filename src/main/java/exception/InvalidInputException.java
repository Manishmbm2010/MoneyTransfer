package exception;

public class InvalidInputException extends RuntimeException {

    public InvalidInputException(String message) {
        super(message);
        InvalidInputExceptionMapper exMapper = new InvalidInputExceptionMapper();
        exMapper.prepareErrorMessage(this);
    }
}

