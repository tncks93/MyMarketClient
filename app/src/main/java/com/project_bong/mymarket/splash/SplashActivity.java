package com.project_bong.mymarket.splash;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.TaskStackBuilder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.project_bong.mymarket.R;
import com.project_bong.mymarket.chat.ChatRoomActivity;
import com.project_bong.mymarket.dto.LoginUser;
import com.project_bong.mymarket.login.AuthActivity;
import com.project_bong.mymarket.main.MainActivity;
import com.project_bong.mymarket.util.LoginUserGetter;
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

                    String launchMode = getIntent().getStringExtra("launchMode");
                    if(launchMode != null && launchMode.equals("CHAT")){
                        startChatActivity(getIntent());
                    }else{
                        startMainActivity();
                    }

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

    private void startChatActivity(Intent intent){
        int roomId = intent.getIntExtra("roomId",0);
        String opName = intent.getStringExtra("opName");

        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.putExtra("fragmentId",3);

        Intent chatIntent = new Intent(this, ChatRoomActivity.class);
        chatIntent.putExtra("roomId",roomId);
        chatIntent.putExtra("opName",opName);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getBaseContext());
        stackBuilder.addNextIntentWithParentStack(mainIntent);
        stackBuilder.addNextIntent(chatIntent);

        stackBuilder.startActivities();
        finish();
    }
}