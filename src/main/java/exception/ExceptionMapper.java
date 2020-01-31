package exception;

import util.HttpStatus;
import util.Utils;

public interface ExceptionMapper<E extends Throwable> {

    void prepareErrorMessage(E exception);

    default void sendResponse(ErrorMessage errorMessage, HttpStatus status) {
        // todo make a property
        boolean shouldSendHttpResponse = true;
        if (shouldSendHttpResponse) {
            Utils.sendResponse(status, errorMessage);
        }
    }
}
