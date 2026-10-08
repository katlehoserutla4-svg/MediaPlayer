package com.mediaplayer.util;

import javafx.util.Duration;

public final class TimeFormatter {
    private TimeFormatter() {
    }

    public static String format(Duration duration) {
        if (duration == null || duration.isUnknown() || duration.lessThan(Duration.ZERO)) {
            return "00:00";
        }

        long seconds = Math.round(duration.toSeconds());
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
        }
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }
}