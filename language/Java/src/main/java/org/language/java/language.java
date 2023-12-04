package org.language.java;


import org.kohsuke.MetaInfServices;
import org.language_api.ParsingException;
import org.language_api.Token;

import java.io.File;
import java.util.List;
import java.util.Set;

@MetaInfServices(org.language_api.Language.class)
public class language implements org.language_api.Language{
    private static final String IDENTIFIER = "java";

    private final Parser parser;

    public language() {
        this.parser = new Parser();
    }

    @Override
    public String[] suffixes() {
        return new String[] {".java", ".JAVA"};
    }

    @Override
    public String getName() {
        return "Javac based AST plugin";
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public int minimumTokenMatch() {
        return 9;
    }

    @Override
    public List<Token> parse(Set<File> files) throws ParsingException {
        return this.parser.parse(files);
    }
}
