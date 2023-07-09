package com.project_bong.mymarket.util;

import android.util.Log;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

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

    public String getTime(String utcTime){
        final String timeFormat = "a hh:mm";
        LocalDateTime localDateTime = convertUTCtoLocal(utcTime);
        return localDateTime.format(DateTimeFormatter.ofPattern(timeFormat)).toString();
    }

    public boolean isDifferentDay(String before,String next){
        LocalDateTime beforeDate = convertUTCtoLocal(before);
        LocalDateTime nextDate = convertUTCtoLocal(next);

        if(beforeDate.toLocalDate().isBefore(nextDate.toLocalDate())){
            return true;
        }else{
            return false;
        }
    }

    public String getDateForChatDivider(String utc){
        LocalDateTime dateTime = convertUTCtoLocal(utc);
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));
    }
}
