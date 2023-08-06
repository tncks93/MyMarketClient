package com.project_bong.mymarket.dto;

import com.google.gson.annotations.SerializedName;

public class InterestingGoods extends Goods{

    @SerializedName("saved_at")
    private String saved_at;

    @SerializedName("is_interest")
    private boolean isInterest;

    public String getSaved_at(){
        return this.saved_at;
    }

    public boolean isInterest() {
        return isInterest;
    }

    public void setInterest(boolean interest) {
        isInterest = interest;
    }

    private InterestingGoods(String name, String category, int price, String details) {
        super(name, category, price, details);
    }

}
