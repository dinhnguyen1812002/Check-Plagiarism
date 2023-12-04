package org.checkplagiarism.exceptions;

import java.io.Serial;

public class BasecodeException extends ExitException {
    @Serial
    private static final long serialVersionUID = -3911476090624995247L; // generated

    public BasecodeException(String message) {
        super(message);
    }

    public BasecodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
