package org.openalpr.model;

/**
 * ResultsError model.
 */
public class ResultsError {

    private final Boolean error;

    private final String msg;

    public ResultsError(Boolean error, String msg) {
        this.error = error;
        this.msg = msg;
    }

    /**
     * Gets the error.
     *
     * @return the error.
     */
    public Boolean getError() {
        return error;
    }

    /**
     * Gets the message.
     *
     * @return the message.
     */
    public String getMsg() {
        return msg;
    }

}
