package com.project_bong.mymarket.login;

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
import com.project_bong.mymarket.databinding.ActivitySignUpBinding;
import com.project_bong.mymarket.dto.LoginUser;
import com.project_bong.mymarket.main.MainActivity;
import com.project_bong.mymarket.retrofit.RetrofitClientInstance;
import com.project_bong.mymarket.retrofit.RetrofitInterface;
import com.project_bong.mymarket.util.ImageProcessor;
import com.project_bong.mymarket.util.LoginUserGetter;
import com.project_bong.mymarket.util.PermissionsGetter;
import com.project_bong.mymarket.util.Shared;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {
    private String phoneNum;
    private ActivitySignUpBinding binding;
    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_MEDIA_IMAGES};

    private boolean isDefaultImage = true;
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
                        Glide.with(getBaseContext()).load(currentImagePath).circleCrop().into(binding.imgUserProfileSignUp);

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
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        phoneNum = getIntent().getStringExtra("phone");
        Glide.with(getBaseContext()).load(ResourcesCompat.getDrawable(getResources(),R.drawable.user_default,null)).circleCrop().into(binding.imgUserProfileSignUp);
        binding.txtUserPhoneSignUp.setText(phoneNum);
        setSupportActionBar(binding.toolbarSignUp.toolbarDefault);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        binding.toolbarSignUp.toolbarTitle.setText(getString(R.string.str_title_sign_up));


        //프로필 이미지 수정
        binding.imgUserProfileSignUp.setOnClickListener(v->{
            showProfileImageDialog();
        });
    }

    private void startSignUp(){
        binding.progressSignUp.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        JsonObject userJson = new JsonObject();
        userJson.addProperty("phone",phoneNum);
        userJson.addProperty("name",binding.editUserNameSignUp.getText().toString());
        RequestBody bodyUser = RequestBody.create(userJson.toString(), MediaType.parse("application/json; charset=utf-8"));
        RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(getBaseContext()).create(RetrofitInterface.class);
        Call<String> callSignUp;

        if(!isDefaultImage){
            File imageFile = new File(currentImagePath);
            RequestBody imageBody = RequestBody.create(imageFile,MediaType.parse("image/jpeg"));
            String fileName = "user_"+System.currentTimeMillis()+".jpeg";
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image",fileName,imageBody);

            callSignUp = retrofit.callSignUp(bodyUser,imagePart);
        }else{
            callSignUp = retrofit.callSignUp(bodyUser,null);
        }

        callSignUp.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.body() != null){
                    String res = response.body();
                    if(res.equals("success")){
                        LoginUserGetter loginUserGetter = new LoginUserGetter(getBaseContext());
                        loginUserGetter.setOnLoginListener(new LoginUserGetter.OnLoginListener() {
                            @Override
                            public void onLogin(LoginUser loginUser) {
                                if(loginUser == null){
                                    Toast.makeText(getBaseContext(),getString(R.string.failure_on_network),Toast.LENGTH_SHORT).show();
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                }
                                Log.d("login","user_phone : "+loginUser.getPhoneNum());
                                Log.d("login","user_name : "+loginUser.getName());
                                Log.d("login","user_sess : "+loginUser.getSessId());
                                Shared shared = new Shared(getBaseContext(),Shared.LOGIN_FILE_NAME);
                                shared.setSharedString(Shared.LOGIN_KEY,loginUser.getSessId());
                                RetrofitClientInstance.initiate();
                                Toast.makeText(getBaseContext(),getString(R.string.str_success_sign_up),Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getBaseContext(), MainActivity.class));
                                finish();

                            }
                            @Override
                            public void onLoginFailed() {
                                Toast.makeText(getBaseContext(),getString(R.string.failure_on_network),Toast.LENGTH_SHORT).show();
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            }
                        });

                        loginUserGetter.callLoginUser(phoneNum,true);
                    }else{
                        Log.d("res","response : "+response.body());
                    }
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                RetrofitClientInstance.setOnFailure(getBaseContext(),t);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });

    }

    private void showProfileImageDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
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
                        Glide.with(getBaseContext()).load(ResourcesCompat.getDrawable(getResources(),R.drawable.user_default,null)).circleCrop().into(binding.imgUserProfileSignUp);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(getBaseContext());
        inflater.inflate(R.menu.menu_sign_up,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()== R.id.menu_action_sign_up){
            //가입하기 버튼
            if(isFilledForm()){
                startSignUp();
            }
        }
        return super.onOptionsItemSelected(item);
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

    private boolean isFilledForm(){
        if(binding.editUserNameSignUp.getText().toString().replace(" ","").equals("")){
            return false;
        }
        return true;
    }
}