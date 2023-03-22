package com.project_bong.mymarket.dto;

public class Category {
    private int categoryId;
    private int drawableId;

    public Category(int categoryId,int drawableId){
        this.categoryId = categoryId;
        this.drawableId = drawableId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public int getDrawableId() {
        return drawableId;
    }
}
