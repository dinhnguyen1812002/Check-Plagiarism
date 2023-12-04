package org.checkplagiarism.exceptions;

import java.io.Serial;

public class SubmissionException extends ExitException {

    @Serial
    private static final long serialVersionUID = 794916053362767596L; // generated

    public SubmissionException(String message) {
        super(message);
    }

    public SubmissionException(String message, Throwable cause) {
        super(message, cause);
    }
}
