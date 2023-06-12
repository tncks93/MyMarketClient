package com.project_bong.mymarket.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationBarView;
import com.project_bong.mymarket.R;
import com.project_bong.mymarket.chat.ChatFragment;
import com.project_bong.mymarket.databinding.ActivityMainBinding;
import com.project_bong.mymarket.dto.LoginUser;
import com.project_bong.mymarket.home.HomeFragment;
import com.project_bong.mymarket.my_page.MyPageFragment;
import com.project_bong.mymarket.retrofit.RetrofitClientInstance;
import com.project_bong.mymarket.sale.SaleActivity;
import com.project_bong.mymarket.util.LoginUserGetter;

import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

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

        setFragmentManager();






    }

    private void setFragmentManager(){
        homeFragment = new HomeFragment();
        chatFragment = new ChatFragment();
        myPageFragment = new MyPageFragment();
        fm = getSupportFragmentManager();

        fm.beginTransaction().replace(containerId,homeFragment).commit();

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



    }

    private void showFragment(Fragment fragment){
        for(Fragment frag:fm.getFragments()){
            fm.beginTransaction().hide(frag).commit();
        }
        fm.beginTransaction().remove(chatFragment).commit();

            if(fragment.isAdded()){
                fm.beginTransaction().show(fragment).commit();
            }else{
                fm.beginTransaction().add(containerId,fragment).show(fragment).commit();
            }



    }
}