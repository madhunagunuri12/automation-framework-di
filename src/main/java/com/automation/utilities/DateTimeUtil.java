package com.automation.utilities;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Central utility class for Date & Time operations.
 * Uses Java 8+ java.time API (Thread-safe & Immutable).
 */
public final class DateTimeUtil {

    /* =========================
       CONSTRUCTOR
       ========================= */

    private DateTimeUtil() {
        throw new UnsupportedOperationException("Utility class");
    }


    /* =========================
       DEFAULT FORMATS
       ========================= */

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";


    /* =========================
       CURRENT DATE / TIME
       ========================= */

    public static LocalDate currentDate() {
        return LocalDate.now();
    }

    public static LocalDateTime currentDateTime() {
        return LocalDateTime.now();
    }

    public static ZonedDateTime currentZonedDateTime() {
        return ZonedDateTime.now();
    }

    public static String currentDate(String pattern) {
        return format(LocalDate.now(), pattern);
    }

    public static String currentDateTime(String pattern) {
        return format(LocalDateTime.now(), pattern);
    }


    /* =========================
       RELATIVE DATE
       ========================= */

    /**
     *  0  = Today
     * -1  = Yesterday
     *  1  = Tomorrow
     */
    public static LocalDate getRelativeDate(int relativeDays) {
        return LocalDate.now().plusDays(relativeDays);
    }

    public static String getRelativeDate(int relativeDays, String pattern) {
        return format(getRelativeDate(relativeDays), pattern);
    }

    public static LocalDateTime getRelativeDateTime(int relativeDays) {
        return LocalDateTime.now().plusDays(relativeDays);
    }


    /* =========================
       FORMAT METHODS
       ========================= */

