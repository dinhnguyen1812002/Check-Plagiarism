package org.language_api;



import java.io.File;
import java.io.Serial;
import java.util.Collection;
import java.util.stream.Collectors;


public class ParsingException extends Exception {
    @Serial
    private static final long serialVersionUID = 4385949762027596330L;

    public ParsingException(File file) {
        this(file, (String) null);
    }


    public ParsingException(File file, String reason) {
        super(constructMessage(file, reason));
    }


    public ParsingException(File file, Throwable cause) {
        this(file, null, cause);
    }


    public ParsingException(File file, String reason, Throwable cause) {
        super(constructMessage(file, reason), cause);
    }


    public static ParsingException wrappingExceptions(Collection<ParsingException> exceptions) {
        switch (exceptions.size()) {
            case 0:
                return null;
            case 1:
                return exceptions.iterator().next();
            default: {
                String message = exceptions.stream().map(ParsingException::getMessage).collect(Collectors.joining("\n"));
                return new ParsingException(message);
            }
        }
    }

    private ParsingException(String message) {
        super(message);
    }

    private static String constructMessage(File file, String reason) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("failed to parse '%s'".formatted(file));
        if (reason != null && !reason.isBlank()) {
            messageBuilder.append(" with reason: %s".formatted(reason));
        }
        return messageBuilder.toString();
    }
}

