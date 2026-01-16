package uj.wmii.pwj.delegations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Calc {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ROOT);

    public BigDecimal calculate(String name, String start, String end, BigDecimal dailyRate) {
        if (start == null || end == null || dailyRate == null) {
            throw new IllegalArgumentException("start/end/dailyRate cannot be null");
        }

        ZonedDateTime zStart = parse(start);
        ZonedDateTime zEnd = parse(end);

        Instant iStart = zStart.toInstant();
        Instant iEnd = zEnd.toInstant();

        if (!iStart.isBefore(iEnd)) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        Duration duration = Duration.between(iStart, iEnd);

        long totalMinutes = duration.toMinutes();
        long minutesPerDay = 24L * 60L;

        long fullDays = totalMinutes / minutesPerDay;
        long remainderMinutes = totalMinutes % minutesPerDay;

        BigDecimal result = dailyRate.multiply(BigDecimal.valueOf(fullDays));

        if (remainderMinutes > 0) {
            long minutes8h = 8L * 60L;
            long minutes12h = 12L * 60L;

            BigDecimal fraction;
            if (remainderMinutes <= minutes8h) {
                fraction = BigDecimal.ONE.divide(BigDecimal.valueOf(3), 10, RoundingMode.HALF_UP);
            } else if (remainderMinutes <= minutes12h) {
                fraction = BigDecimal.ONE.divide(BigDecimal.valueOf(2), 10, RoundingMode.HALF_UP);
            } else {
                fraction = BigDecimal.ONE;
            }

            result = result.add(dailyRate.multiply(fraction));
        }

        return result.setScale(2, RoundingMode.HALF_UP);
    }

    private static ZonedDateTime parse(String value) {
        String trimmed = value.trim();
        int lastSpace = trimmed.lastIndexOf(' ');
        if (lastSpace < 0) {
            throw new IllegalArgumentException("Invalid date-time format: " + value);
        }

        String dateTimePart = trimmed.substring(0, lastSpace).trim();
        String zonePart = trimmed.substring(lastSpace + 1).trim();

        ZoneId zone = ZoneId.of(zonePart);
        return ZonedDateTime.of(
                java.time.LocalDateTime.parse(dateTimePart, FORMATTER),
                zone
        );
    }
}