    public static String format(LocalDate date, String pattern) {
        Objects.requireNonNull(date);
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String format(LocalDateTime dateTime, String pattern) {
        Objects.requireNonNull(dateTime);
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String format(ZonedDateTime zonedDateTime, String pattern) {
        Objects.requireNonNull(zonedDateTime);
        return zonedDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }


    /* =========================
       STRING PARSING
       ========================= */

    public static LocalDate parseToLocalDate(String date, String pattern) {
        return LocalDate.parse(date,
                DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDateTime parseToLocalDateTime(String dateTime,
                                                     String pattern) {

        return LocalDateTime.parse(dateTime,
                DateTimeFormatter.ofPattern(pattern));
    }

    public static ZonedDateTime parseToZonedDateTime(String dateTime,
                                                     String pattern,
                                                     String zoneId) {

        LocalDateTime localDateTime =
                LocalDateTime.parse(dateTime,
                        DateTimeFormatter.ofPattern(pattern));

        return localDateTime.atZone(ZoneId.of(zoneId));
    }


    /* =========================
       TIME ZONE
       ========================= */

    public static ZonedDateTime convertZone(ZonedDateTime source,
                                            String targetZone) {

        return source.withZoneSameInstant(
                ZoneId.of(targetZone)
        );
    }


    /* =========================
       START / END OF DAY
       ========================= */

    public static LocalDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    public static LocalDateTime endOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }


    /* =========================
       EPOCH
       ========================= */

    public static long toEpochMilli(ZonedDateTime zonedDateTime) {
        return zonedDateTime
                .toInstant()
                .toEpochMilli();
    }

    public static ZonedDateTime fromEpochMilli(long epochMilli,
                                               String zoneId) {

        return Instant.ofEpochMilli(epochMilli)
                .atZone(ZoneId.of(zoneId));
    }


    /* =========================
       DATE DIFFERENCE
       ========================= */

    public static long daysBetween(LocalDate start,
                                   LocalDate end) {

        return ChronoUnit.DAYS.between(start, end);
    }

    public static long hoursBetween(LocalDateTime start,
                                    LocalDateTime end) {

        return ChronoUnit.HOURS.between(start, end);
    }


    /* ==========================================================
       SMART DATE EXPRESSION ENGINE
       ========================================================== */

    /**
     * Supported expressions:
     *
     * TODAY
     * TODAY+2
     * TODAY-1
     * TODAY+1M
     * TODAY+1Y
     *
     * NOW
     * NOW+3H
     * NOW-15M
     * NOW+10S
     *
     * TODAY+2@Asia/Kolkata
     * NOW@America/New_York
     */
    public static String resolve(String expression) {

        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Expression cannot be null/empty");
        }

        String exp = expression.toUpperCase().trim();

        /* ---------- Zone Handling ---------- */

        String zone = null;

        if (exp.contains("@")) {
            String[] parts = exp.split("@");
            exp = parts[0];
            zone = parts[1];
        }

        ZoneId zoneId = (zone != null)
                ? ZoneId.of(zone)
                : ZoneId.systemDefault();


        /* ---------- TODAY ---------- */

        if (exp.equals("TODAY")) {

            return LocalDate.now(zoneId)
                    .format(DateTimeFormatter.ofPattern(
                            DEFAULT_DATE_FORMAT));
        }


        /* ---------- NOW ---------- */

        if (exp.equals("NOW")) {

            return ZonedDateTime.now(zoneId)
                    .format(DateTimeFormatter.ofPattern(
                            DEFAULT_DATE_TIME_FORMAT));
        }


        /* ---------- TODAY +/- ---------- */

        if (exp.startsWith("TODAY")) {

            LocalDate base =
                    LocalDate.now(zoneId);

            return resolveDateOperation(base, exp);
        }


        /* ---------- NOW +/- ---------- */

        if (exp.startsWith("NOW")) {

            ZonedDateTime base =
                    ZonedDateTime.now(zoneId);

            return resolveDateTimeOperation(base, exp);
        }


        throw new IllegalArgumentException(
                "Invalid expression: " + expression);
    }


    /* =========================
       DATE OPERATIONS
       ========================= */

    private static String resolveDateOperation(LocalDate base,
                                               String exp) {

        if (exp.equals("TODAY")) {
            return format(base,
                    DEFAULT_DATE_FORMAT);
        }

        String op = exp.substring(5);

        int sign = op.startsWith("-") ? -1 : 1;

        op = op.substring(1);

        long value = extractNumber(op);
        char unit = extractUnit(op);

        switch (unit) {

            case 'D':
                base = base.plusDays(sign * value);
                break;

            case 'M':
                base = base.plusMonths(sign * value);
                break;

            case 'Y':
                base = base.plusYears(sign * value);
                break;

            default:
                throw new IllegalArgumentException(
                        "Invalid unit for TODAY: " + unit);
        }

        return format(base,
                DEFAULT_DATE_FORMAT);
    }


    /* =========================
       DATETIME OPERATIONS
       ========================= */

    private static String resolveDateTimeOperation(ZonedDateTime base,
                                                   String exp) {

        if (exp.equals("NOW")) {

            return format(base,
                    DEFAULT_DATE_TIME_FORMAT);
        }

        String op = exp.substring(3);

        int sign = op.startsWith("-") ? -1 : 1;

        op = op.substring(1);

        long value = extractNumber(op);
        char unit = extractUnit(op);

        switch (unit) {

            case 'S':
                base = base.plusSeconds(sign * value);
                break;

            case 'M':
                base = base.plusMinutes(sign * value);
                break;

            case 'H':
                base = base.plusHours(sign * value);
                break;

            case 'D':
                base = base.plusDays(sign * value);
                break;

            default:
                throw new IllegalArgumentException(
                        "Invalid unit for NOW: " + unit);
        }

        return format(base,
                DEFAULT_DATE_TIME_FORMAT);
    }


    /* =========================
       PARSER HELPERS
       ========================= */

    private static long extractNumber(String value) {

        String num = value.replaceAll("[^0-9]", "");

        if (num.isEmpty()) {
            throw new IllegalArgumentException(
                    "Missing number in expression");
        }

        return Long.parseLong(num);
    }

    private static char extractUnit(String value) {

        String unit = value.replaceAll("[0-9]", "");

        if (unit.length() != 1) {
            throw new IllegalArgumentException(
                    "Invalid unit in expression");
        }

        return unit.charAt(0);
    }

}
