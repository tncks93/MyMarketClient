package com.project_bong.mymarket.my_page;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.project_bong.mymarket.R;
import com.project_bong.mymarket.adapter.PurchaseListAdapter;
import com.project_bong.mymarket.databinding.ActivityPurchaseBinding;
import com.project_bong.mymarket.dto.Goods;
import com.project_bong.mymarket.retrofit.RetrofitClientInstance;
import com.project_bong.mymarket.retrofit.RetrofitInterface;
import com.project_bong.mymarket.sale.GoodsActivity;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PurchaseActivity extends AppCompatActivity {
    private ActivityPurchaseBinding binding;
    private boolean isLoading = true;
    private PurchaseListAdapter purchaseAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPurchaseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarPurchase.toolbarDefault);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbarPurchase.toolbarTitle.setText(getString(R.string.str_purchase_history));

        initAdapter();
        getPurchase(0);

        purchaseAdapter.setOnItemClickListener(new PurchaseListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                int goodsId = purchaseAdapter.getItem(pos).getId();
                Intent intent = new Intent(getBaseContext(), GoodsActivity.class);
                intent.putExtra("goodsId",goodsId);
                startActivity(intent);
            }

            @Override
            public void onMoreClick(View v, int pos) {
                PopupMenu popupMenu = new PopupMenu(PurchaseActivity.this,v);
                getMenuInflater().inflate(R.menu.menu_purchase_history,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId() == R.id.menu_action_delete_purchase){
                            removePurchase(purchaseAdapter.getItem(pos).getId());
                            purchaseAdapter.removeItem(pos);
                            return true;
                        }else{
                            return false;
                        }
                    }
                });
                popupMenu.show();

            }
        });

        binding.recyclerPurchase.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItemPosition = ((LinearLayoutManager)binding.recyclerPurchase.getLayoutManager()).findLastCompletelyVisibleItemPosition() +1;
                int itemTotalCount = purchaseAdapter.getItemCount();
                if(lastVisibleItemPosition == itemTotalCount && !isLoading && lastVisibleItemPosition != 0){
                    int pagingIdx = purchaseAdapter.getItem(itemTotalCount-1).getId();

                    getPurchase(pagingIdx);

                }
            }
        });
    }

    private void initAdapter(){
        purchaseAdapter = new PurchaseListAdapter(new ArrayList<Goods>());
        purchaseAdapter.setHasStableIds(true);
        binding.recyclerPurchase.setLayoutManager(new LinearLayoutManager(getBaseContext(),LinearLayoutManager.VERTICAL,false));
        binding.recyclerPurchase.addItemDecoration(new DividerItemDecoration(getBaseContext(),LinearLayoutManager.VERTICAL));
        binding.recyclerPurchase.setAdapter(purchaseAdapter);
    }

    private void getPurchase(int pagingIdx){
        isLoading = true;
        RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(getBaseContext()).create(RetrofitInterface.class);
        Call<ArrayList<Goods>> callGetPurchase = retrofit.callGetPurchase(pagingIdx);
        callGetPurchase.enqueue(new Callback<ArrayList<Goods>>() {
            @Override
            public void onResponse(Call<ArrayList<Goods>> call, Response<ArrayList<Goods>> response) {
                if(response.body() != null){
                    purchaseAdapter.addItems(response.body());
                    if(purchaseAdapter.getItemCount() == 0){
                        binding.recyclerPurchase.setVisibility(View.GONE);
                        binding.txtHaveNoPurchases.setVisibility(View.VISIBLE);
                    }
                }
                isLoading = false;
            }

            @Override
            public void onFailure(Call<ArrayList<Goods>> call, Throwable t) {
                RetrofitClientInstance.setOnFailure(getBaseContext(),t);
                isLoading = false;
            }
        });
    }

    private void removePurchase(int goodsId){

        RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(getBaseContext()).create(RetrofitInterface.class);
        Call<Void> callRemovePurchase = retrofit.callRemovePurchase(goodsId);
        callRemovePurchase.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d("removePurchase","삭제 성공");
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
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