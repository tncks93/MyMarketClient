package com.project_bong.mymarket.util;

import android.util.Log;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeConverter {
    private final String format = "yyyy-MM-dd HH:mm:ss.SSSSSS";
    private ZoneId zoneId;
    private ZoneId zoneUTC;

    public TimeConverter(){
        zoneId = ZoneId.systemDefault();
        zoneUTC = ZoneId.of("UTC");
    }

    public String getUTC(){
        Instant instant = Instant.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        String dateTime = instant.atZone(zoneUTC).format(formatter);

        return dateTime;
    }

    public LocalDateTime convertUTCtoLocal(String utcTime){
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(format);
        ZonedDateTime zonedLocal = LocalDateTime.parse(utcTime,timeFormatter).atZone(zoneUTC).withZoneSameInstant(zoneId);

        return zonedLocal.toLocalDateTime();
    }

    public String formatLocal(LocalDateTime localDateTime, String pattern){

        return localDateTime.format(DateTimeFormatter.ofPattern(pattern)).toString();
    }
}
