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

    @SerializedName("last_msg")
    private String lastMessage;

    @SerializedName("last_msg_type")
    private String lastMessageType;

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

    public String getLastMessage(){
        return lastMessage;
    }

    public String getLastMessageType(){
        return lastMessageType;
    }
    public String getUpdatedAt() {
        return updatedAt;
    }
}
