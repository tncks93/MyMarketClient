package com.project_bong.mymarket.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ChatMessage {
    public static final String TYPE_TEXT = "text";


    @SerializedName("chat_id")
    @Expose
    private int chatId;

    @SerializedName("op_image")
    @Expose
    private String opImage;

    @SerializedName("from_id")
    private int fromId;

    @SerializedName("to_id")
    private int toId;

    @SerializedName("msg_type")
    private String msgType;

    @SerializedName("content")
    private String content;

    @SerializedName("is_read")
    private int isRead;

    public ChatMessage(int fromId,String msgType,String content,int isRead){
        this.fromId = fromId;
        this.msgType = msgType;
        this.content = content;
        this.isRead = isRead;

    }

    @SerializedName("sent_at")
    private String sentAt;

    public int getChatId() {
        return chatId;
    }

    public String getOpImage(){
        return opImage;
    }

    public int getFromId() {
        return fromId;
    }

    public int getToId() {
        return toId;
    }

    public String getMsgType() {
        return msgType;
    }

    public String getContent() {
        return content;
    }

    public int getIsRead() {
        return isRead;
    }

    public String getSentAt() {
        return sentAt;
    }
}
