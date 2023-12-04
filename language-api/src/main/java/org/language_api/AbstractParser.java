package org.language_api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractParser {
    public final Logger logger;

    protected AbstractParser() {
        this.logger = LoggerFactory.getLogger(this.getClass());
    }
}