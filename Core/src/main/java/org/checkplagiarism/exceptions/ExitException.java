package org.checkplagiarism.exceptions;

import java.io.Serial;

public abstract class ExitException extends Exception {
    @Serial
    private static final long serialVersionUID = 7091658804288889231L; // generated

    protected ExitException(String message) {
        super(message);
    }

    protected ExitException(String message, Throwable cause) {
        super(message, cause);
    }
}
