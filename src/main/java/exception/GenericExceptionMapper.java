package exception;

import util.HttpStatus;

public class GenericExceptionMapper implements ExceptionMapper<GenericException> {

    @Override
    public void prepareErrorMessage(GenericException exception) {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setErrorMessageKey(ErrorMessages.INTERNAL_SERVER_ERROR.name());
        errorMessage.setErrorMessageValue(exception.getMessage());
        sendResponse(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
