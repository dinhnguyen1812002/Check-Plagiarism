package org.language.java;

import org.language_api.AbstractParser;
import org.language_api.ParsingException;
import org.language_api.Token;
import org.language_api.TokenType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Parser extends AbstractParser {
    private List<Token> tokens;

    /**
     * Creates the parser.
     */
    public Parser() {
        super();
    }

    public List<Token> parse(Set<File> files) throws ParsingException {
        tokens = new ArrayList<>();
        new JavacAdapter().parseFiles(files, this);
        return tokens;
    }

    public void add(TokenType type, File file, long line, long column, long length) {
        add(new Token(type, file, (int) line, (int) column, (int) length));
    }

    public void add(Token token) {
        tokens.add(token);
    }
}
