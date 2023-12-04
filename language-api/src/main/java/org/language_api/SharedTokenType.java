package org.language_api;

public enum SharedTokenType implements TokenType {

    FILE_END("EOF");

    private final String description;

    public String getDescription() {
        return description;
    }

    SharedTokenType(String description) {
        this.description = description;
    }

    @Override
    public Boolean isExcludedFromMatching() {
        return true;
    }
}

