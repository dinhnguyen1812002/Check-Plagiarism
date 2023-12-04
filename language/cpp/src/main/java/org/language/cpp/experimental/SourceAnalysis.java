package org.language.cpp.experimental;

import java.io.File;
import java.util.Set;

public interface SourceAnalysis  {
    boolean isTokenIgnored(org.language.cpp.Token token, File file);

    void findUnusedVariableLines(Set<File> files) throws InterruptedException;
}
