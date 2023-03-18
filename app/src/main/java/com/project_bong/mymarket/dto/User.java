package com.project_bong.mymarket.dto;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("user_id")
    private int id;

    @SerializedName("user_image")
    private String userImage;

    @SerializedName("name")
    private String name;

    public int getId() {
        return id;
    }

    public String getUserImage() {
        return userImage;
    }

    public String getName() {
        return name;
    }
}
