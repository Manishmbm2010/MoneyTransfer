package exception;

import util.HttpStatus;

public class RecordNotPresentExceptionMapper implements ExceptionMapper<RecordNotPresentException> {

    @Override
    public void prepareErrorMessage(RecordNotPresentException exception) {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setErrorMessageKey(ErrorMessages.NO_RECORD_FOUND.name());
        errorMessage.setErrorMessageValue(exception.getMessage());
        sendResponse(errorMessage, HttpStatus.NOT_FOUND);
    }
}
