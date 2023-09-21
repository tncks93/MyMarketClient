package com.project_bong.mymarket.util;

import android.content.Context;
import android.content.SharedPreferences;

public class Shared {
    private SharedPreferences pref;
    private Context mContext;

    public static final String LOGIN_FILE_NAME = "Login";
    public static final String BOOK_MARK_FILE_NAME = "BookMark";
    public static final String WALLET_SOURCE_FILE_NAME = "Wallet";
    public static final String LOGIN_KEY = "loginKey";
    public static final String WALLET_NAME_KEY = "walletName";

    public Shared(Context context,String fileName){
        mContext = context;
        pref = mContext.getSharedPreferences(fileName,Context.MODE_PRIVATE);
    }

    public String getSharedString(String key) {
//        pref = mContext.getSharedPreferences(fileName,Context.MODE_PRIVATE);
        String result = pref.getString(key,null);
        return result;
    }

    public void setSharedString(String key,String data){
//        pref = mContext.getSharedPreferences(fileName,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key,data);
        editor.apply();

    }

    public void deleteString(String key){
//        pref = mContext.getSharedPreferences(fileName,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(key);
        editor.apply();
    }

    public void changeFile(String fileName){
        this.pref = mContext.getSharedPreferences(fileName,Context.MODE_PRIVATE);
    }
}
