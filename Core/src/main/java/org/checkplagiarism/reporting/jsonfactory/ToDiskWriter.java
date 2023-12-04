package org.checkplagiarism.reporting.jsonfactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

public class ToDiskWriter implements FileWriter {
    private static final Logger logger = LoggerFactory.getLogger(ToDiskWriter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void saveAsJSON(Object fileToSave, String folderPath, String fileName) {
        try {
            objectMapper.writeValue(Path.of(folderPath, fileName).toFile(), fileToSave);
        } catch (IOException e) {
            logger.error("Failed to save json file " + fileName + ": " + e.getMessage(), e);
        }
    }
}

