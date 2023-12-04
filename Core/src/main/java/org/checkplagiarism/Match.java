package org.checkplagiarism;




public record Match(int startOfFirst, int startOfSecond, int length) {

    public boolean overlaps(Match other) {
        if (startOfFirst < other.startOfFirst) {
            if ((other.startOfFirst - startOfFirst) < length) {
                return true;
            }
        } else {
            if ((startOfFirst - other.startOfFirst) < other.length) {
                return true;
            }
        }

        if (startOfSecond < other.startOfSecond) {
            return (other.startOfSecond - startOfSecond) < length;
        } else {
            return (startOfSecond - other.startOfSecond) < other.length;
        }
    }

    /**
     * @return the token index of the last token of the match in the first submission.
     */
    public int endOfFirst() {
        return startOfFirst + length - 1;
    }

    /**
     * @return the token index of the last token of the match in the second submission.
     */
    public int endOfSecond() {
        return startOfSecond + length - 1;
    }
}
