package org.language.python;

import org.kohsuke.MetaInfServices;
import org.language_api.ParsingException;
import org.language_api.Token;

import java.io.File;
import java.util.List;
import java.util.Set;

@MetaInfServices(org.language_api.Language.class)
public class Language implements org.language_api.Language {
    private static final String IDENTIFIER = "python3";

    private final Parsers parser;

    public Language() {
        parser = new Parsers();
    }

    @Override
    public String[] suffixes() {
        return new String[] {".py"};
    }

    @Override
    public String getName() {
        return "Python3 Parser";
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public int minimumTokenMatch() {
        return 12;
    }

    @Override
    public List<Token> parse(Set<File> files) throws ParsingException {
        return null;

    }
}
