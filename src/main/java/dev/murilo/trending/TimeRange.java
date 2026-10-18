package dev.murilo.trending;

import java.time.LocalDate;

public enum TimeRange {
    DAY,
    WEEK,
    MONTH,
    YEAR;

    public LocalDate cutoffDate(LocalDate hoje){
        return switch (this) {
            case DAY -> hoje.minusDays(1);
            case WEEK -> hoje.minusWeeks(1);
            case MONTH -> hoje.minusMonths(1);
            case YEAR -> hoje.minusYears(1);
        };
    }
}
