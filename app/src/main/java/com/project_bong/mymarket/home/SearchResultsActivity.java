package com.project_bong.mymarket.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.project_bong.mymarket.adapter.GoodsRecyclerAdapter;
import com.project_bong.mymarket.databinding.ActivitySearchResultsBinding;
import com.project_bong.mymarket.dto.Goods;
import com.project_bong.mymarket.dto.SearchFilter;
import com.project_bong.mymarket.retrofit.RetrofitClientInstance;
import com.project_bong.mymarket.retrofit.RetrofitInterface;

import java.lang.reflect.Array;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchResultsActivity extends AppCompatActivity {
    private ActivitySearchResultsBinding binding;

    private SearchFilterDialogFragment fragmentSearchFilterDialog;
    private SearchFilter searchFilter;
    private GoodsRecyclerAdapter goodsAdapter;
    private ArrayList<Goods> goodsList;

    private boolean isLoading = false;
    private boolean isLastPage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSearchFilter();
        initGoodsAdapter();
        getGoodsSearched(0);

        binding.btnSetCategorySearchResults.setOnClickListener(v->{
            displaySearchFilterFragment();

        });

        binding.layoutRefreshSearchResults.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("searchFilter","isLoadingWhenRefresh : "+isLoading);
                if(!isLoading){
                    getGoodsSearched(0);
                }else{
                    finishRefreshing();
                }
            }
        });

        binding.recyclerSearchResults.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItemPosition = ((LinearLayoutManager)binding.recyclerSearchResults.getLayoutManager()).findLastCompletelyVisibleItemPosition() +1;
                int itemTotalCount = goodsAdapter.getItemCount();
                if(lastVisibleItemPosition == itemTotalCount && !isLoading && !isLastPage && lastVisibleItemPosition != 0){
                    int lastIdx = goodsAdapter.getItem(goodsAdapter.getItemCount()-1).getId();

                    getGoodsSearched(lastIdx);

                }
            }
        });
    }

    private void getGoodsSearched(int pageIdx){
        isLoading = true;

        if(pageIdx == 0){
            isLastPage = false;
        }

        RequestBody bodyFilter = RequestBody.create(new Gson().toJson(searchFilter), MediaType.parse("application/json; charset=utf-8"));
        RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(getBaseContext()).create(RetrofitInterface.class);
        Call<ArrayList<Goods>> callSearchedGoods = retrofit.callSearchedGoodsList(pageIdx,bodyFilter);
        callSearchedGoods.enqueue(new Callback<ArrayList<Goods>>() {
            @Override
            public void onResponse(Call<ArrayList<Goods>> call, Response<ArrayList<Goods>> response) {
                if(response.body() != null){
                    Log.d("searchFilter","result -> pageIdx : "+pageIdx+" size : "+response.body().size());

                    if(response.body().size() == 0){
                        isLastPage = true;
                        isLoading = false;
                        return;
                    }

                    if(pageIdx == 0){
                        goodsAdapter.resetList(response.body());

                    }else{
                        goodsAdapter.addNewList(response.body());
                    }

                }
                finishRefreshing();
                isLoading = false;
            }

            @Override
            public void onFailure(Call<ArrayList<Goods>> call, Throwable t) {
                RetrofitClientInstance.setOnFailure(getBaseContext(),t);
                finishRefreshing();
            }
        });

    }

    private void getSearchFilter(){
        searchFilter = new Gson().fromJson(getIntent().getStringExtra("searchFilter"),SearchFilter.class);
        Log.d("searchFilter","SearchResultsActivity -> category : "+searchFilter.getCategory()+" keyword : "+searchFilter.getKeyword());
    }

    private void initGoodsAdapter(){
        goodsList = new ArrayList<>();
        goodsAdapter = new GoodsRecyclerAdapter(goodsList);
        goodsAdapter.setHasStableIds(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getBaseContext(),LinearLayoutManager.VERTICAL,false);
        binding.recyclerSearchResults.addItemDecoration(new DividerItemDecoration(getBaseContext(),LinearLayoutManager.VERTICAL));
        binding.recyclerSearchResults.setLayoutManager(linearLayoutManager);
        binding.recyclerSearchResults.setAdapter(goodsAdapter);
    }

    private void displaySearchFilterFragment(){

        String jsonSearchFilter = new Gson().toJson(searchFilter);
        fragmentSearchFilterDialog = SearchFilterDialogFragment.newInstance(jsonSearchFilter);
        fragmentSearchFilterDialog.show(getSupportFragmentManager(),null);
    }

    private void finishRefreshing(){
        if(binding.layoutRefreshSearchResults.isRefreshing()){
            binding.layoutRefreshSearchResults.setRefreshing(false);
        }
    }

}