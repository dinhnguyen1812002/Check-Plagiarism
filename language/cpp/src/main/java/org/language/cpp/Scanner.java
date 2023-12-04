package org.language.cpp;

import org.language_api.AbstractParser;
import org.language_api.ParsingException;
import org.language_api.Token;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
public class Scanner extends AbstractParser {
    private File currentFile;

    private List<Token> tokens;

    /**
     * Creates the parser.
     */
    public Scanner() {
        super();
    }

    public List<Token> scan(Set<File> files) throws ParsingException {
        tokens = new ArrayList<>();
        for (File file : files) {
            this.currentFile = file;
            logger.trace("Scanning file {}", currentFile);
            CPPScanner.scanFile(file, this);
            tokens.add(Token.fileEnd(currentFile));
        }
        return tokens;
    }

    public void add(CPPTokenType type, org.language.cpp.Token token) {
        int length = token.endColumn - token.beginColumn + 1;
        tokens.add(new Token(type, currentFile, token.beginLine, token.beginColumn, length));
    }
}