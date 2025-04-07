package com.traccar.PositionGeofence.helper;


import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public final class DateUtil {

    private DateUtil() {
    }

    public static Date correctDay(Date guess) {
        return correctDate(new Date(), guess, Calendar.DAY_OF_MONTH);
    }

    public static Date correctYear(Date guess) {
        return correctDate(new Date(), guess, Calendar.YEAR);
    }

    public static Date correctDate(Date now, Date guess, int field) {

        if (guess.getTime() > now.getTime()) {
            Date previous = dateAdd(guess, field, -1);
            if (now.getTime() - previous.getTime() < guess.getTime() - now.getTime()) {
                return previous;
            }
        } else if (guess.getTime() < now.getTime()) {
            Date next = dateAdd(guess, field, 1);
            if (next.getTime() - now.getTime() < now.getTime() - guess.getTime()) {
                return next;
            }
        }

        return guess;
    }

    private static Date dateAdd(Date guess, int field, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(guess);
        calendar.add(field, amount);
        return calendar.getTime();
    }

    public static Date parseDate(String value) {
        return Date.from(Instant.from(DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(value)));
    }

    public static String formatDate(Date date) {
        return formatDate(date, true);
    }

    public static String formatDate(Date date, boolean zoned) {
        if (zoned) {
            return DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault()).format(date.toInstant());
        } else {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        }
    }

}
