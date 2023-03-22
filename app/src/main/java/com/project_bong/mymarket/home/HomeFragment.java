package com.project_bong.mymarket.home;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.project_bong.mymarket.R;
import com.project_bong.mymarket.adapter.GoodsRecyclerAdapter;
import com.project_bong.mymarket.databinding.FragmentHomeBinding;
import com.project_bong.mymarket.dto.Goods;
import com.project_bong.mymarket.retrofit.RetrofitClientInstance;
import com.project_bong.mymarket.retrofit.RetrofitInterface;
import com.project_bong.mymarket.sale.GoodsActivity;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    private GoodsRecyclerAdapter goodsAdapter;
    private ArrayList<Goods> goodsList;

    private boolean isLoading = false;
    private boolean isLastPage = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.toolbarHome.toolbarDefault.inflateMenu(R.menu.menu_home);
        binding.toolbarHome.toolbarTitle.setText(R.string.str_title_home);

        initGoodsRecyclerView();
        getGoodsList(0);

        binding.toolbarHome.toolbarDefault.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId()==R.id.menu_action_search){
                    //검색 Activity Start
                    Intent intent = new Intent(getContext(),SearchActivity.class);
                    startActivity(intent);

                    return true;
                }
                return false;
            }
        });

        binding.layoutRefreshHome.setOnRefreshListener(()->{
            isLastPage = false;
            getGoodsList(0);
        });

        goodsAdapter.setOnItemClickListener(new GoodsRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                int goodsId = goodsAdapter.getItem(pos).getId();
                Intent intent = new Intent(getContext(), GoodsActivity.class);
                intent.putExtra("goodsId",goodsId);
                startActivity(intent);
            }
        });

        //recyclerView 페이징
        binding.recyclerViewGoodsHome.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItemPosition = ((LinearLayoutManager)binding.recyclerViewGoodsHome.getLayoutManager()).findLastCompletelyVisibleItemPosition() +1;
                int itemTotalCount = goodsAdapter.getItemCount();
                if(lastVisibleItemPosition == itemTotalCount && !isLoading && !isLastPage && lastVisibleItemPosition != 0){
                    int lastIdx = goodsAdapter.getItem(goodsAdapter.getItemCount()-1).getId();

                    getGoodsList(lastIdx);

                }
            }
        });

    }

    private void initGoodsRecyclerView(){
        goodsList = new ArrayList<>();
        goodsAdapter = new GoodsRecyclerAdapter(goodsList);
        goodsAdapter.setHasStableIds(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        binding.recyclerViewGoodsHome.addItemDecoration(new DividerItemDecoration(getActivity(),LinearLayoutManager.VERTICAL));
        binding.recyclerViewGoodsHome.setLayoutManager(linearLayoutManager);
        binding.recyclerViewGoodsHome.setAdapter(goodsAdapter);
    }

    private void getGoodsList(int pageIndex){
        isLoading = true;
        RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(getContext()).create(RetrofitInterface.class);
        Call<ArrayList<Goods>> callGoodsList = retrofit.callGoodsList(pageIndex);
        callGoodsList.enqueue(new Callback<ArrayList<Goods>>() {
            @Override
            public void onResponse(Call<ArrayList<Goods>> call, Response<ArrayList<Goods>> response) {
                if(response.body() != null){
                    if(response.body().size() == 0){
                        isLastPage = true;
                        return;
                    }

                    if(pageIndex == 0){
                        goodsAdapter.resetList(response.body());

                    }else{
                        goodsAdapter.addNewList(response.body());
                    }

                    Log.d("paging","firstId : "+response.body().get(0).getId()+" count : "+response.body().size());

                }

                finishRefreshing();
                isLoading = false;
            }

            @Override
            public void onFailure(Call<ArrayList<Goods>> call, Throwable t) {
                RetrofitClientInstance.setOnFailure(getActivity(),t);
                finishRefreshing();
                isLoading = false;
            }
        });

    }

    private void finishRefreshing(){
        if(binding.layoutRefreshHome.isRefreshing()){
            binding.layoutRefreshHome.setRefreshing(false);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}