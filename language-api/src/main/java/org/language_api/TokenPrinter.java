package org.language_api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class TokenPrinter {
    private static final Logger logger = LoggerFactory.getLogger(TokenPrinter.class);
    // Representation:
    private static final String BAR = "|";
    private static final String TAB = "\t";
    private static final String SPACE = " ";
    private static final String NON_WHITESPACE = "\\S";
    private static final int MIN_PADDING = 1;
    private static final int TAB_LENGTH = 8;
    private static final String TAB_REPLACEMENT = SPACE.repeat(TAB_LENGTH); // might depend on files

    // Configuration:
    private static final boolean INDICATE_TINY_TOKEN = true;
    private static final boolean REPLACE_TABS = false;
    private static final boolean PRINT_EMPTY_LINES = true;
    private static final boolean SPACIOUS = true;

    private TokenPrinter() {

    }


    public static String printTokens(List<Token> tokens, File rootDirectory) {
        return printTokens(tokens, rootDirectory, Optional.empty());
    }


    public static String printTokens(List<Token> tokenList, File rootDirectory, Optional<String> suffix) {
        PrinterOutputBuilder builder = new PrinterOutputBuilder();
        Map<File, List<Token>> fileToTokens = groupTokensByFile(tokenList);

        fileToTokens.forEach((File file, List<Token> fileTokens) -> {
            builder.append(rootDirectory.toPath().relativize(file.toPath()).toString());

            List<LineData> lineDatas = getLineData(fileTokens, suffix);
            lineDatas.forEach(lineData -> {
                builder.setLine(lineData.lineNumber());

                // Print (prefix and) code line
                String currentLine = lineData.text();
                builder.appendCodeLine(currentLine);

                List<Token> tokens = lineData.tokens();
                if (tokens.isEmpty()) {
                    return;
                }

                builder.appendTokenLinePrefix();

                // Print tokens
                for (Token token : tokens) {
                    // Move to token index, possibly adding a new line:
                    builder.advanceToTokenPosition(currentLine, token.getColumn(), true);

                    // Print the actual token:
                    String stringRepresentation = getStringRepresentation(token);
                    builder.append(BAR).append(stringRepresentation);

                    // Move up to token end:
                    int tokenEndIndex = token.getColumn() + token.getLength() - 1;
                    builder.advanceToTokenPosition(currentLine, tokenEndIndex, false);

                    // Print token end if not already past it:
                    if (builder.positionBeforeOrEqualTo(tokenEndIndex)) {
                        builder.append(BAR);
                    }
                }

                builder.appendTokenLineSuffix();
                builder.advanceToNextLine();
            });
            builder.advanceToNextLine();
        });

        return builder.toString();
    }

    private static List<LineData> getLineData(List<Token> fileTokens, Optional<String> suffix) {
        // We expect that all fileTokens share the same Token.file!
        File file = fileTokens.get(0).getFile();
        if (suffix.isPresent()) {
            file = new File(file.getPath() + suffix.get());
        }

        // Sort tokens by file and line -> tokens can be processed without any further checks
        List<String> lines = linesFromFile(file);

        int currentLine = Token.NO_VALUE;
        Map<Integer, List<Token>> lineNumbersToTokens = new HashMap<>(fileTokens.size());
        for (Token token : fileTokens) {
            if (token.getLine() != Token.NO_VALUE) {
                currentLine = token.getLine();
            }
            int line = token.getType() == SharedTokenType.FILE_END ? lines.size() : currentLine;
            List<Token> tokens = lineNumbersToTokens.containsKey(line) ? lineNumbersToTokens.get(line) : new ArrayList<>();
            tokens.add(token);
            lineNumbersToTokens.put(line, tokens);
        }

        // create LineData for each line -- 1-based line index
        Stream<Integer> lineNumbers = PRINT_EMPTY_LINES ? IntStream.range(1, lines.size() + 1).boxed() : lineNumbersToTokens.keySet().stream();
        return lineNumbers.map(lineIndex -> new LineData(lineIndex, lines.get(lineIndex - 1), lineNumbersToTokens.getOrDefault(lineIndex, List.of())))
                .toList();
    }

    private static Map<File, List<Token>> groupTokensByFile(List<Token> tokens) {
        return tokens.stream().collect(Collectors.groupingBy(Token::getFile));
    }

    /**
     * Determines the string representation of the token.
     */
    private static String getStringRepresentation(Token token) {
        String description = token.getType().getDescription();
        return token.getLength() <= 1 && INDICATE_TINY_TOKEN ? description.toLowerCase() : description;
    }

    /**
     * Parses a file and returns a list of the contained lines.
     */
    private static List<String> linesFromFile(File file) {
        try {
            return Files.readAllLines(file.toPath());
        } catch (NoSuchFileException exception) {
            logger.error("File does not exist, thus no tokens are printed: " + file.getAbsolutePath());
        } catch (IOException exception) {
            logger.error("Cannot read " + file.getAbsolutePath() + ":", exception);
        }
        return Collections.emptyList();
    }

    private record LineData(Integer lineNumber, String text, List<Token> tokens) {

    }

    /**
     * A proxy for the StringBuilder that keeps track of the position inside the output.
     */
    private static class PrinterOutputBuilder {
        public static final String LINE_SEPARATOR = System.lineSeparator();
        private final StringBuilder builder = new StringBuilder();
        private int columnIndex = 1;
        private int lineNumber;
        private int trailingLineSeparators = 0;

        /**
         * Returns the number of digits (including a minus) of the given number.
         */
        private static int digitCount(int number) {
            if (number == 0) {
                return 1;
            }
            int minusLength = number < 0 ? 1 : 0;
            // The 'log10' variant is supposedly faster than the 'toString' variant.
            return (int) Math.log10(Math.abs(number)) + minusLength + 1;
        }

        private void resetLinePosition() {
            columnIndex = 1;
        }

        /**
         * Appends the given string to the output
         * @param str the string to append
         * @return this
         */
        public PrinterOutputBuilder append(String str) {
            // Avoid too many blank lines
            trailingLineSeparators = str.equals(LINE_SEPARATOR) ? trailingLineSeparators + 1 : 0;
            if (trailingLineSeparators >= 3)
                return this;

            builder.append(str);
            columnIndex += str.length();
            return this;
        }

        /**
         * Appends the given integer to the output.
         * @param i the integer to append
         * @return this
         */
        public PrinterOutputBuilder append(int i) {
            return append(Integer.toString(i));
        }

        /**
         * In SPACIOUS mode, appends an empty line before the next code line.
         * @return a reference to this
         */
        private PrinterOutputBuilder appendTokenLineSuffix() {
            return SPACIOUS ? advanceToNextLine() : this;
        }

        /**
         * Appends the code line to the StringBuilder, applying the configuration of this TokenPrinter.
         * @param currentLine The line of code to append
         * @return a reference to this
         */
        private PrinterOutputBuilder appendCodeLine(String currentLine) {
            // Prefix
            advanceToNextLine().append(lineNumber);

            // Code line should be padded up to at least one tab length, and no less than MIN_PADDING
            int paddingLength = Math.max(TAB_LENGTH - digitCount(lineNumber), MIN_PADDING);
            appendPadding(paddingLength);

            // Code line
            if (REPLACE_TABS) {
                currentLine = currentLine.replaceAll(TAB, TAB_REPLACEMENT);
            }

            return append(currentLine);
        }


        PrinterOutputBuilder advanceToTokenPosition(String currentLine, int targetPosition, boolean breakLine) {
            targetPosition = Math.min(targetPosition, currentLine.length());
            if (!positionBeforeOrEqualTo(targetPosition)) {
                if (!breakLine) {
                    // we are past targetPosition, do nothing
                    return this;
                }

                // new line
                appendTokenLinePrefix();
                if (!positionBeforeOrEqualTo(targetPosition)) {
                    // still past targetPosition -> negative targetPosition
                    return this;
                }
            }

            // The replacement operation preserves TAB characters, which is essential for correct alignment
            String padding = currentLine.substring(columnIndex - 1, targetPosition - 1)
                    // TAB is a string, so use replace
                    .replace(TAB, REPLACE_TABS ? TAB_REPLACEMENT : TAB)
                    // NON_WHITESPACE is a pattern, so use replaceAll
                    .replaceAll(NON_WHITESPACE, SPACE);
            append(padding);

            return this;
        }

        /**
         * Returns true if the current position is before or at the target position.
         * @param targetPosition the target position
         * @return true if before or at target position
         */
        private boolean positionBeforeOrEqualTo(int targetPosition) {
            return columnIndex <= targetPosition;
        }

        /**
         * Appends a newline, plus the amount of padding necessary to align tokens correctly.
         * @return this
         */
        PrinterOutputBuilder appendTokenLinePrefix() {
            int paddingLength = Math.max(digitCount(lineNumber) + MIN_PADDING, TAB_LENGTH);
            advanceToNextLine().appendPadding(paddingLength);
            resetLinePosition();
            return this;
        }

        /**
         * Appends the amount of padding given.
         * @param paddingLength Length of padding to add
         * @return this
         */
        private PrinterOutputBuilder appendPadding(int paddingLength) {
            String padding = SPACE.repeat(paddingLength);
            append(padding);
            return this;
        }

        /**
         * Appends a newline.
         * @return this
         */
        private PrinterOutputBuilder advanceToNextLine() {
            append(LINE_SEPARATOR);
            resetLinePosition();
            return this;
        }

        public void setLine(int lineNumber) {
            this.lineNumber = lineNumber;
        }

        @Override
        public String toString() {
            return builder.toString();
        }
    }
}
