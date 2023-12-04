package org.checkplagiarism;

public class TimeUtil {
    private TimeUtil() {}

    public static String formatDuration(long durationInMilliseconds) {
        int timeInSeconds = (int) (durationInMilliseconds / 1000);
        String hours = (timeInSeconds / 3600 > 0) ? (timeInSeconds / 3600) + " h " : "";
        String minutes = (timeInSeconds / 60 > 0) ? ((timeInSeconds / 60) % 60) + " min " : "";
        String seconds = (timeInSeconds % 60) + " sec";
        return hours + minutes + seconds;
    }
}
