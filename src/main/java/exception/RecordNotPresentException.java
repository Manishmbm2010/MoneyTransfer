package exception;

public class RecordNotPresentException extends RuntimeException {

    public RecordNotPresentException(String message) {
        super(message);
        RecordNotPresentExceptionMapper exMapper = new RecordNotPresentExceptionMapper();
        exMapper.prepareErrorMessage(this);
    }
}
