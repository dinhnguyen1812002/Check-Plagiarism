package org.checkplagiarism.reporting.reportobject.model;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;


public record Version(@JsonProperty("major") int major, @JsonProperty("minor") int minor, @JsonProperty("patch") int patch) {

    private static final Logger logger = LoggerFactory.getLogger(Version.class);

    public static final Version DEVELOPMENT = new Version(0, 0, 0);


    public static Version parseVersion(String version) {
        String plainVersion = version.startsWith("v") ? version.substring(1) : version;
        plainVersion = plainVersion.replace("-SNAPSHOT", "");

        if (!plainVersion.matches("\\d+\\.\\d+\\.\\d+")) {
            logger.debug("Version {} could not be parsed. Defaulting to null.", version);
            return null;
        }
        String[] versionParts = plainVersion.split("\\.");
        return new Version(Integer.parseInt(versionParts[0]), Integer.parseInt(versionParts[1]), Integer.parseInt(versionParts[2]));
    }

    @Override
    public String toString() {
        return String.format("%d.%d.%d", major, minor, patch);
    }
}

