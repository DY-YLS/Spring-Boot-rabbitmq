package com.example.demo.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeUtil {

    private static String defaultFormatter = "yyyy-MM-dd HH:mm:ss";

    public static String format(LocalDateTime localDateTime) {
        return format(localDateTime, defaultFormatter);
    }

    public static String format(LocalDateTime localDateTime, String formatter) {
        String format = localDateTime.format(DateTimeFormatter.ofPattern(formatter));
        return format;
    }

    public static LocalDateTime parse(String dateTimeString) {
        return parse(dateTimeString, defaultFormatter);
    }

    public static LocalDateTime parse(String dateTimeString, String formatter) {
        LocalDateTime parse = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(formatter));
        return parse;
    }
}
