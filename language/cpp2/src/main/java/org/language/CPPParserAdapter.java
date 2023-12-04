package org.language;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.language_api.AbstractParser;
import org.language_api.ParsingException;
import org.language_api.Token;
import org.language_api.TokenType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CPPParserAdapter extends AbstractParser {
    private File currentFile;

    private List<Token> tokens;
    public List<Token> scan(Set<File> files) throws ParsingException {
        tokens = new ArrayList<>();
        for (File file : files) {
            this.currentFile = file;
            logger.trace("Parsing file {}", currentFile);
            try {
                CPP14Lexer lexer = new CPP14Lexer(CharStreams.fromStream(Files.newInputStream(file.toPath())));

                CommonTokenStream tokenStream = new CommonTokenStream(lexer);
                CPP14Parser parser = new CPP14Parser(tokenStream);
                CPP14Parser.TranslationUnitContext translationUnit = parser.translationUnit();

                ParseTreeWalker.DEFAULT.walk(new CPPTokenListener(this), translationUnit);
            } catch (IOException e) {
                throw new ParsingException(file, e);
            }
            tokens.add(Token.fileEnd(currentFile));
        }
        return tokens;
    }

    public void addToken(TokenType type, int column, int line, int length) {
        tokens.add(new Token(type, currentFile, line, column, length));
    }

}
