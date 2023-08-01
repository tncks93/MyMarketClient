package com.project_bong.mymarket.sale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.project_bong.mymarket.R;
import com.project_bong.mymarket.adapter.SelectBuyerAdapter;
import com.project_bong.mymarket.databinding.ActivitySelectBuyerBinding;
import com.project_bong.mymarket.dto.Goods;
import com.project_bong.mymarket.dto.User;
import com.project_bong.mymarket.retrofit.RetrofitClientInstance;
import com.project_bong.mymarket.retrofit.RetrofitInterface;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectBuyerActivity extends AppCompatActivity {
    private ActivitySelectBuyerBinding binding;
    private int goodsId;
    private SelectBuyerAdapter selectBuyerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySelectBuyerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarSelectBuyer.toolbarDefault);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.toolbarSelectBuyer.toolbarTitle.setText(getString(R.string.str_title_select_buyer));

        goodsId = getIntent().getIntExtra("goodsId",-1);

        getGoods(goodsId);

        initSelectBuyerAdapter();

        getBuyerList(goodsId);

        binding.btnNotSelectBuyer.setOnClickListener(v->{
            finish();
        });

        binding.btnSelectBuyer.setOnClickListener(v->{
            savePurchase();
        });



    }

    private void getGoods(int goodsId){
        RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(getBaseContext())
                .create(RetrofitInterface.class);
        Call<Goods> callGoods = retrofit.callGoods(goodsId);
        callGoods.enqueue(new Callback<Goods>() {
            @Override
            public void onResponse(Call<Goods> call, Response<Goods> response) {
                if(response.body() != null){
                    Goods goods = response.body();
                    Glide.with(getBaseContext()).load(goods.getGoodsImageList().get(0))
                                    .transform(new CenterCrop(),new RoundedCorners(20)).into(binding.imgGoodsSelectBuyer);

                    binding.txtCategorySelectBuyer.setText(goods.getCategory());
                    binding.txtNameSelectBuyer.setText(goods.getName());
                    binding.txtPriceSelectBuyer.setText(goods.getFormattedPrice());
                    binding.txtStateSelectBuyer.setText(goods.getState());
                    binding.txtCreatedAtSelectBuyer.setText(goods.getCreatedAtForUser());
                }
            }

            @Override
            public void onFailure(Call<Goods> call, Throwable t) {
                RetrofitClientInstance.setOnFailure(getBaseContext(),t);
                finish();
            }
        });
    }

    private void initSelectBuyerAdapter(){
        selectBuyerAdapter = new SelectBuyerAdapter(new ArrayList<User>());
        binding.recyclerSelectBuyer.setLayoutManager(new LinearLayoutManager(getBaseContext(),LinearLayoutManager.VERTICAL,false));
        binding.recyclerSelectBuyer.addItemDecoration(new DividerItemDecoration(getBaseContext(),LinearLayoutManager.VERTICAL));
        binding.recyclerSelectBuyer.setHasFixedSize(true);
        binding.recyclerSelectBuyer.setItemAnimator(null);
        selectBuyerAdapter.setOnItemClickListener(new SelectBuyerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                binding.btnSelectBuyer.setEnabled(true);
                selectBuyerAdapter.setClick(pos);
            }
        });

        binding.recyclerSelectBuyer.setAdapter(selectBuyerAdapter);
    }

    private void getBuyerList(int goodsId){
        RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(getBaseContext()).create(RetrofitInterface.class);
        Call<ArrayList<User>> callBuyerList = retrofit.callBuyerList(goodsId);
        callBuyerList.enqueue(new Callback<ArrayList<User>>() {
            @Override
            public void onResponse(Call<ArrayList<User>> call, Response<ArrayList<User>> response) {
                if(response.body() != null){
                    if(response.body().size() == 0){
                        binding.recyclerSelectBuyer.setVisibility(View.GONE);
                        binding.txtEmptyListSelectBuyer.setVisibility(View.VISIBLE);
                        return;
                    }
                    selectBuyerAdapter.setItems(response.body());
                }
            }

            @Override
            public void onFailure(Call<ArrayList<User>> call, Throwable t) {
                RetrofitClientInstance.setOnFailure(getBaseContext(),t);
                finish();
            }
        });

    }

    private void savePurchase(){
        int selectedBuyerId = selectBuyerAdapter.getSelectedBuyerId();

        if(selectedBuyerId == -1){
            return;
        }

        RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(getBaseContext()).create(RetrofitInterface.class);
        Call<String> callSavePurchase = retrofit.callSavePurchase(selectedBuyerId,goodsId);
        callSavePurchase.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.body() != null){
                    String res = response.body();
                    Log.d("resPurchase",res);
                    if(res.equals("success")){
                        finish();
                    }else{
                        Toast.makeText(getBaseContext(),getString(R.string.failure_on_network),Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                RetrofitClientInstance.setOnFailure(getBaseContext(),t);
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}