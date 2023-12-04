package org.cli;


import org.language_api.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class LanguageLoader {
    private static final Logger logger = LoggerFactory.getLogger(LanguageLoader.class);

    private static Map<String, Language> cachedLanguageInstances = null;

    private LanguageLoader() {
        throw new IllegalAccessError();
    }

    public static synchronized Map<String, Language> getAllAvailableLanguages() {
        if (cachedLanguageInstances != null)
            return cachedLanguageInstances;

        Map<String, Language> languages = new TreeMap<>();

        for (Language language : ServiceLoader.load(Language.class)) {
            String languageIdentifier = language.getIdentifier();
            if (languages.containsKey(languageIdentifier)) {
                logger.error("Multiple implementations for a language '{}' are present in the classpath! Skipping ..", languageIdentifier);
                languages.remove(languageIdentifier);
                continue;
            }
            logger.debug("Loading Language Module '{}'", language.getName());
            languages.put(languageIdentifier, language);
        }
        logger.info("Available languages: '{}'", languages.values().stream().map(Language::getName).toList());

        cachedLanguageInstances = Collections.unmodifiableMap(languages);
        return cachedLanguageInstances;
    }

    public static Optional<Language> getLanguage(String identifier) {
        var language = getAllAvailableLanguages().get(identifier);
        if (language == null)
            logger.warn("Attempt to load Language {} was not successful", identifier);
        return Optional.ofNullable(language);
    }

    public static Set<String> getAllAvailableLanguageIdentifiers() {
        return new TreeSet<>(getAllAvailableLanguages().keySet());
    }
    public static synchronized void clearCache() {
        cachedLanguageInstances = null;
    }
}
