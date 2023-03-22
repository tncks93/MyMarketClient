package com.project_bong.mymarket.dto;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class SearchFilter {
    public final static String FILTER_NONE = "none";
    public final int DEFAULT_MIN_PRICE = 0;
    public final int DEFAULT_MAX_PRICE = 100000000;

    @SerializedName("category")
    private String category;

    @SerializedName("keyword")
    private String keyword;

    @SerializedName("min_price")
    private int minPrice;

    @SerializedName("max_price")
    private int maxPrice;

    public SearchFilter(String category,String keyword){
        this.category = category;
        this.keyword = keyword;
        this.minPrice = DEFAULT_MIN_PRICE;
        this.maxPrice = DEFAULT_MAX_PRICE;
    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public int getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(int minPrice) {
        this.minPrice = minPrice;
    }

    public int getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(int maxPrice) {
        this.maxPrice = maxPrice;
    }


}
