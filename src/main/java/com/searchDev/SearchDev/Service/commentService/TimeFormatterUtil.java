package com.searchDev.SearchDev.Service.commentService;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TimeFormatterUtil {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM d, h:mm a", Locale.ENGLISH);

    public static String format(Instant instant , ZoneId zoneId){
        return instant.atZone(zoneId)
                      .format(FORMATTER);
    }
}
