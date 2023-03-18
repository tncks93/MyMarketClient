package com.project_bong.mymarket.login;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.project_bong.mymarket.R;
import com.project_bong.mymarket.databinding.ActivityAuthBinding;
import com.project_bong.mymarket.dto.LoginUser;
import com.project_bong.mymarket.main.MainActivity;
import com.project_bong.mymarket.retrofit.RetrofitClientInstance;
import com.project_bong.mymarket.retrofit.RetrofitInterface;
import com.project_bong.mymarket.util.LoginUserGetter;
import com.project_bong.mymarket.util.Shared;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthActivity extends AppCompatActivity {
    private ActivityAuthBinding binding;
    private BroadcastReceiver smsReceiver;

    private ActivityResultLauncher<String[]> requestSmsPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
        @Override
        public void onActivityResult(Map<String, Boolean> result) {
            //permissions 요청 결과 처리
            if(result.get(Manifest.permission.SEND_SMS) != null){
                if(!result.get(Manifest.permission.SEND_SMS)){
                    finish();
                }
            }
        }
    });

    private String authNum;
    private String phoneNum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String[] permissionList = getSmsPermissions();

        requestSmsPermissionLauncher.launch(permissionList);

        setSmsReceiver();


        //인증번호 전송
        binding.btnGetAuthNum.setOnClickListener(v ->{
            String inputPhoneNum = binding.editInputPhoneNumAuth.getText().toString().replace(" ","");
            if(inputPhoneNum.equals("")){
                Toast.makeText(getBaseContext(),getString(R.string.str_hint_input_phone_num),Toast.LENGTH_SHORT).show();
                binding.layoutInputAuthAuth.setVisibility(View.GONE);
                return;
            }
            phoneNum = binding.editInputPhoneNumAuth.getText().toString();
            binding.btnGetAuthNum.setText(getString(R.string.str_re_send_auth_num));
            binding.layoutInputAuthAuth.setVisibility(View.VISIBLE);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendSMS(phoneNum);
                }
            },500);
        });


        binding.btnConfirmAuthNum.setOnClickListener(v->{
            //인증번호 확인
            Log.d("confirm","clicked");
            boolean isThisPhone = binding.editInputPhoneNumAuth.getText().toString().equals(phoneNum);
            boolean isAuthNum = binding.editInputAuthNumAuth.getText().toString().equals(authNum);

            if(isThisPhone && isAuthNum){
                Log.d("auth","인증완료");
                RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(getBaseContext()).create(RetrofitInterface.class);
                Call<String> callConfirmUser = retrofit.callConfirmUser(phoneNum);
                callConfirmUser.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if(response.body() != null){
                            String res = response.body();
                            Log.d("res","res : "+res);

                            if(res.equals("yes")){
                                //폰 번호로 로그인 진행
                                LoginUserGetter userGetter = new LoginUserGetter(getBaseContext());
                                userGetter.setOnLoginListener(new LoginUserGetter.OnLoginListener() {
                                    @Override
                                    public void onLogin(LoginUser loginUser) {
                                        if(loginUser != null){
                                            String sessId = LoginUserGetter.getLoginUser().getSessId();
                                            String phone = LoginUserGetter.getLoginUser().getPhoneNum();

                                            Log.d("userInfo","sess : "+sessId+" phone : "+phone);

                                            //MainActivity로 진행행
                                            Shared shared = new Shared(getBaseContext(),Shared.LOGIN_FILE_NAME);
                                            shared.setSharedString(Shared.LOGIN_KEY,sessId);
                                            startActivity(new Intent(getBaseContext(), MainActivity.class));
                                            finish();
                                        }else{
                                            Toast.makeText(getBaseContext(),getString(R.string.failure_on_network),Toast.LENGTH_SHORT).show();
                                            finish();
                                        }

                                    }

                                    @Override
                                    public void onLoginFailed() {
                                        Toast.makeText(getBaseContext(),getString(R.string.failure_on_network),Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });

                                userGetter.callLoginUser(phoneNum,true);

                            }else if(res.equals("no")){
                                //회원 가입 진행
                                Intent intent = new Intent(getBaseContext(),SignUpActivity.class);
                                intent.putExtra("phone",phoneNum);
                                startActivity(intent);
                                finish();

                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        RetrofitClientInstance.setOnFailure(getBaseContext(),t);
                    }
                });
            }
        });

    }

    private String[] getSmsPermissions(){
        ArrayList<String> permissionList = new ArrayList<>();

        if(ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_DENIED){
            permissionList.add(Manifest.permission.READ_PHONE_NUMBERS);
        }

        if(ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }

        if(ContextCompat.checkSelfPermission(getBaseContext(),Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED){
            permissionList.add(Manifest.permission.SEND_SMS);
        }
        if(ContextCompat.checkSelfPermission(getBaseContext(),Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_DENIED){
            permissionList.add(Manifest.permission.RECEIVE_SMS);
        }



        return permissionList.toArray(new String[permissionList.size()]);


    }

    private void setSmsReceiver(){
        smsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("sms","sms도착");
                SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                Log.d("msg","messages.length : "+messages.length);
                for(int i=0;i<messages.length;i++){
                    SmsMessage smsMessage = messages[i];
                    setMessage(smsMessage);
                }
            }
        };

        registerReceiver(smsReceiver,new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
    }

    private void sendSMS(String phoneNum) {
        authNum = genNum();
        String message = "마이마켓 가입 인증번호 ["+authNum+"]";

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNum,null,message,null,null);
    }

    //인증번호 생성
    private String genNum(){
        Random random = new Random();
        String ranNum = "";

        for(int i =0 ; i < 6;i++){
            String ran = Integer.toString(random.nextInt(10));
            ranNum += ran;
        }

        return ranNum;
    }

    @SuppressLint("MissingPermission")
    private void setMessage(SmsMessage smsMessage) {
        String TAG = "smsReceived";
        Log.d(TAG, "DisplayMEssageBody :: " + smsMessage.getDisplayMessageBody());
        Log.d(TAG, "DisplayOrinatingAddress" +  smsMessage.getDisplayOriginatingAddress());
        Log.d(TAG, "EmailBody :: " +  smsMessage.getEmailBody());
        Log.d(TAG, "originatingAddress :: " +  smsMessage.getOriginatingAddress());
        Log.d(TAG, "MessageBody :: " +  smsMessage.getMessageBody());
        Log.d(TAG, "serviceCenterAddress :: " +  smsMessage.getServiceCenterAddress());
        Log.d(TAG, "time :: " +  smsMessage.getTimestampMillis());


        String message = smsMessage.getMessageBody();
        String msgNum = message.replaceAll("\\D", "");
        Log.d(TAG, "msgNum : " + msgNum);
        binding.editInputAuthNumAuth.setText(msgNum);
        binding.btnConfirmAuthNum.performClick();




    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(smsReceiver);
        super.onDestroy();
    }
}