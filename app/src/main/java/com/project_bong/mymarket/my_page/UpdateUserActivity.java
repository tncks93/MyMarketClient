package com.project_bong.mymarket.my_page;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.project_bong.mymarket.R;
import com.project_bong.mymarket.databinding.ActivityUpdateUserBinding;
import com.project_bong.mymarket.dto.LoginUser;
import com.project_bong.mymarket.login.SignUpActivity;
import com.project_bong.mymarket.retrofit.RetrofitClientInstance;
import com.project_bong.mymarket.retrofit.RetrofitInterface;
import com.project_bong.mymarket.util.ImageProcessor;
import com.project_bong.mymarket.util.LoginUserGetter;
import com.project_bong.mymarket.util.PermissionsGetter;
import com.project_bong.mymarket.util.Shared;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateUserActivity extends AppCompatActivity {
    private ActivityUpdateUserBinding binding;

    private final String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_MEDIA_IMAGES};

    private boolean isDefaultImage = false;
    private String currentImagePath = null;

    private ActivityResultLauncher<String[]> requestStoragePermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
        @Override
        public void onActivityResult(Map<String, Boolean> result) {
            boolean isAllGranted = true;
            for(boolean isGranted: result.values()){
                if(!isGranted){
                    isAllGranted = false;
                    break;
                }
            }

            if(isAllGranted){
                //앨범 선택 진행
                startGettingAlbum();

            }

        }
    });

    private ActivityResultLauncher<Intent> getPicture = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result != null){
                try{
                    isDefaultImage = false;
                    if(result.getData().getData() != null){
                        Uri uri = result.getData().getData();
                        ImageProcessor ip = new ImageProcessor(getBaseContext());
                        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
                        String fileName = "user_"+timeStamp;

                        String imgPath = ip.saveJPEGFromSingleBitmap(ip.getBitmapFromContentUri(uri),fileName);
                        if(currentImagePath != null){
                            ip.deleteFile(currentImagePath);
                        }

                        currentImagePath = imgPath;
                        Glide.with(getBaseContext()).load(currentImagePath).circleCrop().into(binding.imgUserProfileUpdateUser);

                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }

        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarUpdateUser.toolbarDefault);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.toolbarUpdateUser.toolbarTitle.setText(getString(R.string.str_title_update_goods));

        binding.imgUserProfileUpdateUser.setOnClickListener(v->{
            showProfileImageDialog();
        });
        setUserInfo();


    }

    private void setUserInfo(){
            LoginUser loginUser = LoginUserGetter.getLoginUser();
            new Thread(()->{
                try{
                    URL url = new URL(loginUser.getUserImage());
                    Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    ImageProcessor ip = new ImageProcessor(getBaseContext());
                    String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
                    String fileName = "user_"+timeStamp;
                    currentImagePath = ip.saveJPEGFromSingleBitmap(bitmap,fileName);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(getBaseContext()).load(currentImagePath).circleCrop().circleCrop()
                                    .into(binding.imgUserProfileUpdateUser);
                        }
                    });


                }catch (Exception e){
                    e.printStackTrace();
                }

            }).start();
            binding.txtUserPhoneUpdateUser.setText(loginUser.getPhoneNum());
            binding.editUserNameUpdateUser.setText(loginUser.getName());


    }

    private void saveUserInfo(){
        String name = binding.editUserNameUpdateUser.getText().toString();
        if(!isFilledForm(name)){
            Toast.makeText(getBaseContext(),getString(R.string.str_hint_input_name),Toast.LENGTH_SHORT).show();
            return;
        }
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//        binding.progressUpdateUser.setVisibility(View.VISIBLE);

        JsonObject userJson = new JsonObject();
        userJson.addProperty("name",name);
        RequestBody userBody = RequestBody.create(userJson.toString(), MediaType.parse("application/json; charset=utf-8"));
        RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(getBaseContext()).create(RetrofitInterface.class);
        Call<String> callUpdateUser;
        if(isDefaultImage){
            callUpdateUser = retrofit.callUpdateUser(userBody,null);
        }else{

            File imageFile = new File(currentImagePath);
            RequestBody imageBody = RequestBody.create(imageFile, MediaType.parse("image/jpeg"));
            String fileName = "user_"+System.currentTimeMillis()+".jpeg";
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image",fileName,imageBody);
            callUpdateUser = retrofit.callUpdateUser(userBody,imagePart);
        }

        callUpdateUser.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.body() != null){
                    Log.d("res",response.body());
                    LoginUserGetter loginUserGetter = new LoginUserGetter(getBaseContext());
                    loginUserGetter.setOnLoginListener(new LoginUserGetter.OnLoginListener() {
                        @Override
                        public void onLogin(LoginUser loginUser) {
                            Shared shared = new Shared(getBaseContext(),Shared.LOGIN_FILE_NAME);
                            shared.setSharedString(Shared.LOGIN_KEY,loginUser.getSessId());
                            setResult(RESULT_OK,new Intent());
                            finish();
                        }

                        @Override
                        public void onLoginFailed() {
                            finish();
                        }
                    });
                    loginUserGetter.callLoginUser(LoginUserGetter.getLoginUser().getSessId(),false);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                RetrofitClientInstance.setOnFailure(getBaseContext(),t);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                binding.progressUpdateUser.setVisibility(View.GONE);
            }
        });
    }

    private boolean isFilledForm(String name){
        if(name.replaceAll(" ","").equals("")){
            return false;
        }else{
            return true;
        }
    }

    private void showProfileImageDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(UpdateUserActivity.this);
        builder.setItems(getResources().getStringArray(R.array.str_dialog_for_profile_img), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //0:앨범
                //1:기본 이미지
                switch (i) {
                    case 0:
                        getAlbumImage();

                        break;
                    case 1:
                        isDefaultImage = true;
                        Glide.with(getBaseContext()).load(ResourcesCompat.getDrawable(getResources(),R.drawable.user_default,null))
                                .circleCrop().into(binding.imgUserProfileUpdateUser);
                        //기존 이미지 있을 경우 삭제
                        if(currentImagePath!=null){
                            ImageProcessor ip = new ImageProcessor(getBaseContext());
                            ip.deleteFile(currentImagePath);
                            currentImagePath = null;
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        builder.show();
    }

    private void getAlbumImage(){
        boolean isNotGrantedPermissions = isNeedPermissions();
        if(!isNotGrantedPermissions){
            startGettingAlbum();
        }
    }

    private void startGettingAlbum(){
        Intent albumIntent = new Intent();
        albumIntent.setAction(Intent.ACTION_GET_CONTENT);
        albumIntent.setType("image/jpeg");
        getPicture.launch(Intent.createChooser(albumIntent,"앨범 선택"));

    }

    private boolean isNeedPermissions(){
        PermissionsGetter permissionsGetter = new PermissionsGetter(getBaseContext());
        if(permissionsGetter.haveDeniedPermissions(permissions)){
            String[] deniedPermissions = permissionsGetter.getPermissions();
            if(deniedPermissions != null){
                requestStoragePermissions.launch(deniedPermissions);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(getBaseContext());
        inflater.inflate(R.menu.menu_update_user,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_update_user:
                saveUserInfo();
                return true;
            case android.R.id.home:
                finish();
                return true;

            default:
                return false;
        }
    }
}