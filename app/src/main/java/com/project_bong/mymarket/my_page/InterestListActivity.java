package com.project_bong.mymarket.my_page;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.project_bong.mymarket.R;
import com.project_bong.mymarket.adapter.InterestListAdapter;
import com.project_bong.mymarket.databinding.ActivityInterestListBinding;
import com.project_bong.mymarket.dto.InterestingGoods;
import com.project_bong.mymarket.retrofit.RetrofitClientInstance;
import com.project_bong.mymarket.retrofit.RetrofitInterface;
import com.project_bong.mymarket.sale.GoodsActivity;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InterestListActivity extends AppCompatActivity {
    private ActivityInterestListBinding binding;
    private boolean isLoading = true;
    private InterestListAdapter interestAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInterestListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarInterestList.toolbarDefault);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.toolbarInterestList.toolbarTitle.setText(getString(R.string.str_interest_goods));
        initRecyclerView();

        getInterests("0");

        binding.layoutRefreshInterests.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //새로고침
                getInterests("0");
            }
        });

        binding.recyclerListInterests.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItemPosition = ((LinearLayoutManager)binding.recyclerListInterests.getLayoutManager()).findLastCompletelyVisibleItemPosition()+1;
                int itemTotalCount = interestAdapter.getItemCount();
                Log.d("paging","last : "+lastVisibleItemPosition+" total : "+itemTotalCount+" isLoading : "+isLoading);
                if(lastVisibleItemPosition == itemTotalCount && !isLoading && lastVisibleItemPosition != 0){
                    String pagingIdx = interestAdapter.getItem(interestAdapter.getItemCount()-1).getSaved_at();
                    Log.d("paging","pagingIdx : "+pagingIdx);
                    getInterests(pagingIdx);

                }
            }
        });

    }

    private void initRecyclerView(){
        interestAdapter = new InterestListAdapter(new ArrayList<InterestingGoods>());
        interestAdapter.setHasStableIds(true);
        binding.recyclerListInterests.setLayoutManager(new LinearLayoutManager(getBaseContext(),LinearLayoutManager.VERTICAL,false));
        binding.recyclerListInterests.addItemDecoration(new DividerItemDecoration(getBaseContext(),LinearLayoutManager.VERTICAL));
        binding.recyclerListInterests.setAdapter(interestAdapter);

        interestAdapter.setOnItemClickListener(new InterestListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                int goodsId = interestAdapter.getItem(pos).getId();
                Intent intent = new Intent(getBaseContext(), GoodsActivity.class);
                intent.putExtra("goodsId",goodsId);
                startActivity(intent);
            }

            @Override
            public void onLikeClick(View v, int pos) {
                setInterest(pos);
            }
        });
    }

    private void getInterests(String pagingIdx){
        isLoading = true;
        RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(getBaseContext()).create(RetrofitInterface.class);
        Call<ArrayList<InterestingGoods>> callGetInterests = retrofit.callGetInterest(pagingIdx);
        callGetInterests.enqueue(new Callback<ArrayList<InterestingGoods>>() {
            @Override
            public void onResponse(Call<ArrayList<InterestingGoods>> call, Response<ArrayList<InterestingGoods>> response) {
                if(response.body() != null){
                    Log.d("paging","onResponse ItemCount : "+response.body().size());
                    if(pagingIdx.equals("0")){
                        interestAdapter.setNewItems(response.body());
                        binding.layoutRefreshInterests.setRefreshing(false);
                        isLoading = false;
                        return;
                    }

                    interestAdapter.addNewItems(response.body());
                }
                isLoading = false;

            }

            @Override
            public void onFailure(Call<ArrayList<InterestingGoods>> call, Throwable t) {
                RetrofitClientInstance.setOnFailure(getBaseContext(),t);
                binding.layoutRefreshInterests.setRefreshing(false);
                isLoading = false;
            }
        });
    }

    private void setInterest(int pos){
        int goodsId = interestAdapter.getItem(pos).getId();
        RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(getBaseContext()).create(RetrofitInterface.class);
        Call<Boolean> callSetInterest = retrofit.callSetIsInterest(goodsId);
        callSetInterest.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if(response.body() != null){
                    interestAdapter.setInterest(pos,response.body());
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
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