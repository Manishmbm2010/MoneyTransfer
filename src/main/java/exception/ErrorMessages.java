package exception;

public enum ErrorMessages {
    MISSING_REQUIRED_FIELD("Missing required field. Please check documentation for required fields"),
    COULD_NOT_CREATE_TRANSACTION("Could not create transaction"),
    COULD_NOT_UPDATE_TRANSACTION("Could not update transaction"),
    COULD_NOT_DELETE_TRANSACTION("Could not delete transaction"),
    NO_RECORD_FOUND("No record found for provided id"),
    COULD_NOT_CREATE_ACCOUNT("Could not create account"),
    COULD_NOT_UPDATE_ACCOUNT("Could not update account"),
    COULD_NOT_DELETE_ACCOUNT("Could not delete account"),
    RECORD_ALREADY_EXISTS("Record already exists"),
    INTERNAL_SERVER_ERROR("Something went wrong. Please repeat this operation later.");

    private String errorMessage;

    ErrorMessages(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param errorMessage the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
