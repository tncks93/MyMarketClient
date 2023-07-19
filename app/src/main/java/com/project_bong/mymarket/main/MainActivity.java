package com.project_bong.mymarket.main;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.project_bong.mymarket.R;
import com.project_bong.mymarket.chat.ChatFragment;
import com.project_bong.mymarket.databinding.ActivityMainBinding;
import com.project_bong.mymarket.dto.LoginUser;
import com.project_bong.mymarket.home.HomeFragment;
import com.project_bong.mymarket.my_page.MyPageFragment;
import com.project_bong.mymarket.retrofit.RetrofitClientInstance;
import com.project_bong.mymarket.retrofit.RetrofitInterface;
import com.project_bong.mymarket.sale.SaleActivity;
import com.project_bong.mymarket.util.LoginUserGetter;

import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted->{
        Log.d("fcm","알림 권한 허용 유무 : "+isGranted);
    });

    private HomeFragment homeFragment;
    private ChatFragment chatFragment;
    private MyPageFragment myPageFragment;
    private FragmentManager fm;
    private final int containerId = R.id.container_main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        RetrofitClientInstance.initiate();

        askNotificationPermission();
        saveFCMToken();

        setFragmentManager(getIntent().getIntExtra("fragmentId",0));

    }

    private void setFragmentManager(int firstFragIndex){
        homeFragment = new HomeFragment();
        chatFragment = new ChatFragment();
        myPageFragment = new MyPageFragment();
        fm = getSupportFragmentManager();

        binding.bottomNavMain.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.menu_action_home:
                        showFragment(homeFragment);

                        return true;

                    case R.id.menu_action_sale:
                        Intent intent = new Intent(getBaseContext(), SaleActivity.class);
                        intent.putExtra("editMode","create");
                        startActivity(intent);
                        return false;

                    case R.id.menu_action_chat:
                        showFragment(chatFragment);
                        return true;

                    case R.id.menu_action_my_page:
                        showFragment(myPageFragment);
                        return true;

                    default:
                        return false;
                }

            }
        });

//        fm.beginTransaction().replace(containerId,chatFragment).commit();
        if(firstFragIndex == 3){
            binding.bottomNavMain.setSelectedItemId(R.id.menu_action_chat);
        }else{
            binding.bottomNavMain.setSelectedItemId(R.id.menu_action_home);
        }


    }

    private void showFragment(Fragment fragment){
        for(Fragment frag:fm.getFragments()){
            fm.beginTransaction().hide(frag).commit();
        }
        fm.beginTransaction().remove(chatFragment).commit();

        if(fragment.isAdded()){
            Log.d("frag","fragAdded : "+fragment.getId());
            fm.beginTransaction().show(fragment).commit();
        }else{
            Log.d("frag","fragNotAdded : ");
            fm.beginTransaction().add(containerId,fragment).show(fragment).commit();
//            fm.beginTransaction().replace(containerId,fragment).show(fragment).commit();
        }
    }

    private void askNotificationPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED){
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void saveFCMToken(){
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if(!task.isSuccessful()){
                    Log.d("fcm","getToken 실패 : "+task.getException());
                    return;
                }

                String token = task.getResult();

                Log.d("fcm","getToken 성공 : "+token);

                RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(getBaseContext())
                        .create(RetrofitInterface.class);
                Call<String> callSaveFcmToken = retrofit.callSaveFcmToken(token);
                callSaveFcmToken.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if(response.body() != null){
                            String res = response.body();
                            if(res.equals("success")){
                                Log.d("fcm","token Save 성공");        
                            }else{
                                Log.d("fcm","token Save 실패");
                            }
                        }
                        
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.d("fcm","tokenSave 오류 : "+t.toString());
                    }
                });
            }
        });
    }
}