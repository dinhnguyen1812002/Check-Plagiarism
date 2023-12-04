package org.language_api;



import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Common interface for all languages. Each language-front end must provide a concrete language implementation.
 */
public interface Language {

    String[] suffixes();

    String getName();


    String getIdentifier();

    /**
     * Minimum number of tokens required for a match.
     */
    int minimumTokenMatch();


    List<Token> parse(Set<File> files) throws ParsingException;

    /**
     * Determines whether a fixed-width font should be used to display that language.
     */
    default boolean isPreformatted() {
        return true;
    }


    default boolean useViewFiles() {
        return false;
    }

    /**
     * If the language uses representation files, this method returns the suffix used for the representation files.
     */
    default String viewFileSuffix() {
        return "";
    }
}
