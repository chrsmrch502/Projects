package utils;

import database.Timeline;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

//Utility class to hold a couple of date-related methods, since java.time.LocalDateTime is final and can't be extended
public class DateUtils {
    public static int distanceBetween(LocalDateTime from, LocalDateTime to, int scale) {
        TemporalUnit unit = null;
        switch (scale) {
            case 1:
                unit = ChronoUnit.MILLIS;
                break;
            case 2:
                unit = ChronoUnit.SECONDS;
                break;
            case 3:
                unit = ChronoUnit.MINUTES;
                break;
            case 4:
                unit = ChronoUnit.HOURS;
                break;
            case 5:
                unit = ChronoUnit.DAYS;
                break;
            case 6:
                unit = ChronoUnit.WEEKS;
                break;
            case 7:
                unit = ChronoUnit.MONTHS;
                break;
            case 8:
                unit = ChronoUnit.YEARS;
                break;
            case 9:
                unit = ChronoUnit.DECADES;
                break;
            case 10:
                unit = ChronoUnit.CENTURIES;
                break;
            case 11:
                unit = ChronoUnit.MILLENNIA;
                break;
        }

        assert unit != null;
        return (int) from.until(to, unit);
    }

    public static String ddmmyyToString(Timeline activeTimeline) {
        return activeTimeline.getStartDate().getDayOfMonth() + "."
                + activeTimeline.getStartDate().getMonthValue() + "." + activeTimeline.getStartDate().getYear() + " - "
                + activeTimeline.getEndDate().getDayOfMonth() + "." + activeTimeline.getEndDate().getMonthValue() + "."
                + activeTimeline.getEndDate().getYear();
    }
}
