package org.cli;

import net.sourceforge.argparse4j.inf.ArgumentContainer;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;

import java.util.HashMap;
import java.util.Map;

public class CliGroupHelper {

    private final ArgumentParser parser;
    private final Map<String, MutuallyExclusiveGroup> mutuallyExclusiveGroups = new HashMap<>();
    private final Map<String, ArgumentGroup> argumentGroups = new HashMap<>();

    public CliGroupHelper(ArgumentParser parser) {
        this.parser = parser;
    }

    public ArgumentContainer getMutuallyExclusiveGroup(String name) {
        return mutuallyExclusiveGroups.computeIfAbsent(name, parser::addMutuallyExclusiveGroup);
    }

    public ArgumentContainer getArgumentGroup(String name) {
        return argumentGroups.computeIfAbsent(name, parser::addArgumentGroup);
    }
}
