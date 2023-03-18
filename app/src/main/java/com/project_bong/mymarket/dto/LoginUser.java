package com.project_bong.mymarket.dto;

import com.google.gson.annotations.SerializedName;

public class LoginUser extends User{

    @SerializedName("phone")
    private String phoneNum;

    @SerializedName("sess_id")
    private String sessId;

    public String getPhoneNum() {
        return phoneNum;
    }

    public String getSessId(){ return sessId;}
}
