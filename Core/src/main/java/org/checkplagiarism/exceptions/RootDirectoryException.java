package org.checkplagiarism.exceptions;

import java.io.Serial;

public class RootDirectoryException extends ExitException {

    @Serial
    private static final long serialVersionUID = 3134534079325843267L; // generated

    public RootDirectoryException(String message) {
        super(message);
    }

    public RootDirectoryException(String message, Throwable cause) {
        super(message, cause);
    }
}

