package dev.murilo.trending;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimeRangeTest {

    @Test
    void dayTest(){
        LocalDate today = LocalDate.of(2026,10,18);
        LocalDate expected = LocalDate.of(2026,10,17);

        LocalDate result = TimeRange.DAY.cutoffDate(today);

        assertEquals(expected,result);
    }
    @Test
    void weekTest(){
        LocalDate today = LocalDate.of(2026,10,18);
        LocalDate expected = LocalDate.of(2026,10,11);

        LocalDate result = TimeRange.WEEK.cutoffDate(today);
        assertEquals(expected,result);
    }
    @Test
    void monthTest(){
        LocalDate today = LocalDate.of(2026,10,18);
        LocalDate expected = LocalDate.of(2026,9,18);

        LocalDate result = TimeRange.MONTH.cutoffDate(today);
        assertEquals(expected,result);
    }
    @Test
    void yearTest(){
        LocalDate today = LocalDate.of(2026,10,18);
        LocalDate expected = LocalDate.of(2025,10,18);

        LocalDate result = TimeRange.YEAR.cutoffDate(today);
        assertEquals(expected,result);
    }

    @Test
    void monthClampsToEndOfShorterMonth(){
        LocalDate today = LocalDate.of(2026,3,31);
        LocalDate expected = LocalDate.of(2026,2,28);

        LocalDate result = TimeRange.MONTH.cutoffDate(today);
        assertEquals(expected,result);
    }
    @Test
    void yearFromLeapDayLandsOnFeb28(){
        LocalDate today = LocalDate.of(2028,2,29);
        LocalDate expected = LocalDate.of(2027,2,28);

        LocalDate result = TimeRange.YEAR.cutoffDate(today);
        assertEquals(expected,result);
    }

}
