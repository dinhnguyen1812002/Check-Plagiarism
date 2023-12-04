package org.language_api;

public interface TokenType {
    String getDescription();

    default Boolean isExcludedFromMatching() {
        return false;
    }
}
