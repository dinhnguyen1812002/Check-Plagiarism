package org.cli.logger;


import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class CollectedLoggerFactory implements ILoggerFactory {

    private final ConcurrentMap<String, CollectedLogger> loggerMap;

    public CollectedLoggerFactory() {
        loggerMap = new ConcurrentHashMap<>();
    }

    public Logger getLogger(String name) {
        CollectedLogger simpleLogger = loggerMap.get(name);
        if (simpleLogger != null) {
            return simpleLogger;
        } else {
            CollectedLogger newInstance = new CollectedLogger(name);
            Logger oldInstance = loggerMap.putIfAbsent(name, newInstance);
            return oldInstance == null ? newInstance : oldInstance;
        }
    }
    public void finalizeInstances() {
        List<CollectedLogger> copy = new ArrayList<>(loggerMap.values());
        copy.forEach(CollectedLogger::printAllErrorsForLogger);
    }
}
