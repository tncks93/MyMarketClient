package com.project_bong.mymarket.util;

import android.content.Context;

import com.project_bong.mymarket.dto.LoginUser;
import com.project_bong.mymarket.retrofit.RetrofitClientInstance;
import com.project_bong.mymarket.retrofit.RetrofitInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginUserGetter {
    private static LoginUser loginUser;
    private Context mContext;

    public LoginUserGetter(Context context){
        this.mContext = context;
    }

    public interface OnLoginListener{
        void onLogin(LoginUser loginUser);
        void onLoginFailed();
    }

    private OnLoginListener loginListener = null;

    public void setOnLoginListener(OnLoginListener listener){
        this.loginListener = listener;
    }

    public static LoginUser getLoginUser(){
        return loginUser;
    }

    public void callLoginUser(String id, boolean isFirst){
        RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(mContext).create(RetrofitInterface.class);
        Call<LoginUser> callLoginUser = retrofit.callLoginUser(id,isFirst);
        callLoginUser.enqueue(new Callback<LoginUser>() {
            @Override
            public void onResponse(Call<LoginUser> call, Response<LoginUser> response) {
                    setLoginUser(response.body());
                    if(loginListener != null){
                        loginListener.onLogin(response.body());
                    }

            }

            @Override
            public void onFailure(Call<LoginUser> call, Throwable t) {
                if(loginListener != null){
                    loginListener.onLoginFailed();
                }

            }
        });
    }

    private void setLoginUser(LoginUser loginUser) {
        LoginUserGetter.loginUser = loginUser;
    }
}



