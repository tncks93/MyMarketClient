package com.project_bong.mymarket.splash;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.project_bong.mymarket.R;
import com.project_bong.mymarket.dto.LoginUser;
import com.project_bong.mymarket.login.AuthActivity;
import com.project_bong.mymarket.main.MainActivity;
import com.project_bong.mymarket.util.LoginUserGetter;
import com.project_bong.mymarket.util.PermissionsGetter;
import com.project_bong.mymarket.util.Shared;

public class SplashActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Shared shared = new Shared(getBaseContext(),Shared.LOGIN_FILE_NAME);
        String loginKey = shared.getSharedString(Shared.LOGIN_KEY);

        if(loginKey != null){
            LoginUserGetter loginUserGetter = new LoginUserGetter(getBaseContext());
            loginUserGetter.setOnLoginListener(new LoginUserGetter.OnLoginListener() {
                @Override
                public void onLogin(LoginUser loginUser) {
                    if(loginUser == null){
                        shared.deleteString(Shared.LOGIN_KEY);
                        startAuthActivity();
                        return;
                    }
                    shared.setSharedString(Shared.LOGIN_KEY,loginUser.getSessId());
                    startMainActivity();

                }

                @Override
                public void onLoginFailed() {
                    Toast.makeText(getBaseContext(),getString(R.string.failure_on_network),Toast.LENGTH_SHORT).show();
                    shared.deleteString(Shared.LOGIN_KEY);
                    startAuthActivity();
                }
            });

            loginUserGetter.callLoginUser(loginKey,false);

        }else{
            //로그인 키 없을 시 휴대폰 인증 화면 이동
            startAuthActivity();
        }
    }

    private void startAuthActivity(){
        startActivity(new Intent(getBaseContext(),AuthActivity.class));
        finish();
    }

    private void startMainActivity(){

        startActivity(new Intent(getBaseContext(), MainActivity.class));
        finish();
    }
}