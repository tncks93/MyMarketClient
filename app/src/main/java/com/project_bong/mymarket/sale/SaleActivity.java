package com.project_bong.mymarket.sale;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.project_bong.mymarket.R;
import com.project_bong.mymarket.adapter.RegisterGoodsImageAdapter;
import com.project_bong.mymarket.databinding.ActivitySaleBinding;
import com.project_bong.mymarket.dto.Goods;
import com.project_bong.mymarket.retrofit.RetrofitClientInstance;
import com.project_bong.mymarket.retrofit.RetrofitInterface;
import com.project_bong.mymarket.util.ImageProcessor;
import com.project_bong.mymarket.util.ItemMoveCallback;
import com.project_bong.mymarket.util.PermissionsGetter;

import java.net.URL;
import java.text.DecimalFormat;
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

public class SaleActivity extends AppCompatActivity {
    private final String CREATE_MODE = "create";
    private final String UPDATE_MODE = "update";
    private String editMode;
    private int updateGoodsId;
    private ActivitySaleBinding binding;

    //이미지 recyclerview 변수들
    private RegisterGoodsImageAdapter imageAdapter;
    private ArrayList<String> imagePaths;

    private String price = "";


    //갤러리 권한
    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_MEDIA_IMAGES};
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

            if(!isAllGranted){
                finish();
            }
        }
    });
    private ActivityResultLauncher<Intent> getPictures = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getData() == null || result.getResultCode()!=RESULT_OK){
                return;
            }
            if(result.getData().getClipData() != null){
                //여러장 일때
                ClipData clipData = result.getData().getClipData();
                if(imagePaths.size() + clipData.getItemCount() > 10){
                    Toast.makeText(getBaseContext(),getString(R.string.str_too_many_images),Toast.LENGTH_SHORT).show();
                    return;
                }
                ArrayList<String> imagePaths = new ArrayList<>();
                Log.d("image","clipData.size : "+clipData.getItemCount());

                for(int i=0;i<clipData.getItemCount();i++){
                    try{
                        String path = getImagePath(clipData.getItemAt(i).getUri(),i);
                        imagePaths.add(path);


                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                imageAdapter.addImages(imagePaths);


            }else if(result.getData().getData() != null){
                //한장 일때
                if(imagePaths.size() + 1 > 10){
                    Toast.makeText(getBaseContext(),getString(R.string.str_too_many_images),Toast.LENGTH_SHORT).show();
                    return;
                }
                try{
                    String path = getImagePath(result.getData().getData(),0);
                    imageAdapter.addImage(path);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySaleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        editMode = getIntent().getStringExtra("editMode");

        setSupportActionBar(binding.toolbarRegisterGoods.toolbarDefault);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        isNeedPermissions();
        initImageAdapter();

        if(editMode.equals(CREATE_MODE)){
            binding.toolbarRegisterGoods.toolbarTitle.setText(getString(R.string.str_title_register_goods));
        }else if(editMode.equals(UPDATE_MODE)){
            binding.toolbarRegisterGoods.toolbarTitle.setText(getString(R.string.str_title_update_goods));
            updateGoodsId = getIntent().getIntExtra("goodsId",-1);
            getGoods();
        }

        //이미지
        imageAdapter.setOnItemClickListener(new RegisterGoodsImageAdapter.OnItemClickListener() {
            //이미지 추가
            @Override
            public void onItemAddClick(View v) {
                if (imagePaths.size() >= 10) {
                    Toast.makeText(getBaseContext(), getString(R.string.str_too_many_images), Toast.LENGTH_SHORT).show();
                    return;
                }
                startGettingAlbum();
            }

            //이미지 삭제
            @Override
            public void onItemRemoveClick(View v, int pos) {
                imageAdapter.removeItem(pos);
            }
        });

        //카테고리 선택
        binding.editCategoryGoodsRegister.setOnClickListener(v->{
            setCategory();
        });

        //가격 화폐 표시
        binding.editPriceGoodsRegister.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                DecimalFormat decimalFormat = new DecimalFormat("#,###");
                int maxLength = 9;
                String textPrice = binding.editPriceGoodsRegister.getText().toString();
                if (hasFocus) {
                    if (!textPrice.isEmpty()){
                        binding.editPriceGoodsRegister.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
                        binding.editPriceGoodsRegister.setText(price);
                        binding.editPriceGoodsRegister.setSelection(String.valueOf(price).length());
                    }
                } else {
                    if (!textPrice.isEmpty()) {
                        price = binding.editPriceGoodsRegister.getText().toString();
                        String result = decimalFormat.format(Integer.parseInt(price))+"원";
                        binding.editPriceGoodsRegister.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength+3)});
                        binding.editPriceGoodsRegister.setText(result);
                        binding.editPriceGoodsRegister.setSelection(binding.editPriceGoodsRegister.getText().toString().length());
                    }
                }
            }
        });
    }

    private void getGoods(){
        RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(getBaseContext()).create(RetrofitInterface.class);
        Call<Goods> callGetGoods = retrofit.callGoods(updateGoodsId);
        callGetGoods.enqueue(new Callback<Goods>() {
            @Override
            public void onResponse(Call<Goods> call, Response<Goods> response) {
                if(response.body()!= null){
                    Goods goods = response.body();
                    new Thread(()->{
                        try{
                            ArrayList<String> fileList = getPathList(goods.getGoodsImageList());
                            imageAdapter.addImages(fileList);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }).start();

                    binding.editNameGoodsRegister.setText(goods.getName());
                    binding.editCategoryGoodsRegister.setText(goods.getCategory());
                    binding.editPriceGoodsRegister.setText(goods.getFormattedPrice());
                    binding.editDetailsGoodsRegister.setText(goods.getDetails());
                }else{
                    Toast.makeText(getBaseContext(),getString(R.string.failure_on_network),Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Goods> call, Throwable t) {
                RetrofitClientInstance.setOnFailure(getBaseContext(),t);
            }
        });

    }

    private void saveGoods(){
        getWindow().getDecorView().clearFocus();
        String name = binding.editNameGoodsRegister.getText().toString();
        String category = binding.editCategoryGoodsRegister.getText().toString();
        String details = binding.editDetailsGoodsRegister.getText().toString();

        if(!isValidatedForm(name,category,price,details)){
            return;
        }

        binding.progressGoodsRegister.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        Goods goods = new Goods(name,category,Integer.parseInt(price),details);
        RequestBody goodsBody = RequestBody.create(new Gson().toJson(goods), MediaType.parse("application/json; charset=utf-8"));
        ImageProcessor ip = new ImageProcessor(getBaseContext());
        ArrayList<MultipartBody.Part> imagePartList = ip.getImagePartListFromPaths(imagePaths,"goods");

        RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(getBaseContext()).create(RetrofitInterface.class);

        JsonObject infoJson = new JsonObject();
        infoJson.addProperty("mode",editMode);
        if(editMode.equals(UPDATE_MODE)){
            infoJson.addProperty("goods_id",updateGoodsId);
        }
        RequestBody infoBody = RequestBody.create(infoJson.toString(),MediaType.parse("application/json; charset=utf-8"));
        Call<Integer> callSaveGoods = retrofit.callSaveGoods(goodsBody,imagePartList,infoBody);
        callSaveGoods.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if(response.body() != null){
                    int goodsId = response.body();
                    Log.d("goods","goodsId : "+goodsId);
                    Intent intent = new Intent(getBaseContext(),GoodsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("goodsId",goodsId);
                    startActivity(intent);
                    finish();
                }

            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                RetrofitClientInstance.setOnFailure(getBaseContext(),t);
                binding.progressGoodsRegister.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });
    }

    private ArrayList<String> getPathList(ArrayList<String> urlList) throws Exception{
        ArrayList<String> pathList = new ArrayList<>();
        for(int i=0;i<urlList.size();i++){
            URL url = new URL(urlList.get(i));
            Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            ImageProcessor ip = new ImageProcessor(getBaseContext());
            String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
            String fileName = "goods_"+timeStamp+i;
            pathList.add(ip.saveJPEGFromSingleBitmap(bitmap,fileName));
        }

        return pathList;
    }

    private boolean isValidatedForm(String name,String category,String price,String details){

        if(imagePaths.size() == 0) {
            Toast.makeText(getBaseContext(), getString(R.string.str_fail_no_image), Toast.LENGTH_SHORT).show();
            return false;
        }
        if(name.replaceAll(" ","").equals("")){
            Toast.makeText(getBaseContext(),getString(R.string.str_fail_no_name),Toast.LENGTH_SHORT).show();
            return false;
        }

        if(category.replaceAll(" ","").equals("")){
            Toast.makeText(getBaseContext(),getString(R.string.str_fail_no_category),Toast.LENGTH_SHORT).show();
            return false;
        }
        if(price.replaceAll(" ","").equals("")){
            Toast.makeText(getBaseContext(),getString(R.string.str_fail_no_price),Toast.LENGTH_SHORT).show();
            return false;
        }
        if(details.replaceAll(" ","").equals("")){
            Toast.makeText(getBaseContext(),getString(R.string.str_fail_no_details),Toast.LENGTH_SHORT).show();
            return false;

        }

        return true;
    }

    private void initImageAdapter(){
        imagePaths = new ArrayList<>();
        imageAdapter = new RegisterGoodsImageAdapter(imagePaths);
        ItemTouchHelper.Callback touchCallback = new ItemMoveCallback(imageAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(touchCallback);
        touchHelper.attachToRecyclerView(binding.recyclerGoodsImageRegister);

        binding.recyclerGoodsImageRegister.setLayoutManager(new LinearLayoutManager(getBaseContext(),LinearLayoutManager.HORIZONTAL,false));
        binding.recyclerGoodsImageRegister.setHasFixedSize(true);
        binding.recyclerGoodsImageRegister.setAdapter(imageAdapter);
    }


    private String getImagePath(Uri contentUri,int seperator) throws Exception{

        ImageProcessor ip = new ImageProcessor(getBaseContext());
        Bitmap imgBitmap = ip.getBitmapFromContentUri(contentUri);

        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String fileName = "goods_"+timeStamp+seperator;

        return ip.saveJPEGFromSingleBitmap(imgBitmap,fileName);
    }

    private void startGettingAlbum(){
        Intent albumIntent = new Intent();
        albumIntent.setType("image/jpeg");
        albumIntent.setAction(Intent.ACTION_GET_CONTENT);
        albumIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);

        getPictures.launch(albumIntent);
    }

    private void setCategory(){
        String[] categoryList = getResources().getStringArray(R.array.array_list_goods_category);
        AlertDialog.Builder builder = new AlertDialog.Builder(SaleActivity.this);
        builder.setTitle(getString(R.string.str_select_category))
                .setItems(categoryList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String category = categoryList[which];
                        binding.editCategoryGoodsRegister.setText(category);
                    }
                });
        builder.show();
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
        inflater.inflate(R.menu.menu_save_goods,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.menu_action_save_goods:
                saveGoods();
                return true;

            default:
                return false;
        }
    }
}