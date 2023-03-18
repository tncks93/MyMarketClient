package com.project_bong.mymarket.util;

import android.app.Activity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

public class Loading {
    Activity activity;
    ProgressBar progressBar;

    public Loading(Activity activity,ProgressBar progressBar){
        this.activity = activity;
        this.progressBar = progressBar;
    }

    public void setNotTouchable(){
        progressBar.setVisibility(View.VISIBLE);
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void clearNotTouchable(){
        progressBar.setVisibility(View.GONE);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}
