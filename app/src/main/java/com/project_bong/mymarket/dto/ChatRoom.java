package com.project_bong.mymarket.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ChatRoom {

    @SerializedName("room_id")
    private int roomId;

    @SerializedName("op_name")
    private String opName;

    @SerializedName("op_image")
    private String opImage;

    @SerializedName("goods_image")
    @Expose
    private String goodsImage;

    @SerializedName("updated_at")
    private String updatedAt;

    public int getRoomId() {
        return roomId;
    }

    public String getOpName() {
        return opName;
    }

    public String getOpImage() {
        return opImage;
    }

    public String getGoodsImage() {
        return goodsImage;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
