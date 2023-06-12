package com.project_bong.mymarket.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeConverter {


    public String getUTC(){
        Instant instant = Instant.now();

        ZoneId zone = ZoneId.of("UTC");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        String dateTime = instant.atZone(zone).format(formatter);

        return dateTime;
    }
}
