package com.project_bong.mymarket.sale;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.TaskStackBuilder;

import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.project_bong.mymarket.R;
import com.project_bong.mymarket.adapter.GoodsImagePagerAdapter;
import com.project_bong.mymarket.chat.ChatRoomActivity;
import com.project_bong.mymarket.databinding.ActivityGoodsBinding;
import com.project_bong.mymarket.dto.Goods;
import com.project_bong.mymarket.retrofit.RetrofitClientInstance;
import com.project_bong.mymarket.retrofit.RetrofitInterface;
import com.project_bong.mymarket.util.LoginUserGetter;
import com.project_bong.mymarket.util.TimeConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GoodsActivity extends AppCompatActivity {
    private ActivityGoodsBinding binding;
    private GoodsImagePagerAdapter imagePagerAdapter;
    private Goods goods;
    private int goodsId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGoodsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarGoods);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        goodsId = getIntent().getIntExtra("goodsId",-1);

        imagePagerAdapter = new GoodsImagePagerAdapter(new ArrayList<String>());
        binding.pagerForImageGoods.setOffscreenPageLimit(10);
        binding.pagerForImageGoods.setAdapter(imagePagerAdapter);
        binding.indicatorForPagerGoods.attachTo(binding.pagerForImageGoods);

        getGoods(goodsId);

        //판매자 메뉴 클릭리스너 셋
        SellerBtnClickListener sellerClickListener = new SellerBtnClickListener();
        binding.btnUpdateGoods.setOnClickListener(sellerClickListener);
        binding.btnChangeStateGoods.setOnClickListener(sellerClickListener);
        binding.btnDeleteGoods.setOnClickListener(sellerClickListener);

        //구매자 메뉴 클릭리스너 셋
        BuyerBtnClickListener buyerBtnClickListener = new BuyerBtnClickListener();
        binding.btnChatGoods.setOnClickListener(buyerBtnClickListener);


    }

    private void getGoods(int goodsId){
        RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(getBaseContext()).create(RetrofitInterface.class);
        Call<Goods> callGoods = retrofit.callGoods(goodsId);
        callGoods.enqueue(new Callback<Goods>() {
            @Override
            public void onResponse(Call<Goods> call, Response<Goods> response) {
                if(response.body() != null){
                    goods = response.body();

                    imagePagerAdapter.setList(goods.getGoodsImageList());
                    Glide.with(getBaseContext()).load(goods.getSeller().getUserImage()).centerCrop().circleCrop().into(binding.imgSellerGoods);
                    binding.txtNameSellerGoods.setText(goods.getSeller().getName());
                    binding.txtNameGoods.setText(goods.getName());
                    binding.txtPriceGoods.setText(goods.getFormattedPrice());
                    binding.txtCreatedAtGoods.setText(goods.getCreatedAtForUser());
                    binding.txtCategoryGoods.setText(goods.getCategory());
                    binding.txtDetailsGoods.setText(goods.getDetails());

                    if(goods.getState().equals(Goods.STATE_ON_SAIL)){
                        binding.txtStateGoods.setVisibility(View.GONE);
                    }else{
                        binding.txtStateGoods.setVisibility(View.VISIBLE);
                        binding.txtStateGoods.setText(goods.getState());
                    }



                    int userId = LoginUserGetter.getLoginUser().getId();
                    if(userId == goods.getSeller().getId()){
                        binding.menuForCustomerGoods.setVisibility(View.GONE);
                        binding.menuForSellerGoods.setVisibility(View.VISIBLE);
                    }else{
                        binding.menuForCustomerGoods.setVisibility(View.VISIBLE);
                        binding.menuForSellerGoods.setVisibility(View.GONE);
                    }



                }else{
                    Toast.makeText(getBaseContext(),getString(R.string.str_deleted_goods),Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Goods> call, Throwable t) {
                RetrofitClientInstance.setOnFailure(getBaseContext(),t);
                finish();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }else{
            return false;
        }
    }

    private class SellerBtnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_update_goods:
                    //상품수정
                    startUpdatingGoods();
                    break;
                case R.id.btn_change_state_goods:
                    //상품상태변경
                    changeState();
                    break;
                case R.id.btn_delete_goods:
                    //상품삭제
                    deleteGoods();
                    break;

                default:
                    break;
            }
        }

        private void startUpdatingGoods(){
            Intent intent = new Intent(getBaseContext(),SaleActivity.class);
            intent.putExtra("editMode","update");
            intent.putExtra("goodsId",goodsId);
            startActivity(intent);
        }

        private void changeState(){

            List<String> listState = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.array_list_goods_state)));
            int currentStatePosition = -1;
            for(int i=0;i<listState.size();i++){
                if(listState.get(i).equals(goods.getState())){
                    currentStatePosition = i;
                    break;
                }
            }

            Log.d("state","size : "+listState.size()+" current : "+currentStatePosition);

            if(currentStatePosition != -1){
                listState.remove(currentStatePosition);
            }
            String[] arrayState = listState.toArray(new String[listState.size()]);


            AlertDialog.Builder builder = new AlertDialog.Builder(GoodsActivity.this)
                    .setTitle(getString(R.string.str_change_state_goods))
                    .setItems(arrayState, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(getBaseContext()).create(RetrofitInterface.class);
                            Call<String> callChangeState = retrofit.callChangeState(goodsId,arrayState[which]);
                            callChangeState.enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    if(response.body() != null){
                                        if(response.body().equals("success")){
                                            Intent goodsIntent = new Intent(getBaseContext(),GoodsActivity.class);
                                            goodsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            goodsIntent.putExtra("goodsId",goodsId);

                                            if(arrayState[which].equals(Goods.STATE_SOLD_OUT)){
                                                Intent selectBuyerIntent = new Intent(getBaseContext(), SelectBuyerActivity.class);
                                                selectBuyerIntent.putExtra("goodsId",goodsId);

                                                startActivities(new Intent[]{goodsIntent,selectBuyerIntent});
                                            }else{
                                                startActivity(goodsIntent);
                                                Toast.makeText(getBaseContext(),getString(R.string.str_success_change_state),Toast.LENGTH_SHORT).show();
                                            }

                                            //구매자 선택 Activity 추가해야 함
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
            builder.show();
        }

        private void deleteGoods(){
            AlertDialog.Builder builder = new AlertDialog.Builder(GoodsActivity.this)
                    .setTitle(getString(R.string.str_delete_goods))
                    .setMessage(getString(R.string.str_warning_delete_goods))
                    .setNegativeButton("취소",null)
                    .setPositiveButton("삭제하기", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(getBaseContext()).create(RetrofitInterface.class);
                            Call<String> callDeleteGoods = retrofit.callDeleteGoods(goodsId);
                            callDeleteGoods.enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    if(response.body() != null && response.body().equals("success")){
                                        Toast.makeText(getBaseContext(),getString(R.string.str_success_delete_goods),Toast.LENGTH_SHORT).show();
                                        finish();

                                    }else{
                                        Toast.makeText(getBaseContext(),getString(R.string.failure_on_network),Toast.LENGTH_SHORT).show();
                                        Log.d("failure",response.body());
                                    }
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    RetrofitClientInstance.setOnFailure(getBaseContext(),t);
                                }
                            });
                        }
                    });
            builder.show();
        }
    }

    private class BuyerBtnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_favorite_goods:
                    checkFavoriteGoods();
                    break;

                case R.id.btn_chat_goods:
                    enterChatRoom();
                    break;

                default:
                    break;
            }
        }

        private void checkFavoriteGoods(){

        }

        private void enterChatRoom(){
            String timeUTC = new TimeConverter().getUTC();
            RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(getBaseContext()).create(RetrofitInterface.class);
            Call<Integer> callConfirmChatRoom = retrofit.callConfirmChatRoom(goodsId,goods.getSeller().getId(),timeUTC);
            callConfirmChatRoom.enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, Response<Integer> response) {
                    if(response.body() != null){
                        int roomId = response.body();
                        Log.d("chat","roomId : "+roomId);
                        Intent intent = new Intent(getBaseContext(),ChatRoomActivity.class);
                        intent.putExtra("roomId",roomId);
                        startActivity(intent);

                    }
                    Log.d("chat","onResponse");
                }

                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    RetrofitClientInstance.setOnFailure(getBaseContext(),t);
                }
            });

        }
    }


}
