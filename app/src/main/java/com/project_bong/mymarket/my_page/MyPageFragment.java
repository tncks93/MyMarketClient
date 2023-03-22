package com.project_bong.mymarket.my_page;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.project_bong.mymarket.R;
import com.project_bong.mymarket.databinding.FragmentMyPageBinding;
import com.project_bong.mymarket.dto.LoginUser;
import com.project_bong.mymarket.util.LoginUserGetter;

public class MyPageFragment extends Fragment {
    private FragmentMyPageBinding binding;
    private ActivityResultLauncher<Intent> getResultFromUpdatingUser = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            setUserProfile();
        }
    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyPageBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //유저프로필 세팅
        binding.toolbarMyPage.toolbarTitle.setText(getString(R.string.str_title_my_page));

        setUserProfile();
        binding.layoutUserProfileMyPage.setOnClickListener(v->{
            //유저 프로필 수정
            Intent intent = new Intent(getActivity(),UpdateUserActivity.class);
            getResultFromUpdatingUser.launch(intent);
        });
    }

    private void setUserProfile(){
        Log.d("profile","setUserProfile() 호출됨");
        LoginUser loginUser = LoginUserGetter.getLoginUser();
        Glide.with(getActivity()).load(loginUser.getUserImage()).centerCrop().circleCrop().into(binding.imgUserMyPage);
        binding.txtNameUserMyPage.setText(loginUser.getName());
    }

    @Override
    public void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}
