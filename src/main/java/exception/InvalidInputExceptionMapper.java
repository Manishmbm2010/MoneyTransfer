package exception;

import util.HttpStatus;

public class InvalidInputExceptionMapper implements ExceptionMapper<InvalidInputException> {

    @Override
    public void prepareErrorMessage(InvalidInputException exception) {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setErrorMessageKey(ErrorMessages.MISSING_REQUIRED_FIELD.name());
        errorMessage.setErrorMessageValue(exception.getMessage());
        sendResponse(errorMessage, HttpStatus.BAD_REQUEST);
    }
}
