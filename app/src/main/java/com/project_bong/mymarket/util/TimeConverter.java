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

    public String convertUTCtoLocal(String utcTime){
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(format);
        LocalDateTime dateTime = LocalDateTime.parse(utcTime,timeFormatter);
        ZonedDateTime zonedDateTime = dateTime.atZone(zoneUTC);
//        zonedDateTime.withZoneSameInstant(zoneId);
        ZonedDateTime zonedLocal = zonedDateTime.withZoneSameInstant(zoneId);
        int hour = zonedLocal.getHour();
        int minute = zonedLocal.getMinute();
        Log.d("chat","hour : "+hour+ " minute : "+minute);




        return zonedDateTime.withZoneSameInstant(zoneId).toString();
    }
}
