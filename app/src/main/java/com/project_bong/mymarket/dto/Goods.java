package com.project_bong.mymarket.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class Goods {

    public static final String STATE_ON_SAIL = "판매중";
    public static final String STATE_RESERVED = "예약중";
    public static final String STATE_SOLD_OUT = "판매완료";

    @SerializedName("goods_id")
    int id;

    @Expose
    @SerializedName("seller")
    User seller;

    @SerializedName("name")
    String name;

    @SerializedName("category")
    String category;

    @SerializedName("price")
    String price;

    @SerializedName("details")
    String details;

    @Expose
    @SerializedName("main_image")
    String mainImage;


    @Expose
    @SerializedName("goods_images")
    ArrayList<String> goodsImageList;

    @SerializedName("created_at")
    String createdAt;

    @SerializedName("state")
    String state;

    public Goods(String name,String category,String price,String details){
        this.name = name;
        this.category = category;
        this.price = price;
        this.details = details;

    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public User getSeller(){return seller;}

    public String getCategory() {
        return category;
    }

    public String getPrice() {
        return price;
    }

    public String getDetails() {
        return details;
    }

    public String getMainImage() {
        return mainImage;
    }

    public ArrayList<String> getGoodsImageList() {
        return goodsImageList;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getState() {
        return state;
    }

    public String getCreatedAtForUser(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(this.createdAt,formatter);
        LocalDate date = dateTime.toLocalDate();
        LocalDate today = LocalDate.now();
        boolean isToday = today.isEqual(date);
        if(isToday){
            LocalTime todayTime = LocalTime.now();
            LocalTime time = dateTime.toLocalTime();

            if(ChronoUnit.HOURS.between(time,todayTime) >=1){
                return ChronoUnit.HOURS.between(time,todayTime)+"시간 전";
            }else if(ChronoUnit.MINUTES.between(time,todayTime) >= 1){
                return ChronoUnit.MINUTES.between(time,todayTime)+"분 전";
            }else{
                return ChronoUnit.SECONDS.between(time,todayTime)+"초 전";
            }


        }else{
            return date.toString();
        }

    }


}
