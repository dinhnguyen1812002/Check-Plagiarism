package org.language_api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Token {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    public static final int NO_VALUE = -1;

    private int line;
    private int column;
    private int length;
    private File file;
    private TokenType type;
    public static Token fileEnd(File file) {
        return new Token(SharedTokenType.FILE_END, file, NO_VALUE, NO_VALUE, NO_VALUE);
    }

    public Token(TokenType type, File file, int line, int column, int length) {
        if (line == 0) {
            logger.warn("Creating a token with line index 0 while index is 1-based");
        }
        if (column == 0) {
            logger.warn("Creating a token with column index 0 while index is 1-based");
        }
        this.type = type;
        this.file = file;
        this.line = line;
        this.column = column;
        this.length = length;
    }
    public int getColumn() {
        return column;
    }


    public File getFile() {
        return file;
    }

    public int getLength() {
        return length;
    }

    public int getLine() {
        return line;
    }

    public TokenType getType() {
        return type;
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
